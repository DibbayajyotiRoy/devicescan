package com.devicelens.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DeviceEntity::class,
        TrackerSightingEntity::class,
        NetworkFactsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun trackerSightingDao(): TrackerSightingDao
    abstract fun networkFactsDao(): NetworkFactsDao

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

        // v4: offline intelligence tables.
        //  - tracker_sightings remembers Bluetooth identities across scans, which
        //    is the only way to tell "a tag is nearby" from "a tag is following
        //    you". In-memory state would be lost every time the app is killed.
        //  - network_facts remembers each network's gateway MAC so a router that
        //    silently changes identity can be spotted on the next visit.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tracker_sightings (
                        identity TEXT NOT NULL PRIMARY KEY,
                        label TEXT NOT NULL,
                        tracker_network TEXT,
                        vendor TEXT,
                        first_seen INTEGER NOT NULL,
                        last_seen INTEGER NOT NULL,
                        scan_count INTEGER NOT NULL DEFAULT 1,
                        network_ids TEXT NOT NULL DEFAULT '',
                        closest_rssi INTEGER,
                        last_rssi INTEGER,
                        separated_from_owner INTEGER NOT NULL DEFAULT 0,
                        is_ignored INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_tracker_sightings_last_seen " +
                        "ON tracker_sightings(last_seen)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS network_facts (
                        network_id TEXT NOT NULL PRIMARY KEY,
                        ssid TEXT,
                        bssid TEXT,
                        gateway_ip TEXT,
                        gateway_mac TEXT,
                        dns_servers TEXT NOT NULL DEFAULT '',
                        first_seen INTEGER NOT NULL,
                        last_seen INTEGER NOT NULL,
                        device_count INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
