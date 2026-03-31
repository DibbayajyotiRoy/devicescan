import { Router, Request, Response } from 'express';
import { db } from '../db/connection';
import { ouiDatabase } from '../db/schema';
import { eq, ilike } from 'drizzle-orm';
import { apiLimiter } from '../middleware/rateLimit';

const router = Router();

// GET /api/v1/oui/:prefix
// Look up a MAC OUI prefix and return vendor + threat intelligence
router.get('/:prefix', apiLimiter, async (req: Request, res: Response): Promise<void> => {
  try {
    const prefix = (req.params.prefix as string).toUpperCase().replace(/[-\.]/g, ':').slice(0, 8);

    if (!/^[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}$/.test(prefix)) {
      res.status(400).json({
        success: false,
        error: 'Invalid OUI prefix format. Expected XX:XX:XX (e.g., 24:0A:C4)',
      });
      return;
    }

    const result = await db
      .select()
      .from(ouiDatabase)
      .where(eq(ouiDatabase.prefix, prefix))
      .limit(1);

    if (result.length === 0) {
      res.json({
        success: true,
        found: false,
        prefix,
        vendor: null,
      });
      return;
    }

    const entry = result[0];
    res.json({
      success: true,
      found: true,
      prefix,
      vendor: {
        name: entry.vendor,
        category: entry.category,
        threatWeight: entry.threatWeight,
        isSurveillanceCommon: entry.isSurveillanceCommon,
        aliases: entry.aliases ? JSON.parse(entry.aliases) : [],
      },
    });
  } catch (error) {
    console.error('OUI lookup error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to look up OUI prefix',
    });
  }
});

// GET /api/v1/oui/search/:vendor
// Search OUI entries by vendor name (partial match)
router.get('/search/:vendor', apiLimiter, async (req: Request, res: Response): Promise<void> => {
  try {
    const vendorQuery = req.params.vendor;
    const results = await db
      .select()
      .from(ouiDatabase)
      .where(ilike(ouiDatabase.vendor, `%${vendorQuery}%`))
      .limit(50);

    res.json({
      success: true,
      count: results.length,
      entries: results.map(e => ({
        prefix: e.prefix,
        vendor: e.vendor,
        category: e.category,
        threatWeight: e.threatWeight,
        isSurveillanceCommon: e.isSurveillanceCommon,
      })),
    });
  } catch (error) {
    console.error('OUI search error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to search OUI database',
    });
  }
});

export default router;
