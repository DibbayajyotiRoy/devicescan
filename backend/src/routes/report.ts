import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { validate } from '../middleware/validate';
import { writeLimiter } from '../middleware/rateLimit';
import { db } from '../db/connection';
import { communityReports, knownDevices } from '../db/schema';
import { hashFingerprint, DeviceFingerprint } from '../engine/matcher';
import { eq, sql } from 'drizzle-orm';
import crypto from 'crypto';

const router = Router();

const reportSchema = z.object({
  fingerprint: z.object({
    ouiPrefix: z.string().min(6).max(17),
    openPorts: z.array(z.number()).default([]),
    httpBanner: z.string().nullable().default(null),
    ssdpResponse: z.string().nullable().default(null),
    mDnsServices: z.array(z.string()).default([]),
    bleManufacturerData: z.string().nullable().default(null),
    pageTitle: z.string().nullable().default(null),
    respondsTuya: z.boolean().default(false),
    respondsXmeye: z.boolean().default(false),
  }),
  userClassification: z.enum([
    'spy_camera',
    'security_camera',
    'smart_plug',
    'smart_speaker',
    'phone',
    'tablet',
    'laptop',
    'desktop',
    'printer',
    'router',
    'smart_tv',
    'gaming_console',
    'iot_sensor',
    'smart_light',
    'thermostat',
    'doorbell',
    'audio_bug',
    'gps_tracker',
    'other_safe',
    'other_suspicious',
  ]),
  userDescription: z.string().max(500).optional(),
});

// POST /api/v1/report
// Submit a community device identification report
router.post('/', writeLimiter, validate(reportSchema), async (req: Request, res: Response): Promise<void> => {
  try {
    const { fingerprint, userClassification, userDescription } = req.body;
    const fpHash = hashFingerprint(fingerprint as DeviceFingerprint);

    // Hash the IP for dedup (never store raw IPs)
    const ipHash = crypto
      .createHash('sha256')
      .update(req.ip || 'unknown')
      .digest('hex')
      .slice(0, 16);

    // Insert community report
    await db.insert(communityReports).values({
      fingerprintHash: fpHash,
      ouiPrefix: fingerprint.ouiPrefix,
      openPorts: JSON.stringify(fingerprint.openPorts),
      httpBanner: fingerprint.httpBanner,
      userClassification,
      userDescription: userDescription || null,
      ipHash,
    });

    // If we have enough consistent reports (5+), auto-create a known device entry
    const existingReports = await db
      .select()
      .from(communityReports)
      .where(eq(communityReports.fingerprintHash, fpHash));

    const classificationCounts: Record<string, number> = {};
    for (const r of existingReports) {
      classificationCounts[r.userClassification] = (classificationCounts[r.userClassification] || 0) + 1;
    }

    const topClassification = Object.entries(classificationCounts)
      .sort(([, a], [, b]) => b - a)[0];

    if (topClassification && topClassification[1] >= 5) {
      const isThreat = ['spy_camera', 'audio_bug', 'gps_tracker', 'other_suspicious'].includes(topClassification[0]);
      const confidence = topClassification[1] / existingReports.length;

      // Upsert known device
      await db
        .insert(knownDevices)
        .values({
          fingerprintHash: fpHash,
          deviceType: topClassification[0],
          isThreat,
          threatCategory: isThreat ? topClassification[0] : null,
          confidence: Math.round(confidence * 100) / 100,
          reportCount: existingReports.length,
        })
        .onConflictDoUpdate({
          target: knownDevices.fingerprintHash,
          set: {
            deviceType: topClassification[0],
            isThreat,
            threatCategory: isThreat ? topClassification[0] : null,
            confidence: Math.round(confidence * 100) / 100,
            reportCount: existingReports.length,
            lastSeen: new Date(),
          },
        });
    }

    res.status(201).json({
      success: true,
      reportId: fpHash.slice(0, 12),
      message: 'Thank you for your report. This helps protect other users.',
      totalReportsForDevice: existingReports.length,
    });
  } catch (error) {
    console.error('Report error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to submit report',
    });
  }
});

export default router;
