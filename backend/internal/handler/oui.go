package handler

import (
	"encoding/json"
	"net/http"
	"regexp"
	"strings"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

var ouiRegex = regexp.MustCompile(`^[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}$`)

func OuiLookupHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		raw := chi.URLParam(r, "prefix")
		prefix := strings.ToUpper(strings.ReplaceAll(strings.ReplaceAll(raw, "-", ":"), ".", ":"))
		if len(prefix) > 8 {
			prefix = prefix[:8]
		}

		if !ouiRegex.MatchString(prefix) {
			writeJSON(w, http.StatusBadRequest, map[string]interface{}{
				"success": false,
				"error":   "Invalid OUI prefix format. Expected XX:XX:XX (e.g., 24:0A:C4)",
			})
			return
		}

		var vendor, category string
		var threatWeight int
		var isSurveillanceCommon bool
		var aliases *string

		err := pool.QueryRow(r.Context(),
			`SELECT vendor, category, threat_weight, is_surveillance_common, aliases
			 FROM oui_database WHERE prefix = $1 LIMIT 1`, prefix,
		).Scan(&vendor, &category, &threatWeight, &isSurveillanceCommon, &aliases)

		if err != nil {
			writeJSON(w, http.StatusOK, map[string]interface{}{
				"success": true, "found": false, "prefix": prefix, "vendor": nil,
			})
			return
		}

		var aliasArr interface{} = []interface{}{}
		if aliases != nil {
			json.Unmarshal([]byte(*aliases), &aliasArr)
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"success": true,
			"found":   true,
			"prefix":  prefix,
			"vendor": map[string]interface{}{
				"name":                 vendor,
				"category":             category,
				"threatWeight":         threatWeight,
				"isSurveillanceCommon": isSurveillanceCommon,
				"aliases":              aliasArr,
			},
		})
	}
}

func OuiSearchHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		vendorQuery := chi.URLParam(r, "vendor")
		if vendorQuery == "" {
			writeJSON(w, http.StatusBadRequest, map[string]string{"error": "vendor parameter required"})
			return
		}

		rows, err := pool.Query(r.Context(),
			`SELECT prefix, vendor, category, threat_weight, is_surveillance_common
			 FROM oui_database WHERE vendor ILIKE $1 LIMIT 50`, "%"+vendorQuery+"%")
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]interface{}{
				"success": false, "error": "Failed to search OUI database",
			})
			return
		}
		defer rows.Close()

		type entry struct {
			Prefix               string `json:"prefix"`
			Vendor               string `json:"vendor"`
			Category             string `json:"category"`
			ThreatWeight         int    `json:"threatWeight"`
			IsSurveillanceCommon bool   `json:"isSurveillanceCommon"`
		}

		var entries []entry
		for rows.Next() {
			var e entry
			if rows.Scan(&e.Prefix, &e.Vendor, &e.Category, &e.ThreatWeight, &e.IsSurveillanceCommon) == nil {
				entries = append(entries, e)
			}
		}
		if entries == nil {
			entries = []entry{}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"success": true, "count": len(entries), "entries": entries,
		})
	}
}
