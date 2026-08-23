package com.devicelens.app.di

import android.content.Context
import androidx.room.Room
import com.devicelens.app.data.db.AppDatabase
import com.devicelens.app.data.db.DeviceDao
import com.devicelens.app.data.db.NetworkFactsDao
import com.devicelens.app.data.db.TrackerSightingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDeviceDao(database: AppDatabase): DeviceDao =
        database.deviceDao()

    @Provides
    @Singleton
    fun provideTrackerSightingDao(database: AppDatabase): TrackerSightingDao =
        database.trackerSightingDao()

    @Provides
    @Singleton
    fun provideNetworkFactsDao(database: AppDatabase): NetworkFactsDao =
        database.networkFactsDao()
}
