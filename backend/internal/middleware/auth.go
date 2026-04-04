package middleware

import (
	"context"
	"net/http"
	"strings"

	"devicelens-backend/internal/auth"
)

type contextKey string

const (
	ContextKeyUserID contextKey = "userId"
	ContextKeyEmail  contextKey = "email"
	ContextKeyRole   contextKey = "role"
)

// OptionalAuth extracts JWT claims if present, but does not require auth.
func OptionalAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token := extractBearerToken(r)
		if token != "" {
			claims, err := auth.ValidateToken(token)
			if err == nil {
				ctx := context.WithValue(r.Context(), ContextKeyUserID, claims.UserID)
				ctx = context.WithValue(ctx, ContextKeyEmail, claims.Email)
				ctx = context.WithValue(ctx, ContextKeyRole, claims.Role)
				r = r.WithContext(ctx)
			}
		}
		next.ServeHTTP(w, r)
	})
}

// RequireAuth rejects requests without a valid JWT.
func RequireAuth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token := extractBearerToken(r)
		if token == "" {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte(`{"error":"Authorization required"}`))
			return
		}

		claims, err := auth.ValidateToken(token)
		if err != nil {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusUnauthorized)
			w.Write([]byte(`{"error":"Invalid or expired token"}`))
			return
		}

		ctx := context.WithValue(r.Context(), ContextKeyUserID, claims.UserID)
		ctx = context.WithValue(ctx, ContextKeyEmail, claims.Email)
		ctx = context.WithValue(ctx, ContextKeyRole, claims.Role)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

// RequireSuperadmin rejects requests without superadmin role.
func RequireSuperadmin(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		role, ok := r.Context().Value(ContextKeyRole).(string)
		if !ok || role != "superadmin" {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusForbidden)
			w.Write([]byte(`{"error":"Superadmin access required"}`))
			return
		}
		next.ServeHTTP(w, r)
	})
}

func extractBearerToken(r *http.Request) string {
	h := r.Header.Get("Authorization")
	if strings.HasPrefix(h, "Bearer ") {
		return strings.TrimPrefix(h, "Bearer ")
	}
	return ""
}
