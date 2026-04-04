package handler

import (
	"net/http"

	"devicelens-backend/internal/engine"
	"devicelens-backend/internal/models"
	"github.com/jackc/pgx/v5/pgxpool"
)

func IdentifyHandler(pool *pgxpool.Pool) http.HandlerFunc {
	matcher := engine.NewMatcher(pool)

	return func(w http.ResponseWriter, r *http.Request) {
		var fp models.DeviceFingerprint
		if err := readJSON(r, &fp); err != nil {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "Invalid request body", "details": err.Error(),
			})
			return
		}

		if len(fp.OuiPrefix) < 6 || len(fp.OuiPrefix) > 17 {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "ouiPrefix must be 6-17 characters",
			})
			return
		}

		result, err := matcher.MatchFingerprint(r.Context(), fp)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]interface{}{
				"success": false, "error": "Internal server error during device identification",
			})
			return
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"success":          true,
			"match":            result.Match,
			"communityReports": result.CommunityReports,
			"vendor":           result.Vendor,
		})
	}
}

func IdentifyBatchHandler(pool *pgxpool.Pool) http.HandlerFunc {
	matcher := engine.NewMatcher(pool)

	return func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			Devices []models.DeviceFingerprint `json:"devices"`
		}
		if err := readJSON(r, &req); err != nil {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "Invalid request body", "details": err.Error(),
			})
			return
		}

		if len(req.Devices) == 0 || len(req.Devices) > 50 {
			writeJSON(w, http.StatusBadRequest, map[string]string{
				"error": "devices array must contain 1-50 items",
			})
			return
		}

		results := make([]*models.MatchResult, len(req.Devices))
		for i, fp := range req.Devices {
			result, err := matcher.MatchFingerprint(r.Context(), fp)
			if err != nil {
				results[i] = &models.MatchResult{
					CommunityReports: models.CommunityData{TotalReports: 0, ClassifiedAs: map[string]int{}},
				}
				continue
			}
			results[i] = result
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"success": true,
			"results": results,
		})
	}
}
