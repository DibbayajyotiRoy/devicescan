import { pgTable, text, integer, boolean, real, timestamp, serial, index, uniqueIndex } from 'drizzle-orm/pg-core';

// ─── OUI Database ──────────────────────────────────────────────────
export const ouiDatabase = pgTable('oui_database', {
  prefix: text('prefix').primaryKey(),                    // "24:0A:C4"
  vendor: text('vendor').notNull(),                       // "Espressif Inc."
  category: text('category').default('UNKNOWN'),          // IOT_CHIPSET, CONSUMER, NETWORKING, SURVEILLANCE
  threatWeight: integer('threat_weight').default(0),      // 0-100
  aliases: text('aliases'),                               // JSON: '["Ai-Thinker","NodeMCU"]'
  isSurveillanceCommon: boolean('is_surveillance_common').default(false),
}, (table) => [
  index('oui_vendor_idx').on(table.vendor),
  index('oui_category_idx').on(table.category),
]);

// ─── Threat Signatures ─────────────────────────────────────────────
export const signatures = pgTable('signatures', {
  id: serial('id').primaryKey(),
  name: text('name').notNull(),                           // "Generic ESP32 Camera"
  description: text('description'),
  category: text('category').notNull(),                   // CAMERA, TRACKER, RECORDER, AUDIO_BUG, IOT
  threatLevel: text('threat_level').notNull(),            // LOW, MEDIUM, HIGH, CRITICAL
  confidence: real('confidence').notNull(),               // 0.0 - 1.0

  // Matching rules (JSON strings)
  ouiVendors: text('oui_vendors'),                        // '["Espressif","HiSilicon"]'
  portPatterns: text('port_patterns'),                    // '{"required":[80],"suspicious":[554,34567]}'
  bannerPatterns: text('banner_patterns'),                // '["goahead","boa","mini_httpd"]'
  protocolResponses: text('protocol_responses'),          // '{"tuya_udp":true}'
  mdnsServices: text('mdns_services'),                    // '["_camera._tcp"]'

  source: text('source').default('curated'),              // "curated" | "community"
  reportCount: integer('report_count').default(0),
  version: integer('version').default(1),
  updatedAt: timestamp('updated_at').defaultNow(),
  createdAt: timestamp('created_at').defaultNow(),
}, (table) => [
  index('sig_category_idx').on(table.category),
  index('sig_threat_idx').on(table.threatLevel),
]);

// ─── Community Reports ──────────────────────────────────────────────
export const communityReports = pgTable('community_reports', {
  id: serial('id').primaryKey(),
  fingerprintHash: text('fingerprint_hash').notNull(),     // SHA-256 of normalized fingerprint
  ouiPrefix: text('oui_prefix'),
  openPorts: text('open_ports'),                           // JSON array
  httpBanner: text('http_banner'),
  userClassification: text('user_classification').notNull(), // "spy_camera", "smart_plug", etc.
  userDescription: text('user_description'),
  reportedAt: timestamp('reported_at').defaultNow(),
  ipHash: text('ip_hash'),                                 // Hashed for dedup
  verified: boolean('verified').default(false),
}, (table) => [
  index('report_fp_idx').on(table.fingerprintHash),
  index('report_class_idx').on(table.userClassification),
]);

// ─── Known Devices (verified fingerprints) ──────────────────────────
export const knownDevices = pgTable('known_devices', {
  id: serial('id').primaryKey(),
  fingerprintHash: text('fingerprint_hash').notNull(),
  deviceType: text('device_type').notNull(),
  brand: text('brand'),
  model: text('model'),
  isThreat: boolean('is_threat').default(false),
  threatCategory: text('threat_category'),                 // "hidden_camera", "audio_bug", "gps_tracker"
  confidence: real('confidence').notNull(),
  reportCount: integer('report_count').default(0),
  lastSeen: timestamp('last_seen').defaultNow(),
}, (table) => [
  uniqueIndex('known_fp_idx').on(table.fingerprintHash),
]);
