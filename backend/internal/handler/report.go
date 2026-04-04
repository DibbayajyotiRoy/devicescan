package handler

import (
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"net/http"

	"devicelens-backend/internal/engine"
	"devicelens-backend/internal/models"
	"github.com/jackc/pgx/v5/pgxpool"
)

var validClassifications = map[string]bool{
	"spy_camera": true, "security_camera": true, "smart_plug": true,
	"smart_speaker": true, "phone": true, "tablet": true, "laptop": true,
	"desktop": true, "printer": true, "router": true, "smart_tv": true,
	"gaming_console": true, "iot_sensor": true, "smart_light": true,
	"thermostat": true, "doorbell": true, "audio_bug": true,
	"gps_tracker": true, "other_safe": true, "other_suspicious": true,
}

func ReportHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			Fingerprint        models.DeviceFingerprint `json:"fingerprint"`
			UserClassification string                   `json:"userClassification"`
			UserDescription    *string                  `json:"userDescription"`
		}
		if err := readJSON(r, &req); err != nil {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "Invalid request body", "details": err.Error(),
			})
			return
		}

		if !validClassifications[req.UserClassification] {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "Invalid userClassification",
			})
			return
		}

		fpHash := engine.HashFingerprint(req.Fingerprint)

		// Hash IP for dedup (never store raw)
		ipRaw := r.RemoteAddr
		if fwd := r.Header.Get("X-Forwarded-For"); fwd != "" {
			ipRaw = fwd
		}
		ipHash := fmt.Sprintf("%x", sha256.Sum256([]byte(ipRaw)))[:16]

		portsJSON, _ := json.Marshal(req.Fingerprint.OpenPorts)

		// Insert community report
		_, err := pool.Exec(r.Context(),
			`INSERT INTO community_reports (fingerprint_hash, oui_prefix, open_ports, http_banner, user_classification, user_description, ip_hash)
			 VALUES ($1, $2, $3, $4, $5, $6, $7)`,
			fpHash, req.Fingerprint.OuiPrefix, string(portsJSON),
			req.Fingerprint.HttpBanner, req.UserClassification, req.UserDescription, ipHash,
		)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]interface{}{
				"success": false, "error": "Failed to submit report",
			})
			return
		}

		// Check if we have enough reports to auto-create known device
		rows, err := pool.Query(r.Context(),
			`SELECT user_classification FROM community_reports WHERE fingerprint_hash = $1`, fpHash)
		if err == nil {
			defer rows.Close()
			counts := map[string]int{}
			total := 0
			for rows.Next() {
				var cls string
				if rows.Scan(&cls) == nil {
					counts[cls]++
					total++
				}
			}

			// Find top classification
			topCls := ""
			topCount := 0
			for cls, count := range counts {
				if count > topCount {
					topCls = cls
					topCount = count
				}
			}

			if topCount >= 5 {
				isThreat := topCls == "spy_camera" || topCls == "audio_bug" || topCls == "gps_tracker" || topCls == "other_suspicious"
				confidence := float64(topCount) / float64(total)
				confidence = float64(int(confidence*100)) / 100

				var threatCat *string
				if isThreat {
					threatCat = &topCls
				}

				pool.Exec(r.Context(),
					`INSERT INTO known_devices (fingerprint_hash, device_type, is_threat, threat_category, confidence, report_count)
					 VALUES ($1, $2, $3, $4, $5, $6)
					 ON CONFLICT (fingerprint_hash) DO UPDATE SET
					   device_type = $2, is_threat = $3, threat_category = $4,
					   confidence = $5, report_count = $6, last_seen = NOW()`,
					fpHash, topCls, isThreat, threatCat, confidence, total,
				)
			}

			writeJSON(w, http.StatusCreated, map[string]interface{}{
				"success":               true,
				"reportId":              fpHash[:12],
				"message":               "Thank you for your report. This helps protect other users.",
				"totalReportsForDevice": total,
			})
			return
		}

		writeJSON(w, http.StatusCreated, map[string]interface{}{
			"success":  true,
			"reportId": fpHash[:12],
			"message":  "Thank you for your report. This helps protect other users.",
		})
	}
}
