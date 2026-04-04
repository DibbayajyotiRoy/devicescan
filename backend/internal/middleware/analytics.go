package middleware

import (
	"context"
	"log"
	"net/http"
	"strings"
	"time"

	"devicelens-backend/internal/models"
	"github.com/jackc/pgx/v5/pgxpool"
)

type statusWriter struct {
	http.ResponseWriter
	code int
}

func (sw *statusWriter) WriteHeader(code int) {
	sw.code = code
	sw.ResponseWriter.WriteHeader(code)
}

// Analytics collects per-request telemetry asynchronously.
type Analytics struct {
	pool   *pgxpool.Pool
	events chan models.ApiEvent
}

func NewAnalytics(pool *pgxpool.Pool) *Analytics {
	a := &Analytics{
		pool:   pool,
		events: make(chan models.ApiEvent, 10000),
	}
	go a.worker()
	return a
}

func (a *Analytics) Handler(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Skip analytics for admin panel static assets
		if strings.HasPrefix(r.URL.Path, "/admin") && r.Method == http.MethodGet {
			next.ServeHTTP(w, r)
			return
		}

		start := time.Now()
		sw := &statusWriter{ResponseWriter: w, code: 200}

		next.ServeHTTP(sw, r)

		var userID *int
		if uid, ok := r.Context().Value(ContextKeyUserID).(int); ok {
			userID = &uid
		}

		event := models.ApiEvent{
			UserID:      userID,
			Endpoint:    r.URL.Path,
			Method:      r.Method,
			StatusCode:  sw.code,
			LatencyMs:   int(time.Since(start).Milliseconds()),
			AppVersion:  r.Header.Get("X-App-Version"),
			OsVersion:   r.Header.Get("X-OS-Version"),
			DeviceModel: r.Header.Get("X-Device-Model"),
		}

		// Non-blocking send
		select {
		case a.events <- event:
		default:
		}
	})
}

func (a *Analytics) worker() {
	batch := make([]models.ApiEvent, 0, 100)
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case event := <-a.events:
			batch = append(batch, event)
			if len(batch) >= 100 {
				a.flush(batch)
				batch = make([]models.ApiEvent, 0, 100)
			}
		case <-ticker.C:
			if len(batch) > 0 {
				a.flush(batch)
				batch = make([]models.ApiEvent, 0, 100)
			}
		}
	}
}

func (a *Analytics) flush(events []models.ApiEvent) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	for _, e := range events {
		_, err := a.pool.Exec(ctx,
			`INSERT INTO api_events (user_id, endpoint, method, status_code, latency_ms, country, app_version, os_version, device_model)
			 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)`,
			e.UserID, e.Endpoint, e.Method, e.StatusCode, e.LatencyMs,
			nilIfEmpty(e.Country), nilIfEmpty(e.AppVersion), nilIfEmpty(e.OsVersion), nilIfEmpty(e.DeviceModel),
		)
		if err != nil {
			log.Printf("analytics insert error: %v", err)
		}
	}
}

func nilIfEmpty(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
