import { db } from '../db/connection';
import { signatures, ouiDatabase, knownDevices, communityReports } from '../db/schema';
import { eq, sql, and, ilike } from 'drizzle-orm';
import crypto from 'crypto';

// ─── Types ─────────────────────────────────────────────────────────────

export interface DeviceFingerprint {
  ouiPrefix: string;
  openPorts: number[];
  httpBanner: string | null;
  ssdpResponse: string | null;
  mDnsServices: string[];
  bleManufacturerData: string | null;
  pageTitle: string | null;
  respondsTuya: boolean;
  respondsXmeye: boolean;
}

export interface MatchResult {
  match: {
    deviceType: string;
    category: string;
    threatLevel: string;
    confidence: number;
    description: string;
    knownModel: string | null;
    recommendation: string;
    signatureName: string;
  } | null;
  communityReports: {
    totalReports: number;
    classifiedAs: Record<string, number>;
  };
  vendor: {
    name: string;
    category: string;
    threatWeight: number;
    isSurveillanceCommon: boolean;
  } | null;
}

// ─── Fingerprint Hashing ──────────────────────────────────────────────

export function hashFingerprint(fp: DeviceFingerprint): string {
  const normalized = JSON.stringify({
    oui: fp.ouiPrefix.toUpperCase().slice(0, 8),
    ports: [...fp.openPorts].sort(),
    banner: (fp.httpBanner || '').toLowerCase().trim(),
  });
  return crypto.createHash('sha256').update(normalized).digest('hex');
}

// ─── Matching Engine ──────────────────────────────────────────────────

export async function matchFingerprint(fp: DeviceFingerprint): Promise<MatchResult> {
  const fpHash = hashFingerprint(fp);

  // 1. Check known devices first (exact hash match = instant answer)
  const knownMatch = await db
    .select()
    .from(knownDevices)
    .where(eq(knownDevices.fingerprintHash, fpHash))
    .limit(1);

  if (knownMatch.length > 0) {
    const known = knownMatch[0];
    const communityData = await getCommunityData(fpHash);
    const vendorData = await getVendorData(fp.ouiPrefix);

    return {
      match: {
        deviceType: known.deviceType,
        category: known.threatCategory || 'UNKNOWN',
        threatLevel: known.isThreat ? 'HIGH' : 'LOW',
        confidence: known.confidence,
        description: `Verified device: ${known.brand || ''} ${known.model || ''}`.trim(),
        knownModel: known.model,
        recommendation: known.isThreat
          ? 'This device has been verified as a surveillance device. Investigate immediately.'
          : 'This device has been verified by the community.',
        signatureName: 'known_device_match',
      },
      communityReports: communityData,
      vendor: vendorData,
    };
  }

  // 2. Match against signatures (rule-based)
  const allSignatures = await db.select().from(signatures);
  let bestMatch: (typeof allSignatures)[0] | null = null;
  let bestScore = 0;

  for (const sig of allSignatures) {
    const score = scoreSignature(sig, fp);
    if (score > bestScore && score >= 0.3) {
      bestScore = score;
      bestMatch = sig;
    }
  }

  // 3. Get community data
  const communityData = await getCommunityData(fpHash);

  // 4. Get vendor data
  const vendorData = await getVendorData(fp.ouiPrefix);

  // 5. Build result
  if (bestMatch) {
    const finalConfidence = Math.min(
      bestMatch.confidence * bestScore * (1 + communityData.totalReports * 0.01),
      0.99
    );

    return {
      match: {
        deviceType: bestMatch.name,
        category: bestMatch.category,
        threatLevel: bestMatch.threatLevel,
        confidence: Math.round(finalConfidence * 100) / 100,
        description: bestMatch.description || '',
        knownModel: null,
        recommendation: getRecommendation(bestMatch.threatLevel, bestMatch.category),
        signatureName: bestMatch.name,
      },
      communityReports: communityData,
      vendor: vendorData,
    };
  }

  // No signature match — return vendor data only
  // If the vendor itself is suspicious, report that
  if (vendorData && vendorData.threatWeight >= 50) {
    return {
      match: {
        deviceType: `Unknown ${vendorData.name} Device`,
        category: vendorData.category,
        threatLevel: vendorData.threatWeight >= 80 ? 'HIGH' : 'MEDIUM',
        confidence: Math.round((vendorData.threatWeight / 100) * 0.7 * 100) / 100,
        description: `This device uses a ${vendorData.name} chipset, which is commonly found in IoT and surveillance devices.`,
        knownModel: null,
        recommendation: vendorData.isSurveillanceCommon
          ? 'This chipset is commonly used in surveillance cameras. If you don\'t recognize this device, investigate.'
          : 'This device uses an IoT chipset. Verify it belongs to you.',
        signatureName: 'oui_vendor_match',
      },
      communityReports: communityData,
      vendor: vendorData,
    };
  }

  return {
    match: null,
    communityReports: communityData,
    vendor: vendorData,
  };
}

// ─── Signature Scoring ──────────────────────────────────────────────

function scoreSignature(sig: (typeof signatures.$inferSelect), fp: DeviceFingerprint): number {
  let score = 0;
  let maxScore = 0;

  // 1. OUI vendor match (weight: 3)
  const ouiVendors: string[] = sig.ouiVendors ? JSON.parse(sig.ouiVendors) : [];
  if (ouiVendors.length > 0) {
    maxScore += 3;
    // We need to check if the fp's OUI prefix matches any known vendor in the signature
    // This is checked via a lookup in the calling code, but for now we do a simple approach
    score += 0; // Will be boosted by vendor data separately
  }

  // 2. Port pattern match (weight: 2 for required, 1 per suspicious)
  const portPatterns = sig.portPatterns ? JSON.parse(sig.portPatterns) : { required: [], suspicious: [] };
  const requiredPorts: number[] = portPatterns.required || [];
  const suspiciousPorts: number[] = portPatterns.suspicious || [];

  if (requiredPorts.length > 0) {
    maxScore += 2;
    const allRequired = requiredPorts.every((p: number) => fp.openPorts.includes(p));
    if (allRequired) score += 2;
    else return 0; // Required ports are mandatory
  }

  if (suspiciousPorts.length > 0) {
    maxScore += 2;
    const matchedSuspicious = suspiciousPorts.filter((p: number) => fp.openPorts.includes(p));
    score += Math.min(matchedSuspicious.length * 0.5, 2);
  }

  // 3. Banner pattern match (weight: 3 — strongest signal)
  const bannerPatterns: string[] = sig.bannerPatterns ? JSON.parse(sig.bannerPatterns) : [];
  if (bannerPatterns.length > 0) {
    maxScore += 3;
    const banner = (fp.httpBanner || '').toLowerCase();
    const pageTitle = (fp.pageTitle || '').toLowerCase();
    const combined = `${banner} ${pageTitle}`;

    if (bannerPatterns.some((p: string) => combined.includes(p.toLowerCase()))) {
      score += 3;
    }
  }

  // 4. Protocol response match (weight: 2)
  const protocolResponses = sig.protocolResponses ? JSON.parse(sig.protocolResponses) : {};
  if (Object.keys(protocolResponses).length > 0) {
    maxScore += 2;
    if (protocolResponses.tuya_udp && fp.respondsTuya) score += 2;
    if (protocolResponses.xmeye_udp && fp.respondsXmeye) score += 2;
  }

  // 5. mDNS service match (weight: 2)
  const mdnsServices: string[] = sig.mdnsServices ? JSON.parse(sig.mdnsServices) : [];
  if (mdnsServices.length > 0) {
    maxScore += 2;
    const matched = mdnsServices.filter((s: string) =>
      fp.mDnsServices.some(m => m.includes(s))
    );
    if (matched.length > 0) score += 2;
  }

  return maxScore > 0 ? score / maxScore : 0;
}

// ─── Helpers ────────────────────────────────────────────────────────

async function getCommunityData(fpHash: string) {
  const reports = await db
    .select()
    .from(communityReports)
    .where(eq(communityReports.fingerprintHash, fpHash));

  const classifiedAs: Record<string, number> = {};
  for (const report of reports) {
    classifiedAs[report.userClassification] = (classifiedAs[report.userClassification] || 0) + 1;
  }

  return {
    totalReports: reports.length,
    classifiedAs,
  };
}

async function getVendorData(ouiPrefix: string) {
  const normalized = ouiPrefix.toUpperCase().slice(0, 8);
  const vendor = await db
    .select()
    .from(ouiDatabase)
    .where(eq(ouiDatabase.prefix, normalized))
    .limit(1);

  if (vendor.length === 0) return null;

  return {
    name: vendor[0].vendor,
    category: vendor[0].category || 'UNKNOWN',
    threatWeight: vendor[0].threatWeight || 0,
    isSurveillanceCommon: vendor[0].isSurveillanceCommon || false,
  };
}

function getRecommendation(threatLevel: string, category: string): string {
  if (category === 'CAMERA' || category === 'SURVEILLANCE') {
    switch (threatLevel) {
      case 'CRITICAL':
        return 'This device is almost certainly a surveillance camera. If you did not install it, take immediate action: disconnect it from the network and investigate its physical location.';
      case 'HIGH':
        return 'This device has strong camera/surveillance characteristics. If you don\'t recognize it, investigate immediately.';
      case 'MEDIUM':
        return 'This device may be a camera or IoT device. Verify it belongs to you or someone you trust.';
      default:
        return 'This device has some characteristics of surveillance equipment. Monitor it.';
    }
  }

  if (category === 'IOT') {
    return 'This is an IoT device. If you don\'t recognize it, it could be a smart home device someone else placed, or a hidden surveillance device.';
  }

  return 'Unable to determine the exact nature of this device. If you don\'t recognize it, investigate.';
}
