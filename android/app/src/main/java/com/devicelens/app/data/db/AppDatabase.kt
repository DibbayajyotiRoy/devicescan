package com.devicelens.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DeviceEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        const val DATABASE_NAME = "device_lens.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN device_type TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE devices ADD COLUMN open_ports TEXT NOT NULL DEFAULT ''")
            }
        }

        // v3: add network_id so devices are scoped to the Wi-Fi they were seen on.
        // Existing rows get network_id='offline' so they stop appearing in any real
        // network scan (user will just re-discover them on their current network).
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE devices ADD COLUMN network_id TEXT NOT NULL DEFAULT 'offline'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_network_id ON devices(network_id)")
            }
        }
    }
}
