import { getMigrationClient } from './connection';
import dotenv from 'dotenv';

dotenv.config();

const SCHEMA_SQL = `
-- OUI Database: IEEE vendor prefixes + threat intelligence
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

-- Threat Signatures: patterns that identify spy cameras & surveillance devices
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

-- Community Reports: crowdsourced device identifications
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

-- Known Devices: verified fingerprint → device mappings
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
`;

async function migrate() {
  console.log('Running database migrations...');
  const sql = getMigrationClient();
  
  try {
    await sql.unsafe(SCHEMA_SQL);
    console.log('✅ Database schema created successfully');
  } catch (error) {
    console.error('❌ Migration failed:', error);
    throw error;
  } finally {
    await sql.end();
  }
}

migrate().catch((e) => {
  console.error(e);
  process.exit(1);
});
