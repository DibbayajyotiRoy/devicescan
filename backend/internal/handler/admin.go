package handler

import (
	"net/http"
	"strconv"

	"github.com/jackc/pgx/v5/pgxpool"
)

// AdminDashboardHandler returns aggregate statistics for the superadmin.
func AdminDashboardHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		dashboard := map[string]interface{}{}

		// DAU — distinct users with api_events in last 24h
		var dau int
		pool.QueryRow(ctx, `SELECT COUNT(DISTINCT user_id) FROM api_events WHERE created_at > NOW() - INTERVAL '24 hours' AND user_id IS NOT NULL`).Scan(&dau)
		dashboard["dau"] = dau

		// MAU — distinct users in last 30 days
		var mau int
		pool.QueryRow(ctx, `SELECT COUNT(DISTINCT user_id) FROM api_events WHERE created_at > NOW() - INTERVAL '30 days' AND user_id IS NOT NULL`).Scan(&mau)
		dashboard["mau"] = mau

		// Total registered users
		var totalUsers int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM users`).Scan(&totalUsers)
		dashboard["totalUsers"] = totalUsers

		// Scans today
		var scansToday int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM scan_events WHERE created_at > NOW() - INTERVAL '24 hours'`).Scan(&scansToday)
		dashboard["totalScansToday"] = scansToday

		// All-time scans
		var scansAll int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM scan_events`).Scan(&scansAll)
		dashboard["totalScansAllTime"] = scansAll

		// Avg devices per scan
		var avgDevices, avgThreats float64
		pool.QueryRow(ctx, `SELECT COALESCE(AVG(device_count), 0), COALESCE(AVG(threat_count), 0) FROM scan_events`).Scan(&avgDevices, &avgThreats)
		dashboard["avgDevicesPerScan"] = float64(int(avgDevices*10)) / 10
		dashboard["avgThreatsPerScan"] = float64(int(avgThreats*10)) / 10

		// Active signatures
		var activeSigs int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM signatures`).Scan(&activeSigs)
		dashboard["activeSignatures"] = activeSigs

		// Pending community reports
		var pendingReports int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM community_reports WHERE verified = false`).Scan(&pendingReports)
		dashboard["communityReportsPending"] = pendingReports

		// Requests today
		var requestsToday int
		pool.QueryRow(ctx, `SELECT COUNT(*) FROM api_events WHERE created_at > NOW() - INTERVAL '24 hours'`).Scan(&requestsToday)
		dashboard["requestsToday"] = requestsToday

		// Top countries
		rows, err := pool.Query(ctx,
			`SELECT COALESCE(country, 'unknown'), COUNT(*) as cnt FROM api_events
			 WHERE created_at > NOW() - INTERVAL '30 days' AND country IS NOT NULL
			 GROUP BY country ORDER BY cnt DESC LIMIT 10`)
		if err == nil {
			defer rows.Close()
			countries := []map[string]interface{}{}
			for rows.Next() {
				var country string
				var cnt int
				if rows.Scan(&country, &cnt) == nil {
					countries = append(countries, map[string]interface{}{"country": country, "requests": cnt})
				}
			}
			dashboard["topCountries"] = countries
		} else {
			dashboard["topCountries"] = []interface{}{}
		}

		// Threat distribution from signatures matched
		rows2, err := pool.Query(ctx,
			`SELECT category, COUNT(*) as cnt FROM signatures GROUP BY category ORDER BY cnt DESC`)
		if err == nil {
			defer rows2.Close()
			threats := map[string]int{}
			for rows2.Next() {
				var cat string
				var cnt int
				if rows2.Scan(&cat, &cnt) == nil {
					threats[cat] = cnt
				}
			}
			dashboard["threatDistribution"] = threats
		} else {
			dashboard["threatDistribution"] = map[string]int{}
		}

		writeJSON(w, http.StatusOK, dashboard)
	}
}

// AdminUsersHandler returns a paginated list of registered users.
func AdminUsersHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		page, _ := strconv.Atoi(r.URL.Query().Get("page"))
		limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
		if page < 1 {
			page = 1
		}
		if limit < 1 || limit > 100 {
			limit = 50
		}
		offset := (page - 1) * limit

		rows, err := pool.Query(r.Context(),
			`SELECT u.id, u.email, u.name, u.picture_url, u.role, u.created_at, u.last_login_at, u.login_count,
			        COALESCE((SELECT COUNT(*) FROM scan_events WHERE user_id = u.id), 0) as scan_count
			 FROM users u ORDER BY u.last_login_at DESC LIMIT $1 OFFSET $2`, limit, offset)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to fetch users"})
			return
		}
		defer rows.Close()

		users := []map[string]interface{}{}
		for rows.Next() {
			var id, loginCount, scanCount int
			var email, name, role string
			var pictureURL *string
			var createdAt, lastLoginAt interface{}

			if rows.Scan(&id, &email, &name, &pictureURL, &role, &createdAt, &lastLoginAt, &loginCount, &scanCount) == nil {
				users = append(users, map[string]interface{}{
					"id": id, "email": email, "name": name, "pictureUrl": pictureURL,
					"role": role, "createdAt": createdAt, "lastLoginAt": lastLoginAt,
					"loginCount": loginCount, "scanCount": scanCount,
				})
			}
		}

		var total int
		pool.QueryRow(r.Context(), `SELECT COUNT(*) FROM users`).Scan(&total)

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"users": users, "total": total, "page": page, "limit": limit,
		})
	}
}

// AdminUsageHandler returns time-series usage data.
func AdminUsageHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		rangeParam := r.URL.Query().Get("range")
		days := 7
		switch rangeParam {
		case "30d":
			days = 30
		case "90d":
			days = 90
		}

		rows, err := pool.Query(r.Context(),
			`SELECT date_trunc('day', created_at)::date as day,
			        COUNT(*) as requests,
			        COUNT(DISTINCT user_id) as unique_users
			 FROM api_events
			 WHERE created_at > NOW() - MAKE_INTERVAL(days => $1)
			 GROUP BY day ORDER BY day`, days)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to fetch usage data"})
			return
		}
		defer rows.Close()

		series := []map[string]interface{}{}
		for rows.Next() {
			var day string
			var requests, uniqueUsers int
			if rows.Scan(&day, &requests, &uniqueUsers) == nil {
				series = append(series, map[string]interface{}{
					"date": day, "requests": requests, "uniqueUsers": uniqueUsers,
				})
			}
		}

		// Scan events per day
		scanRows, err := pool.Query(r.Context(),
			`SELECT date_trunc('day', created_at)::date as day, COUNT(*) as scans
			 FROM scan_events
			 WHERE created_at > NOW() - MAKE_INTERVAL(days => $1)
			 GROUP BY day ORDER BY day`, days)
		if err == nil {
			defer scanRows.Close()
			scanMap := map[string]int{}
			for scanRows.Next() {
				var day string
				var scans int
				if scanRows.Scan(&day, &scans) == nil {
					scanMap[day] = scans
				}
			}
			for i := range series {
				if scans, ok := scanMap[series[i]["date"].(string)]; ok {
					series[i]["scans"] = scans
				} else {
					series[i]["scans"] = 0
				}
			}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"range": rangeParam, "data": series,
		})
	}
}

// AdminDevicesHandler returns device model and OS distribution.
func AdminDevicesHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()

		// Device models
		modelRows, err := pool.Query(ctx,
			`SELECT COALESCE(device_model, 'unknown'), COUNT(*) as cnt
			 FROM api_events WHERE device_model IS NOT NULL
			 GROUP BY device_model ORDER BY cnt DESC LIMIT 20`)
		models := []map[string]interface{}{}
		if err == nil {
			defer modelRows.Close()
			for modelRows.Next() {
				var model string
				var cnt int
				if modelRows.Scan(&model, &cnt) == nil {
					models = append(models, map[string]interface{}{"model": model, "count": cnt})
				}
			}
		}

		// OS versions
		osRows, err := pool.Query(ctx,
			`SELECT COALESCE(os_version, 'unknown'), COUNT(*) as cnt
			 FROM api_events WHERE os_version IS NOT NULL
			 GROUP BY os_version ORDER BY cnt DESC LIMIT 20`)
		osVersions := []map[string]interface{}{}
		if err == nil {
			defer osRows.Close()
			for osRows.Next() {
				var ver string
				var cnt int
				if osRows.Scan(&ver, &cnt) == nil {
					osVersions = append(osVersions, map[string]interface{}{"version": ver, "count": cnt})
				}
			}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"deviceModels": models, "osVersions": osVersions,
		})
	}
}

// AdminGeoHandler returns geographic distribution of requests.
func AdminGeoHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		rows, err := pool.Query(r.Context(),
			`SELECT COALESCE(country, 'unknown'), COUNT(*) as cnt
			 FROM api_events WHERE country IS NOT NULL
			 GROUP BY country ORDER BY cnt DESC LIMIT 50`)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to fetch geo data"})
			return
		}
		defer rows.Close()

		countries := []map[string]interface{}{}
		for rows.Next() {
			var country string
			var cnt int
			if rows.Scan(&country, &cnt) == nil {
				countries = append(countries, map[string]interface{}{"country": country, "requests": cnt})
			}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{"countries": countries})
	}
}

// AdminEventsHandler returns recent API events.
func AdminEventsHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		page, _ := strconv.Atoi(r.URL.Query().Get("page"))
		limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
		if page < 1 {
			page = 1
		}
		if limit < 1 || limit > 200 {
			limit = 100
		}
		offset := (page - 1) * limit

		rows, err := pool.Query(r.Context(),
			`SELECT id, user_id, endpoint, method, status_code, latency_ms, country, app_version, os_version, device_model, created_at
			 FROM api_events ORDER BY created_at DESC LIMIT $1 OFFSET $2`, limit, offset)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to fetch events"})
			return
		}
		defer rows.Close()

		events := []map[string]interface{}{}
		for rows.Next() {
			var id int64
			var userID *int
			var endpoint, method string
			var statusCode, latencyMs int
			var country, appVersion, osVersion, deviceModel *string
			var createdAt interface{}

			if rows.Scan(&id, &userID, &endpoint, &method, &statusCode, &latencyMs, &country, &appVersion, &osVersion, &deviceModel, &createdAt) == nil {
				events = append(events, map[string]interface{}{
					"id": id, "userId": userID, "endpoint": endpoint, "method": method,
					"statusCode": statusCode, "latencyMs": latencyMs, "country": country,
					"appVersion": appVersion, "osVersion": osVersion, "deviceModel": deviceModel,
					"createdAt": createdAt,
				})
			}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{
			"events": events, "page": page, "limit": limit,
		})
	}
}

// AdminReportsHandler returns community reports for review.
func AdminReportsHandler(pool *pgxpool.Pool) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		rows, err := pool.Query(r.Context(),
			`SELECT id, fingerprint_hash, oui_prefix, user_classification, user_description, reported_at, verified
			 FROM community_reports ORDER BY reported_at DESC LIMIT 100`)
		if err != nil {
			writeJSON(w, http.StatusInternalServerError, map[string]string{"error": "Failed to fetch reports"})
			return
		}
		defer rows.Close()

		reports := []map[string]interface{}{}
		for rows.Next() {
			var id int
			var fpHash, classification string
			var ouiPrefix, description *string
			var reportedAt interface{}
			var verified bool

			if rows.Scan(&id, &fpHash, &ouiPrefix, &classification, &description, &reportedAt, &verified) == nil {
				reports = append(reports, map[string]interface{}{
					"id": id, "fingerprintHash": fpHash, "ouiPrefix": ouiPrefix,
					"userClassification": classification, "userDescription": description,
					"reportedAt": reportedAt, "verified": verified,
				})
			}
		}

		writeJSON(w, http.StatusOK, map[string]interface{}{"reports": reports})
	}
}
