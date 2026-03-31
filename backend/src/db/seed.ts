import { getMigrationClient } from './connection';
import dotenv from 'dotenv';

dotenv.config();

// ─── OUI Threat Intelligence ──────────────────────────────────────────
// Curated list of OUI prefixes commonly found in surveillance/IoT devices
// threat_weight: 0 = safe consumer device, 100 = almost certainly surveillance
const OUI_THREAT_INTEL: Array<[string, string, string, number, boolean]> = [
  // [prefix, vendor, category, threat_weight, is_surveillance_common]

  // ── Espressif (ESP32/ESP8266) — THE most common spy camera chipset ──
  // 300+ prefixes exist; these are the most frequently seen
  ['24:0A:C4', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['AC:67:B2', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['30:AE:A4', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['A4:CF:12', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['5C:CF:7F', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['60:01:94', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['24:6F:28', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['24:62:AB', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['B4:E6:2D', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['CC:50:E3', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['84:0D:8E', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['84:CC:A8', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['84:F3:EB', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['34:AB:95', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['A0:20:A6', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['C8:C9:A3', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['D8:BF:C0', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['30:83:98', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['EC:FA:BC', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['DC:4F:22', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['10:52:1C', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['C4:4F:33', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['3C:61:05', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['3C:71:BF', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['FC:F5:C4', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['7C:DF:A1', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['E8:DB:84', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['08:3A:F2', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['40:F5:20', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['48:3F:DA', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['78:E3:6D', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['80:7D:3A', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['94:B5:55', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['94:B9:7E', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['A4:E5:7C', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['B8:F0:09', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['E0:98:06', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['F0:08:D1', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['F4:CF:A2', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],
  ['68:C6:3A', 'Espressif Inc.', 'IOT_CHIPSET', 65, true],

  // ── Ai-Thinker (ESP32-CAM module maker) ──
  ['B4:8A:0A', 'Ai-Thinker', 'IOT_CHIPSET', 75, true],

  // ── HiSilicon (Huawei subsidiary — IP camera SoC king) ──
  ['84:8A:59', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['00:18:7D', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['00:E0:FC', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['F4:C7:14', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['48:57:02', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['C0:3F:D5', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],
  ['C0:51:7E', 'HiSilicon', 'SURVEILLANCE_CHIPSET', 85, true],

  // ── Tuya Smart (IoT platform — cameras, plugs, sensors) ──
  ['50:8A:06', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['38:A5:C9', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['D4:A6:51', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['70:38:B4', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['84:E3:42', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['1C:90:FF', 'Tuya Smart', 'IOT_PLATFORM', 70, true],
  ['D8:1F:12', 'Tuya Smart', 'IOT_PLATFORM', 70, true],

  // ── Realtek (common WiFi chipset — used in cameras too) ──
  ['7C:1E:52', 'Realtek', 'IOT_CHIPSET', 40, true],
  ['48:02:86', 'Realtek', 'IOT_CHIPSET', 40, true],
  ['00:E0:4C', 'Realtek', 'IOT_CHIPSET', 40, true],
  ['D8:47:32', 'Realtek', 'IOT_CHIPSET', 40, true],
  ['00:13:EF', 'Realtek', 'IOT_CHIPSET', 40, true],
  ['52:54:00', 'Realtek', 'IOT_CHIPSET', 40, true],

  // ── Anyka — explicitly builds camera modules ──
  ['00:42:68', 'Anyka', 'SURVEILLANCE_CHIPSET', 90, true],
  ['58:56:00', 'Anyka', 'SURVEILLANCE_CHIPSET', 90, true],

  // ── Ingenic — battery-powered spy camera SoCs ──
  ['04:B4:FE', 'Ingenic', 'SURVEILLANCE_CHIPSET', 80, true],

  // ── Xiongmai / XMEye — major DVR/NVR/camera maker ──
  ['00:12:12', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:13', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:14', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:15', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:16', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:17', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['00:12:41', 'Xiongmai', 'SURVEILLANCE', 90, true],
  ['E0:3F:49', 'Xiongmai', 'SURVEILLANCE', 90, true],

  // ── Hikvision ──
  ['C0:56:E3', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['54:C4:15', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['44:47:CC', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['BC:AD:28', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['A4:14:37', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['28:57:BE', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['18:68:CB', 'Hikvision', 'SURVEILLANCE', 80, true],
  ['C4:2F:90', 'Hikvision', 'SURVEILLANCE', 80, true],

  // ── Dahua ──
  ['3C:EF:8C', 'Dahua', 'SURVEILLANCE', 80, true],
  ['9C:E3:7D', 'Dahua', 'SURVEILLANCE', 80, true],
  ['40:2C:76', 'Dahua', 'SURVEILLANCE', 80, true],
  ['A0:BD:1D', 'Dahua', 'SURVEILLANCE', 80, true],

  // ── Reolink ──
  ['EC:71:DB', 'Reolink', 'SURVEILLANCE', 70, true],
  ['B4:6D:83', 'Reolink', 'SURVEILLANCE', 70, true],

  // ── TP-Link Tapo (cameras + smart home) ──
  ['5C:A6:E6', 'TP-Link', 'IOT_MIXED', 40, false],
  ['60:32:B1', 'TP-Link', 'IOT_MIXED', 40, false],

  // ── Wyze ──
  ['2C:AA:8E', 'Wyze', 'SURVEILLANCE', 65, true],

  // ── Arlo ──
  ['9C:34:26', 'Arlo', 'SURVEILLANCE', 60, true],

  // ── Ring ──
  ['34:3E:A4', 'Ring', 'SURVEILLANCE', 55, true],

  // ── Safe consumer vendors ──
  ['00:17:88', 'Philips Lighting', 'SMART_HOME', 10, false],
  ['00:1E:06', 'Wibrain', 'CONSUMER', 5, false],
  ['B8:27:EB', 'Raspberry Pi', 'COMPUTE', 50, false],
  ['DC:A6:32', 'Raspberry Pi', 'COMPUTE', 50, false],
  ['E4:5F:01', 'Raspberry Pi', 'COMPUTE', 50, false],
];

// ─── Threat Signatures ────────────────────────────────────────────────
// These are fingerprint patterns that identify specific device types
const THREAT_SIGNATURES = [
  {
    name: 'ESP32-CAM / Generic IoT Camera',
    description: 'ESP32-based camera module commonly hidden in everyday objects (alarm clocks, smoke detectors, chargers). GoAhead embedded web server is the strongest signal.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.85,
    ouiVendors: JSON.stringify(['Espressif Inc.', 'Espressif', 'Ai-Thinker']),
    portPatterns: JSON.stringify({ required: [], suspicious: [80, 81, 88, 8080, 8888, 554, 8554] }),
    bannerPatterns: JSON.stringify(['goahead', 'esp32', 'esp8266', 'ai-thinker']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'GoAhead Embedded Camera',
    description: 'Device running GoAhead embedded web server — extremely common in IP cameras and spy cameras. Almost never found on phones or laptops.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.90,
    ouiVendors: JSON.stringify([]),
    portPatterns: JSON.stringify({ required: [80], suspicious: [554, 8554, 8080] }),
    bannerPatterns: JSON.stringify(['goahead', 'goahead-webs', 'goahead-http']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Mini HTTPD / Embedded Camera',
    description: 'Device running mini_httpd, boa, or thttpd embedded web server. Common in budget IP cameras.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.80,
    ouiVendors: JSON.stringify([]),
    portPatterns: JSON.stringify({ required: [80], suspicious: [554, 8554] }),
    bannerPatterns: JSON.stringify(['mini_httpd', 'mini-httpd', 'boa', 'thttpd', 'uhttpd']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'HiSilicon IP Camera',
    description: 'HiSilicon chipset device — this SoC family powers the majority of commercial and hidden IP cameras worldwide.',
    category: 'CAMERA',
    threatLevel: 'CRITICAL',
    confidence: 0.92,
    ouiVendors: JSON.stringify(['HiSilicon', 'Huawei HiSilicon']),
    portPatterns: JSON.stringify({ required: [], suspicious: [80, 554, 8554, 8000, 8080, 34567, 9527] }),
    bannerPatterns: JSON.stringify(['hisilicon', 'hi3516', 'hi3518', 'hi3519', 'hi3559']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'XMEye / Xiongmai DVR/Camera',
    description: 'Xiongmai/XMEye device — a massive Chinese manufacturer of DVRs, NVRs, and IP cameras. Port 34567 is their proprietary protocol.',
    category: 'CAMERA',
    threatLevel: 'CRITICAL',
    confidence: 0.95,
    ouiVendors: JSON.stringify(['Xiongmai', 'XMEye']),
    portPatterns: JSON.stringify({ required: [34567], suspicious: [80, 554, 8000, 9527] }),
    bannerPatterns: JSON.stringify(['xmeye', 'xiongmai', 'dvr', 'nvr']),
    protocolResponses: JSON.stringify({ xmeye_udp: true }),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Tuya Smart Camera / IoT Device',
    description: 'Tuya platform device. Could be a camera, smart plug, sensor, or any IoT device using the Tuya/SmartLife ecosystem.',
    category: 'IOT',
    threatLevel: 'MEDIUM',
    confidence: 0.70,
    ouiVendors: JSON.stringify(['Tuya Smart', 'Tuya Inc.']),
    portPatterns: JSON.stringify({ required: [], suspicious: [6666, 6667, 6668, 80, 443] }),
    bannerPatterns: JSON.stringify(['tuya', 'smart life', 'smartlife']),
    protocolResponses: JSON.stringify({ tuya_udp: true }),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Dahua IP Camera / DVR',
    description: 'Dahua Technology device — major surveillance equipment manufacturer. Port 37777 is their proprietary protocol.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.93,
    ouiVendors: JSON.stringify(['Dahua', 'Dahua Technology']),
    portPatterns: JSON.stringify({ required: [], suspicious: [37777, 80, 554, 8080, 443] }),
    bannerPatterns: JSON.stringify(['dahua', 'dh-ipc', 'imou']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Hikvision Camera / DVR',
    description: 'Hikvision device — the world\'s largest surveillance camera manufacturer. Port 8000 is their SDK protocol.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.93,
    ouiVendors: JSON.stringify(['Hikvision', 'Hangzhou Hikvision']),
    portPatterns: JSON.stringify({ required: [], suspicious: [8000, 80, 554, 443, 8200] }),
    bannerPatterns: JSON.stringify(['hikvision', 'isapi', 'hikconnect', 'hik-connect']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'ONVIF Camera',
    description: 'Device supporting ONVIF protocol — standard for IP-based security cameras.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.88,
    ouiVendors: JSON.stringify([]),
    portPatterns: JSON.stringify({ required: [], suspicious: [80, 554, 8899, 8080] }),
    bannerPatterns: JSON.stringify(['onvif']),
    protocolResponses: JSON.stringify({ onvif: true }),
    mdnsServices: JSON.stringify(['_onvif._tcp']),
  },
  {
    name: 'RTSP Streaming Device',
    description: 'Device with RTSP port open — used exclusively for video streaming. Strong indicator of a camera.',
    category: 'CAMERA',
    threatLevel: 'HIGH',
    confidence: 0.85,
    ouiVendors: JSON.stringify([]),
    portPatterns: JSON.stringify({ required: [554], suspicious: [8554, 80] }),
    bannerPatterns: JSON.stringify([]),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify(['_rtsp._tcp']),
  },
  {
    name: 'Anyka Camera Module',
    description: 'Anyka chipset — specifically designed for IP camera hardware. Very high probability of being a camera.',
    category: 'CAMERA',
    threatLevel: 'CRITICAL',
    confidence: 0.93,
    ouiVendors: JSON.stringify(['Anyka']),
    portPatterns: JSON.stringify({ required: [], suspicious: [80, 554, 6000, 8080] }),
    bannerPatterns: JSON.stringify(['anyka', 'ak39']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Ingenic Camera SoC',
    description: 'Ingenic chipset — used in battery-powered and compact spy cameras. Low-power design optimized for covert use.',
    category: 'CAMERA',
    threatLevel: 'CRITICAL',
    confidence: 0.88,
    ouiVendors: JSON.stringify(['Ingenic']),
    portPatterns: JSON.stringify({ required: [], suspicious: [80, 554, 8080, 9527] }),
    bannerPatterns: JSON.stringify(['ingenic', 't31', 't40']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Unknown IoT Device (ESP-based, no services)',
    description: 'An Espressif-based device with no open ports or identifiable services. In a domestic setting, this is suspicious — most legitimate smart home devices advertise services.',
    category: 'IOT',
    threatLevel: 'MEDIUM',
    confidence: 0.60,
    ouiVendors: JSON.stringify(['Espressif Inc.', 'Espressif']),
    portPatterns: JSON.stringify({ required: [], suspicious: [] }),
    bannerPatterns: JSON.stringify([]),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
  {
    name: 'Raspberry Pi (potential surveillance)',
    description: 'Raspberry Pi detected on network. Could be a legitimate device or repurposed as a surveillance tool. Context-dependent.',
    category: 'COMPUTE',
    threatLevel: 'LOW',
    confidence: 0.40,
    ouiVendors: JSON.stringify(['Raspberry Pi', 'Raspberry Pi Foundation', 'Raspberry Pi Trading']),
    portPatterns: JSON.stringify({ required: [], suspicious: [22, 80, 554, 8080] }),
    bannerPatterns: JSON.stringify(['raspbian', 'raspberry', 'motion', 'motioneye']),
    protocolResponses: JSON.stringify({}),
    mdnsServices: JSON.stringify([]),
  },
];

async function seed() {
  console.log('Seeding database...');
  const sql = getMigrationClient();

  try {
    // Seed OUI threat intel
    console.log(`Inserting ${OUI_THREAT_INTEL.length} OUI threat intelligence entries...`);
    for (const [prefix, vendor, category, threatWeight, isSurveillance] of OUI_THREAT_INTEL) {
      await sql`
        INSERT INTO oui_database (prefix, vendor, category, threat_weight, is_surveillance_common)
        VALUES (${prefix}, ${vendor}, ${category}, ${threatWeight}, ${isSurveillance})
        ON CONFLICT (prefix) DO UPDATE SET
          vendor = EXCLUDED.vendor,
          category = EXCLUDED.category,
          threat_weight = EXCLUDED.threat_weight,
          is_surveillance_common = EXCLUDED.is_surveillance_common
      `;
    }
    console.log('✅ OUI threat intel seeded');

    // Seed threat signatures
    console.log(`Inserting ${THREAT_SIGNATURES.length} threat signatures...`);
    for (const sig of THREAT_SIGNATURES) {
      await sql`
        INSERT INTO signatures (
          name, description, category, threat_level, confidence,
          oui_vendors, port_patterns, banner_patterns, protocol_responses, mdns_services,
          source
        ) VALUES (
          ${sig.name}, ${sig.description}, ${sig.category}, ${sig.threatLevel}, ${sig.confidence},
          ${sig.ouiVendors}, ${sig.portPatterns}, ${sig.bannerPatterns}, ${sig.protocolResponses}, ${sig.mdnsServices},
          'curated'
        )
      `;
    }
    console.log('✅ Threat signatures seeded');

    // Count totals
    const ouiCount = await sql`SELECT COUNT(*) as count FROM oui_database`;
    const sigCount = await sql`SELECT COUNT(*) as count FROM signatures`;
    console.log(`\n📊 Database summary:`);
    console.log(`   OUI entries: ${ouiCount[0].count}`);
    console.log(`   Signatures:  ${sigCount[0].count}`);

  } catch (error) {
    console.error('❌ Seed failed:', error);
    throw error;
  } finally {
    await sql.end();
  }
}

seed().catch((e) => {
  console.error(e);
  process.exit(1);
});
