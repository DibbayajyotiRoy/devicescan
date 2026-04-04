package handler

import (
	"net/http"

	"github.com/jackc/pgx/v5/pgxpool"
)

func HealthHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		type dbStats struct {
			Connected        bool `json:"connected"`
			OuiEntries       int  `json:"ouiEntries"`
			Signatures       int  `json:"signatures"`
			CommunityReports int  `json:"communityReports"`
			KnownDevices     int  `json:"knownDevices"`
		}

		var oui, sig, rep, known int

		err := pool.QueryRow(r.Context(), `SELECT count(*) FROM oui_database`).Scan(&oui)
		if err != nil {
			writeJSON(w, http.StatusServiceUnavailable, map[string]string{
				"status": "unhealthy", "error": "Database connection failed",
			})
			return
		}

		pool.QueryRow(r.Context(), `SELECT count(*) FROM signatures`).Scan(&sig)
		pool.QueryRow(r.Context(), `SELECT count(*) FROM community_reports`).Scan(&rep)
		pool.QueryRow(r.Context(), `SELECT count(*) FROM known_devices`).Scan(&known)

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"status":  "healthy",
			"version": "2.0.0",
			"runtime": "go",
			"database": dbStats{
				Connected:        true,
				OuiEntries:       oui,
				Signatures:       sig,
				CommunityReports: rep,
				KnownDevices:     known,
			},
		})
	}
}
