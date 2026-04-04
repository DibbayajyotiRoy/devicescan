package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"strconv"

	"devicelens-backend/internal/db"
	"devicelens-backend/internal/handler"
	"devicelens-backend/internal/middleware"

	"github.com/go-chi/chi/v5"
)

func main() {
	ctx := context.Background()

	// ─── Database ───────────────────────────────────────────────
	pool, err := db.NewPool(ctx)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer pool.Close()

	if err := db.RunMigrations(ctx, pool); err != nil {
		log.Fatalf("Migration failed: %v", err)
	}

	// ─── Rate limiter ───────────────────────────────────────────
	rps := 100.0
	burst := 200
	if v := os.Getenv("RATE_LIMIT_RPS"); v != "" {
		rps, _ = strconv.ParseFloat(v, 64)
	}
	if v := os.Getenv("RATE_LIMIT_BURST"); v != "" {
		burst, _ = strconv.Atoi(v)
	}
	limiter := middleware.NewRateLimiter(rps, burst)

	// ─── Analytics ──────────────────────────────────────────────
	analytics := middleware.NewAnalytics(pool)

	// ─── Router ─────────────────────────────────────────────────
	r := chi.NewRouter()

	// Global middleware
	r.Use(middleware.Recover)
	r.Use(middleware.CORS)
	r.Use(limiter.Handler)
	r.Use(middleware.OptionalAuth)
	r.Use(analytics.Handler)

	// ─── Public API (existing endpoints — same contract) ────────
	r.Get("/api/v1/health", handler.HealthHandler(pool))

	r.Post("/api/v1/identify", handler.IdentifyHandler(pool))
	r.Post("/api/v1/identify/batch", handler.IdentifyBatchHandler(pool))

	r.Post("/api/v1/report", handler.ReportHandler(pool))

	r.Get("/api/v1/signatures/latest", handler.SignaturesLatestHandler(pool))

	r.Get("/api/v1/oui/{prefix}", handler.OuiLookupHandler(pool))
	r.Get("/api/v1/oui/search/{vendor}", handler.OuiSearchHandler(pool))

	// ─── Auth endpoints ─────────────────────────────────────────
	r.Post("/api/v1/auth/google", handler.GoogleAuthHandler(pool))
	r.Post("/api/v1/auth/refresh", handler.RefreshTokenHandler(pool))
	r.With(middleware.RequireAuth).Get("/api/v1/auth/me", handler.MeHandler(pool))

	// ─── Telemetry (optional auth) ──────────────────────────────
	r.Post("/api/v1/telemetry/scan", handler.TelemetryHandler(pool))

	// ─── Superadmin login ───────────────────────────────────────
	r.Post("/api/v1/admin/login", handler.AdminLoginHandler(pool))

	// ─── Superadmin API (requires superadmin JWT) ───────────────
	r.Route("/api/v1/admin", func(r chi.Router) {
		r.Use(middleware.RequireAuth)
		r.Use(middleware.RequireSuperadmin)

		r.Get("/dashboard", handler.AdminDashboardHandler(pool))
		r.Get("/users", handler.AdminUsersHandler(pool))
		r.Get("/analytics/usage", handler.AdminUsageHandler(pool))
		r.Get("/analytics/devices", handler.AdminDevicesHandler(pool))
		r.Get("/analytics/geo", handler.AdminGeoHandler(pool))
		r.Get("/events", handler.AdminEventsHandler(pool))
		r.Get("/reports", handler.AdminReportsHandler(pool))
	})

	// ─── Admin web panel (embedded static files) ────────────────
	adminFS := http.Dir("web/admin")
	r.Handle("/admin/*", http.StripPrefix("/admin/", http.FileServer(adminFS)))
	r.Get("/admin", http.RedirectHandler("/admin/", http.StatusMovedPermanently).ServeHTTP)

	// ─── 404 Handler ────────────────────────────────────────────
	r.NotFound(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusNotFound)
		w.Write([]byte(`{"error":"Not found","availableEndpoints":["POST /api/v1/identify","POST /api/v1/identify/batch","POST /api/v1/report","GET /api/v1/signatures/latest","GET /api/v1/oui/:prefix","GET /api/v1/oui/search/:vendor","GET /api/v1/health","POST /api/v1/auth/google","POST /api/v1/auth/refresh","GET /api/v1/auth/me","POST /api/v1/telemetry/scan","POST /api/v1/admin/login","GET /api/v1/admin/dashboard"]}`))
	})

	// ─── Start ──────────────────────────────────────────────────
	port := os.Getenv("PORT")
	if port == "" {
		port = "3000"
	}

	fmt.Printf(`
╔══════════════════════════════════════════════════╗
║         DeviceLens Intelligence API              ║
║         Runtime: Go                              ║
║         Port: %s                                 ║
╚══════════════════════════════════════════════════╝

Public API:
  POST /api/v1/identify          Identify a device
  POST /api/v1/identify/batch    Identify multiple devices
  POST /api/v1/report            Submit community report
  GET  /api/v1/signatures/latest Get latest signatures
  GET  /api/v1/oui/:prefix       OUI lookup
  GET  /api/v1/health            Health check

Auth:
  POST /api/v1/auth/google       Google OAuth login
  POST /api/v1/auth/refresh      Refresh access token
  GET  /api/v1/auth/me           Current user profile

Telemetry:
  POST /api/v1/telemetry/scan    Report scan telemetry

Admin:
  POST /api/v1/admin/login       Superadmin login
  GET  /api/v1/admin/dashboard   Dashboard stats
  GET  /api/v1/admin/users       User list
  GET  /admin/                   Web panel

`, port)

	log.Fatal(http.ListenAndServe(":"+port, r))
}
