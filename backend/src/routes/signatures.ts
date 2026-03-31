import { Router, Request, Response } from 'express';
import { db } from '../db/connection';
import { signatures } from '../db/schema';
import { gt } from 'drizzle-orm';
import { apiLimiter } from '../middleware/rateLimit';

const router = Router();

// GET /api/v1/signatures/latest
// Returns all signatures, optionally filtered by version
// The app syncs this periodically for offline matching
router.get('/latest', apiLimiter, async (req: Request, res: Response): Promise<void> => {
  try {
    const sinceVersion = parseInt(req.query.sinceVersion as string) || 0;

    let sigs;
    if (sinceVersion > 0) {
      sigs = await db
        .select()
        .from(signatures)
        .where(gt(signatures.version, sinceVersion));
    } else {
      sigs = await db.select().from(signatures);
    }

    res.json({
      success: true,
      currentVersion: sigs.reduce((max, s) => Math.max(max, s.version || 1), 0),
      count: sigs.length,
      signatures: sigs.map(s => ({
        id: s.id,
        name: s.name,
        description: s.description,
        category: s.category,
        threatLevel: s.threatLevel,
        confidence: s.confidence,
        ouiVendors: s.ouiVendors ? JSON.parse(s.ouiVendors) : [],
        portPatterns: s.portPatterns ? JSON.parse(s.portPatterns) : {},
        bannerPatterns: s.bannerPatterns ? JSON.parse(s.bannerPatterns) : [],
        protocolResponses: s.protocolResponses ? JSON.parse(s.protocolResponses) : {},
        mdnsServices: s.mdnsServices ? JSON.parse(s.mdnsServices) : [],
        source: s.source,
        reportCount: s.reportCount,
        version: s.version,
        updatedAt: s.updatedAt,
      })),
    });
  } catch (error) {
    console.error('Signatures error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch signatures',
    });
  }
});

export default router;
