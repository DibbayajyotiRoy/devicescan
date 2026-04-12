package auth

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

// GoogleTokenInfo represents the decoded Google ID token payload.
type GoogleTokenInfo struct {
	Sub           string `json:"sub"`
	Email         string `json:"email"`
	EmailVerified string `json:"email_verified"`
	Name          string `json:"name"`
	Picture       string `json:"picture"`
	Aud           string `json:"aud"`
	Iss           string `json:"iss"`
	Exp           string `json:"exp"`
}

// VerifyGoogleIDToken verifies a Google ID token via Google's tokeninfo endpoint.
func VerifyGoogleIDToken(ctx context.Context, idToken string) (*GoogleTokenInfo, error) {
	clientID := os.Getenv("GOOGLE_CLIENT_ID")
	androidClientID := os.Getenv("GOOGLE_ANDROID_CLIENT_ID")
	if clientID == "" && androidClientID == "" {
		return nil, fmt.Errorf("GOOGLE_CLIENT_ID not configured")
	}

	url := "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create request: %w", err)
	}

	client := &http.Client{Timeout: 5 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("verify token: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("read response: %w", err)
	}

	if resp.StatusCode != 200 {
		return nil, fmt.Errorf("token verification failed: %s", string(body))
	}

	var info GoogleTokenInfo
	if err := json.Unmarshal(body, &info); err != nil {
		return nil, fmt.Errorf("parse response: %w", err)
	}

	if info.Aud != clientID && info.Aud != androidClientID {
		return nil, fmt.Errorf("token audience mismatch: got %s, want web=%s or android=%s", info.Aud, clientID, androidClientID)
	}

	if info.Iss != "accounts.google.com" && info.Iss != "https://accounts.google.com" {
		return nil, fmt.Errorf("invalid issuer: %s", info.Iss)
	}

	return &info, nil
}
