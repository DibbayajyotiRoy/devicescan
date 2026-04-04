package models

import "time"

type User struct {
	ID          int       `json:"id"`
	GoogleID    string    `json:"googleId,omitempty"`
	Email       string    `json:"email"`
	Name        string    `json:"name"`
	PictureURL  *string   `json:"pictureUrl,omitempty"`
	Role        string    `json:"role"`
	CreatedAt   time.Time `json:"createdAt"`
	LastLoginAt time.Time `json:"lastLoginAt"`
	LoginCount  int       `json:"loginCount"`
}

type Session struct {
	ID           int       `json:"id"`
	UserID       int       `json:"userId"`
	RefreshToken string    `json:"-"`
	ExpiresAt    time.Time `json:"expiresAt"`
	CreatedAt    time.Time `json:"createdAt"`
	UserAgent    *string   `json:"userAgent,omitempty"`
	IPCountry    *string   `json:"ipCountry,omitempty"`
}

type AuthResponse struct {
	AccessToken  string `json:"accessToken"`
	RefreshToken string `json:"refreshToken,omitempty"`
	ExpiresIn    int    `json:"expiresIn"`
	User         *User  `json:"user,omitempty"`
}

type AdminLoginRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

type GoogleAuthRequest struct {
	IDToken string `json:"idToken"`
}
