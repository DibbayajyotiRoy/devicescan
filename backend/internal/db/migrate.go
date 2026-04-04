package db

import (
	"context"
	"fmt"
	"log"

	"github.com/jackc/pgx/v5/pgxpool"
)

const migrationSQL = `
-- ═══════════════════════════════════════════════════════════
-- Existing tables (IF NOT EXISTS = safe to re-run)
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS oui_database (
    prefix TEXT PRIMARY KEY,
    vendor TEXT NOT NULL,
    category TEXT DEFAULT 'UNKNOWN',
    threat_weight INTEGER DEFAULT 0,
    aliases TEXT,
    is_surveillance_common BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS oui_vendor_idx ON oui_database(vendor);
CREATE INDEX IF NOT EXISTS oui_category_idx ON oui_database(category);

CREATE TABLE IF NOT EXISTS signatures (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    threat_level TEXT NOT NULL,
    confidence REAL NOT NULL,
    oui_vendors TEXT,
    port_patterns TEXT,
    banner_patterns TEXT,
    protocol_responses TEXT,
    mdns_services TEXT,
    source TEXT DEFAULT 'curated',
    report_count INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    updated_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS sig_category_idx ON signatures(category);
CREATE INDEX IF NOT EXISTS sig_threat_idx ON signatures(threat_level);

CREATE TABLE IF NOT EXISTS community_reports (
    id SERIAL PRIMARY KEY,
    fingerprint_hash TEXT NOT NULL,
    oui_prefix TEXT,
    open_ports TEXT,
    http_banner TEXT,
    user_classification TEXT NOT NULL,
    user_description TEXT,
    reported_at TIMESTAMP DEFAULT NOW(),
    ip_hash TEXT,
    verified BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS report_fp_idx ON community_reports(fingerprint_hash);
CREATE INDEX IF NOT EXISTS report_class_idx ON community_reports(user_classification);

CREATE TABLE IF NOT EXISTS known_devices (
    id SERIAL PRIMARY KEY,
    fingerprint_hash TEXT NOT NULL UNIQUE,
    device_type TEXT NOT NULL,
    brand TEXT,
    model TEXT,
    is_threat BOOLEAN DEFAULT FALSE,
    threat_category TEXT,
    confidence REAL NOT NULL,
    report_count INTEGER DEFAULT 0,
    last_seen TIMESTAMP DEFAULT NOW()
);

-- ═══════════════════════════════════════════════════════════
-- New: Auth tables
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    google_id TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    picture_url TEXT,
    role TEXT NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP DEFAULT NOW(),
    login_count INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    user_agent TEXT,
    ip_country TEXT
);
CREATE INDEX IF NOT EXISTS sessions_user_idx ON sessions(user_id);
CREATE INDEX IF NOT EXISTS sessions_expires_idx ON sessions(expires_at);

-- ═══════════════════════════════════════════════════════════
-- New: Analytics tables
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS api_events (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    endpoint TEXT NOT NULL,
    method TEXT NOT NULL,
    status_code INTEGER NOT NULL,
    latency_ms INTEGER NOT NULL,
    country TEXT,
    app_version TEXT,
    os_version TEXT,
    device_model TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS api_events_created_idx ON api_events(created_at);
CREATE INDEX IF NOT EXISTS api_events_user_idx ON api_events(user_id);
CREATE INDEX IF NOT EXISTS api_events_endpoint_idx ON api_events(endpoint);

CREATE TABLE IF NOT EXISTS scan_events (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    device_count INTEGER NOT NULL,
    threat_count INTEGER NOT NULL DEFAULT 0,
    warning_count INTEGER NOT NULL DEFAULT 0,
    scan_duration_ms INTEGER,
    app_version TEXT,
    os_version TEXT,
    device_model TEXT,
    country TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS scan_events_created_idx ON scan_events(created_at);
CREATE INDEX IF NOT EXISTS scan_events_user_idx ON scan_events(user_id);
`

func RunMigrations(ctx context.Context, pool *pgxpool.Pool) error {
	log.Println("Running database migrations...")
	_, err := pool.Exec(ctx, migrationSQL)
	if err != nil {
		return fmt.Errorf("migration failed: %w", err)
	}
	log.Println("✅ Database migrations complete")
	return nil
}
