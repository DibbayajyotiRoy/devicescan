package handler

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/jackc/pgx/v5/pgxpool"
)

func SignaturesLatestHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		sinceVersion := 0
		if sv := r.URL.Query().Get("sinceVersion"); sv != "" {
			sinceVersion, _ = strconv.Atoi(sv)
		}

		query := `SELECT id, name, description, category, threat_level, confidence,
		           oui_vendors, port_patterns, banner_patterns, protocol_responses, mdns_services,
		           source, report_count, version, updated_at
		          FROM signatures`
		args := []interface{}{}
		if sinceVersion > 0 {
			query += ` WHERE version > $1`
			args = append(args, sinceVersion)
		}

		rows, err := pool.Query(r.Context(), query, args...)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]interface{}{
				"success": false, "error": "Failed to fetch signatures",
			})
			return
		}
		defer rows.Close()

		type sigResponse struct {
			ID                int         `json:"id"`
			Name              string      `json:"name"`
			Description       *string     `json:"description"`
			Category          string      `json:"category"`
			ThreatLevel       string      `json:"threatLevel"`
			Confidence        float64     `json:"confidence"`
			OuiVendors        interface{} `json:"ouiVendors"`
			PortPatterns      interface{} `json:"portPatterns"`
			BannerPatterns    interface{} `json:"bannerPatterns"`
			ProtocolResponses interface{} `json:"protocolResponses"`
			MdnsServices      interface{} `json:"mdnsServices"`
			Source            string      `json:"source"`
			ReportCount       int         `json:"reportCount"`
			Version           int         `json:"version"`
			UpdatedAt         interface{} `json:"updatedAt"`
		}

		var sigs []sigResponse
		maxVersion := 0

		for rows.Next() {
			var (
				id                                                 int
				name, category, threatLevel, source                string
				description                                        *string
				confidence                                         float64
				ouiVendors, portPatterns, bannerPatterns            *string
				protocolResponses, mdnsServices                    *string
				reportCount, version                               int
				updatedAt                                          interface{}
			)

			if err := rows.Scan(&id, &name, &description, &category, &threatLevel, &confidence,
				&ouiVendors, &portPatterns, &bannerPatterns, &protocolResponses, &mdnsServices,
				&source, &reportCount, &version, &updatedAt); err != nil {
				continue
			}

			if version > maxVersion {
				maxVersion = version
			}

			sig := sigResponse{
				ID: id, Name: name, Description: description, Category: category,
				ThreatLevel: threatLevel, Confidence: confidence, Source: source,
				ReportCount: reportCount, Version: version, UpdatedAt: updatedAt,
			}

			sig.OuiVendors = parseJSONOrDefault(ouiVendors, []interface{}{})
			sig.PortPatterns = parseJSONOrDefault(portPatterns, map[string]interface{}{})
			sig.BannerPatterns = parseJSONOrDefault(bannerPatterns, []interface{}{})
			sig.ProtocolResponses = parseJSONOrDefault(protocolResponses, map[string]interface{}{})
			sig.MdnsServices = parseJSONOrDefault(mdnsServices, []interface{}{})

			sigs = append(sigs, sig)
		}

		if sigs == nil {
			sigs = []sigResponse{}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"success":        true,
			"currentVersion": maxVersion,
			"count":          len(sigs),
			"signatures":     sigs,
		})
	}
}

func parseJSONOrDefault(s *string, fallback interface{}) interface{} {
	if s == nil {
		return fallback
	}
	var v interface{}
	if err := json.Unmarshal([]byte(*s), &v); err != nil {
		return fallback
	}
	return v
}
