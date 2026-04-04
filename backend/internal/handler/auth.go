package handler

import (
	"crypto/subtle"
	"net/http"
	"os"
	"time"

	"devicelens-backend/internal/auth"
	"devicelens-backend/internal/middleware"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// AdminLoginHandler authenticates the superadmin with username/password.
func AdminLoginHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			Username string `json:"username"`
			Password string `json:"password"`
		}
		if err := readJSON(r, &req); err != nil {
			writeJSON(w, http.StatusBadRequest, map[string]string{"error": "Invalid request body"})
			return
		}

		expectedPassword := os.Getenv("ADMIN_PASSWORD")
		if expectedPassword == "" {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Admin login not configured"})
			return
		}

		usernameOK := subtle.ConstantTimeCompare([]byte(req.Username), []byte("admin")) == 1
		passwordOK := subtle.ConstantTimeCompare([]byte(req.Password), []byte(expectedPassword)) == 1

		if !usernameOK || !passwordOK {
			writeJSON(w, http.StatusUnauthorized, map[string]string{"error": "Invalid credentials"})
			return
		}

		token, err := auth.IssueAccessToken(0, "admin", "superadmin")
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to issue token"})
			return
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"accessToken": token,
			"expiresIn":   86400,
			"user": map[string]string{
				"username": "admin",
				"role":     "superadmin",
			},
		})
	}
}

// GoogleAuthHandler exchanges a Google ID token for a DeviceLens JWT.
func GoogleAuthHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			IDToken string `json:"idToken"`
		}
		if err := readJSON(r, &req); err != nil || req.IDToken == "" {
			writeJSON(w, http.StatusBadRequest, map[string]string{"error": "idToken is required"})
			return
		}

		info, err := auth.VerifyGoogleIDToken(r.Context(), req.IDToken)
		if err != nil {
			writeJSON(w, http.StatusUnauthorized, map[string]string{
				"error": "Google token verification failed", "details": err.Error(),
			})
			return
		}

		// Upsert user
		var userID int
		var name, email, role string
		var pictureURL *string
		var createdAt, lastLoginAt time.Time
		var loginCount int

		err = pool.QueryRow(r.Context(),
			`INSERT INTO users (google_id, email, name, picture_url)
			 VALUES ($1, $2, $3, $4)
			 ON CONFLICT (google_id) DO UPDATE SET
			   last_login_at = NOW(),
			   login_count = users.login_count + 1,
			   name = $3,
			   picture_url = $4
			 RETURNING id, email, name, picture_url, role, created_at, last_login_at, login_count`,
			info.Sub, info.Email, info.Name, nilStr(info.Picture),
		).Scan(&userID, &email, &name, &pictureURL, &role, &createdAt, &lastLoginAt, &loginCount)

		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to create/update user"})
			return
		}

		accessToken, err := auth.IssueAccessToken(userID, email, role)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to issue token"})
			return
		}

		refreshToken, expiresAt, err := auth.IssueRefreshToken(userID)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to issue refresh token"})
			return
		}

		// Store refresh token
		pool.Exec(r.Context(),
			`INSERT INTO sessions (user_id, refresh_token, expires_at, user_agent)
			 VALUES ($1, $2, $3, $4)`,
			userID, refreshToken, expiresAt, r.UserAgent(),
		)

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"accessToken":  accessToken,
			"refreshToken": refreshToken,
			"expiresIn":    900,
			"user": map[string]interface{}{
				"id":         userID,
				"email":      email,
				"name":       name,
				"pictureUrl": pictureURL,
				"role":       role,
				"createdAt":  createdAt,
			},
		})
	}
}

// RefreshTokenHandler issues a new access token using a valid refresh token.
func RefreshTokenHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			RefreshToken string `json:"refreshToken"`
		}
		if err := readJSON(r, &req); err != nil || req.RefreshToken == "" {
			writeJSON(w, http.StatusBadRequest, map[string]string{"error": "refreshToken is required"})
			return
		}

		claims, err := auth.ValidateToken(req.RefreshToken)
		if err != nil || claims.Role != "refresh" {
			writeJSON(w, http.StatusUnauthorized, map[string]string{"error": "Invalid refresh token"})
			return
		}

		// Verify session exists
		var sessionID int
		err = pool.QueryRow(r.Context(),
			`SELECT id FROM sessions WHERE refresh_token = $1 AND expires_at > NOW()`,
			req.RefreshToken,
		).Scan(&sessionID)
		if err == pgx.ErrNoRows {
			writeJSON(w, http.StatusUnauthorized, map[string]string{"error": "Session expired or revoked"})
			return
		}

		// Get user
		var email, role string
		err = pool.QueryRow(r.Context(),
			`SELECT email, role FROM users WHERE id = $1`, claims.UserID,
		).Scan(&email, &role)
		if err != nil {
			writeJSON(w, http.StatusUnauthorized, map[string]string{"error": "User not found"})
			return
		}

		accessToken, err := auth.IssueAccessToken(claims.UserID, email, role)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to issue token"})
			return
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"accessToken": accessToken,
			"expiresIn":   900,
		})
	}
}

// MeHandler returns the current user's profile.
func MeHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		role, _ := r.Context().Value(middleware.ContextKeyRole).(string)
		if role == "superadmin" {
			writeJSON(w, http.StatusOK, map[string]interface{}{
				"user": map[string]string{"username": "admin", "role": "superadmin"},
			})
			return
		}

		userID, _ := r.Context().Value(middleware.ContextKeyUserID).(int)
		var email, name, userRole string
		var pictureURL *string
		var createdAt, lastLoginAt time.Time
		var loginCount int

		err := pool.QueryRow(r.Context(),
			`SELECT email, name, picture_url, role, created_at, last_login_at, login_count
			 FROM users WHERE id = $1`, userID,
		).Scan(&email, &name, &pictureURL, &userRole, &createdAt, &lastLoginAt, &loginCount)
		if err != nil {
			writeJSON(w, http.StatusNotFound, map[string]string{"error": "User not found"})
			return
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"user": map[string]interface{}{
				"id": userID, "email": email, "name": name,
				"pictureUrl": pictureURL, "role": userRole,
				"createdAt": createdAt, "lastLoginAt": lastLoginAt,
				"loginCount": loginCount,
			},
		})
	}
}

func nilStr(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
