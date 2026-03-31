import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { validate } from '../middleware/validate';
import { identifyLimiter } from '../middleware/rateLimit';
import { matchFingerprint, DeviceFingerprint } from '../engine/matcher';

const router = Router();

const identifySchema = z.object({
  ouiPrefix: z.string().min(6).max(17),
  openPorts: z.array(z.number().int().min(0).max(65535)).default([]),
  httpBanner: z.string().nullable().default(null),
  ssdpResponse: z.string().nullable().default(null),
  mDnsServices: z.array(z.string()).default([]),
  bleManufacturerData: z.string().nullable().default(null),
  pageTitle: z.string().nullable().default(null),
  respondsTuya: z.boolean().default(false),
  respondsXmeye: z.boolean().default(false),
});

// POST /api/v1/identify
// Identify a device from its network fingerprint
router.post('/', identifyLimiter, validate(identifySchema), async (req: Request, res: Response): Promise<void> => {
  try {
    const fingerprint: DeviceFingerprint = req.body;
    const result = await matchFingerprint(fingerprint);

    res.json({
      success: true,
      ...result,
    });
  } catch (error) {
    console.error('Identify error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error during device identification',
    });
  }
});

// POST /api/v1/identify/batch
// Identify multiple devices in one request
const batchSchema = z.object({
  devices: z.array(identifySchema).min(1).max(50),
});

router.post('/batch', identifyLimiter, validate(batchSchema), async (req: Request, res: Response): Promise<void> => {
  try {
    const { devices } = req.body;
    const results = await Promise.all(
      devices.map((fp: DeviceFingerprint) => matchFingerprint(fp))
    );

    res.json({
      success: true,
      results,
    });
  } catch (error) {
    console.error('Batch identify error:', error);
    res.status(500).json({
      success: false,
      error: 'Internal server error during batch identification',
    });
  }
});

export default router;
