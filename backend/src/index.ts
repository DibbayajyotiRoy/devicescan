import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import { apiLimiter } from './middleware/rateLimit';
import identifyRoutes from './routes/identify';
import reportRoutes from './routes/report';
import signaturesRoutes from './routes/signatures';
import ouiRoutes from './routes/oui';
import { db } from './db/connection';
import { ouiDatabase, signatures, communityReports, knownDevices } from './db/schema';
import { sql } from 'drizzle-orm';

dotenv.config();

const app = express();
const PORT = parseInt(process.env.PORT || '3000');

// ─── Middleware ──────────────────────────────────────────────────────
app.use(helmet());
app.use(cors({
  origin: process.env.NODE_ENV === 'production'
    ? ['https://devicelens.app'] // Restrict in production
    : '*',
  methods: ['GET', 'POST'],
  allowedHeaders: ['Content-Type', 'Authorization'],
}));
app.use(express.json({ limit: '1mb' }));
app.use(apiLimiter);

// ─── Routes ─────────────────────────────────────────────────────────
app.use('/api/v1/identify', identifyRoutes);
app.use('/api/v1/report', reportRoutes);
app.use('/api/v1/signatures', signaturesRoutes);
app.use('/api/v1/oui', ouiRoutes);

// ─── Health Check ──────────────────────────────────────────────────
app.get('/api/v1/health', async (_req, res) => {
  try {
    // Quick DB check
    const ouiCount = await db.select({ count: sql<number>`count(*)` }).from(ouiDatabase);
    const sigCount = await db.select({ count: sql<number>`count(*)` }).from(signatures);
    const reportCount = await db.select({ count: sql<number>`count(*)` }).from(communityReports);
    const knownCount = await db.select({ count: sql<number>`count(*)` }).from(knownDevices);

    res.json({
      status: 'healthy',
      version: '1.0.0',
      database: {
        connected: true,
        ouiEntries: ouiCount[0].count,
        signatures: sigCount[0].count,
        communityReports: reportCount[0].count,
        knownDevices: knownCount[0].count,
      },
      uptime: process.uptime(),
    });
  } catch (error) {
    res.status(503).json({
      status: 'unhealthy',
      error: 'Database connection failed',
    });
  }
});

// ─── 404 Handler ───────────────────────────────────────────────────
app.use((_req, res) => {
  res.status(404).json({
    error: 'Not found',
    availableEndpoints: [
      'POST /api/v1/identify',
      'POST /api/v1/identify/batch',
      'POST /api/v1/report',
      'GET  /api/v1/signatures/latest',
      'GET  /api/v1/oui/:prefix',
      'GET  /api/v1/oui/search/:vendor',
      'GET  /api/v1/health',
    ],
  });
});

// ─── Error Handler ─────────────────────────────────────────────────
app.use((err: Error, _req: express.Request, res: express.Response, _next: express.NextFunction) => {
  console.error('Unhandled error:', err);
  res.status(500).json({
    error: 'Internal server error',
  });
});

// ─── Start ─────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`
╔══════════════════════════════════════════════════╗
║         DeviceLens Intelligence API              ║
║         Running on port ${PORT}                     ║
║         Environment: ${process.env.NODE_ENV || 'development'}             ║
╚══════════════════════════════════════════════════╝

Endpoints:
  POST /api/v1/identify          Identify a device
  POST /api/v1/identify/batch    Identify multiple devices
  POST /api/v1/report            Submit community report
  GET  /api/v1/signatures/latest Get latest signatures
  GET  /api/v1/oui/:prefix       OUI lookup
  GET  /api/v1/health            Health check
`);
});

export default app;
