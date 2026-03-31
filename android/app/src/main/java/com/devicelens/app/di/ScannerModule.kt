package com.devicelens.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {
    // WifiScanner, BleScanner, MagnetometerMonitor, IrDetector
    // are all constructor-injected via @Inject and don't need explicit @Provides.
    // ClassificationEngine and ScanOrchestrator are also constructor-injected.
}
