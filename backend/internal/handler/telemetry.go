package handler

import (
	"net/http"

	"devicelens-backend/internal/middleware"
	"github.com/jackc/pgx/v5/pgxpool"
)

// TelemetryHandler receives scan telemetry from the Android client.
func TelemetryHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			DeviceCount    int `json:"deviceCount"`
			ThreatCount    int `json:"threatCount"`
			WarningCount   int `json:"warningCount"`
			ScanDurationMs int `json:"scanDurationMs"`
		}
		if err := readJSON(r, &req); err != nil {
			writeJSON(w, http.StatusBadRequest, map[string]string{"error": "Invalid request body"})
			return
		}

		var userID *int
		if uid, ok := r.Context().Value(middleware.ContextKeyUserID).(int); ok {
			userID = &uid
		}

		appVersion := r.Header.Get("X-App-Version")
		osVersion := r.Header.Get("X-OS-Version")
		deviceModel := r.Header.Get("X-Device-Model")

		_, err := pool.Exec(r.Context(),
			`INSERT INTO scan_events (user_id, device_count, threat_count, warning_count, scan_duration_ms, app_version, os_version, device_model)
			 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
			userID, req.DeviceCount, req.ThreatCount, req.WarningCount, req.ScanDurationMs,
			nilIfEmpty(appVersion), nilIfEmpty(osVersion), nilIfEmpty(deviceModel),
		)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]interface{}{
				"success": false, "error": "Failed to record scan event",
			})
			return
		}

		writeJSON(w, http.StatusCreated, map[string]interface{}{
			"success": true, "message": "Scan telemetry recorded",
		})
	}
}

func nilIfEmpty(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
