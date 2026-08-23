package com.devicelens.app.domain.analysis

import com.devicelens.app.data.db.TrackerSightingDao
import com.devicelens.app.data.db.TrackerSightingEntity
import com.devicelens.app.domain.scanner.BleAdvertParser
import com.devicelens.app.helpers.DebugLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Decides whether a Bluetooth device is *following the user*, as opposed to
 * merely being nearby once.
 *
 * The distinction is the entire product. A shopping centre will show twenty
 * AirTags; none of them are your problem. A tag that is still with you twenty
 * minutes later, after you have changed rooms — or better, after your phone has
 * joined a different Wi-Fi network — is a very different thing.
 *
 * Three offline signals drive the verdict:
 *
 *  - **Persistence**: the same identity in several separate scans.
 *  - **Duration**: a span long enough to rule out a shared bus stop.
 *  - **Displacement**: seen on two different networks, i.e. the user physically
 *    moved and it came along. No GPS and no location permission needed.
 *
 * The known limit, stated plainly rather than papered over: Find My and SmartTag
 * accessories rotate their MAC address, so an identity can only be followed
 * until it rotates. A tag *separated from its owner* rotates far more slowly,
 * which is exactly the case that matters here.
 */
@Singleton
class TrackerDetector @Inject constructor(
    private val dao: TrackerSightingDao
) {
    private val TAG = "TrackerDetector"

    data class Sighting(
        val identity: String,
        val label: String,
        val trackerNetwork: String?,
        val vendor: String?,
        val rssi: Int?,
        val separatedFromOwner: Boolean
    )

    data class TrackerAlert(
        val identity: String,
        val label: String,
        val trackerNetwork: String?,
        val severity: Severity,
        val headline: String,
        val detail: String,
        val advice: String,
        val minutesObserved: Long,
        val scanCount: Int,
        val networksSeenOn: Int,
        val closestRssi: Int?
    ) {
        enum class Severity { INFO, WARNING, CRITICAL }
    }

    /**
     * Folds one scan's tracker sightings into the persistent history.
     * Called once per scan; each identity therefore counts at most once.
     */
    suspend fun record(sightings: List<Sighting>, networkId: String, now: Long = System.currentTimeMillis()) {
        for (sighting in sightings) {
            val existing = dao.find(sighting.identity)
            if (existing == null) {
                dao.insert(
                    TrackerSightingEntity(
                        identity = sighting.identity,
                        label = sighting.label,
                        trackerNetwork = sighting.trackerNetwork,
                        vendor = sighting.vendor,
                        firstSeen = now,
                        lastSeen = now,
                        scanCount = 1,
                        networkIds = networkId,
                        closestRssi = sighting.rssi,
                        lastRssi = sighting.rssi,
                        separatedFromOwner = sighting.separatedFromOwner
                    )
                )
            } else {
                // Sightings inside one scan window are the same encounter, not
                // repeated evidence — only count a genuinely later scan.
                val isNewScan = now - existing.lastSeen >= MIN_SCAN_GAP_MS
                dao.update(
                    existing.copy(
                        label = sighting.label.takeIf { it.isNotBlank() } ?: existing.label,
                        vendor = sighting.vendor ?: existing.vendor,
                        lastSeen = now,
                        scanCount = if (isNewScan) existing.scanCount + 1 else existing.scanCount,
                        networkIds = (existing.networkIdSet + networkId).joinToString(","),
                        closestRssi = maxOfNullable(existing.closestRssi, sighting.rssi),
                        lastRssi = sighting.rssi ?: existing.lastRssi,
                        separatedFromOwner = existing.separatedFromOwner || sighting.separatedFromOwner
                    )
                )
            }
        }

        dao.pruneOlderThan(now - HISTORY_WINDOW_MS)
    }

    /** Everything currently worth telling the user about. */
    suspend fun evaluate(now: Long = System.currentTimeMillis()): List<TrackerAlert> {
        val recent = dao.seenSince(now - RECENT_WINDOW_MS).filter { !it.isIgnored }
        return recent.mapNotNull { assess(it) }
            .sortedByDescending { it.severity.ordinal * 1000 + it.scanCount }
    }

    suspend fun markAsMine(identity: String) = dao.ignore(identity)

    suspend fun clearHistory() = dao.deleteAll()

    private fun assess(entity: TrackerSightingEntity): TrackerAlert? {
        val spanMinutes = (entity.lastSeen - entity.firstSeen) / 60_000
        val networks = entity.networkIdSet.filter { it.isNotBlank() && it != "offline" }.size
        val closest = entity.closestRssi

        // Seen on two different networks means the phone physically moved and
        // this tag came with it. That is the strongest offline evidence there is.
        val movedWithUser = networks >= 2
        val persistent = entity.scanCount >= MIN_SCANS_FOR_ALERT && spanMinutes >= MIN_MINUTES_FOR_ALERT
        val veryClose = closest != null && closest > CLOSE_RSSI

        val severity = when {
            movedWithUser && entity.separatedFromOwner -> TrackerAlert.Severity.CRITICAL
            movedWithUser -> TrackerAlert.Severity.CRITICAL
            persistent && entity.separatedFromOwner -> TrackerAlert.Severity.CRITICAL
            persistent -> TrackerAlert.Severity.WARNING
            entity.scanCount >= 2 && veryClose -> TrackerAlert.Severity.INFO
            else -> return null
        }

        val headline = when (severity) {
            TrackerAlert.Severity.CRITICAL ->
                if (movedWithUser) "${entity.label} has moved with you"
                else "${entity.label} has stayed with you"
            TrackerAlert.Severity.WARNING -> "${entity.label} keeps appearing near you"
            TrackerAlert.Severity.INFO -> "${entity.label} seen nearby more than once"
        }

        val evidence = buildList {
            add("seen in ${entity.scanCount} separate scans")
            if (spanMinutes > 0) add("over ${formatSpan(spanMinutes)}")
            if (movedWithUser) add("on $networks different networks")
            if (entity.separatedFromOwner) add("and it is reporting itself as separated from its owner")
            if (veryClose) add("at very close range (${closest} dBm)")
        }.joinToString(", ")

        val advice = when {
            entity.separatedFromOwner || movedWithUser ->
                "Search your bag, clothing, and vehicle. If you find a tag you do not own, " +
                    "an iPhone or the Google Find Hub app can make it play a sound, and the tag's " +
                    "serial number can be given to the police. Do not destroy it — it is evidence."
            severity == TrackerAlert.Severity.WARNING ->
                "Keep the app scanning as you move. If it is still with you at your next stop, " +
                    "treat it as a real tracker and search your belongings."
            else ->
                "Probably a device that lives where you do. Mark it as yours to stop these alerts."
        }

        DebugLog.i(
            TAG,
            "Tracker ${entity.identity} → $severity scans=${entity.scanCount} " +
                "span=${spanMinutes}m networks=$networks separated=${entity.separatedFromOwner}"
        )

        return TrackerAlert(
            identity = entity.identity,
            label = entity.label,
            trackerNetwork = entity.trackerNetwork,
            severity = severity,
            headline = headline,
            detail = "This ${entity.trackerNetwork ?: "Bluetooth"} tag was $evidence.",
            advice = advice,
            minutesObserved = spanMinutes,
            scanCount = entity.scanCount,
            networksSeenOn = max(networks, 1),
            closestRssi = closest
        )
    }

    private fun formatSpan(minutes: Long): String = when {
        minutes < 60 -> "$minutes minutes"
        minutes < 1440 -> "${minutes / 60} hours"
        else -> "${minutes / 1440} days"
    }

    /** RSSI is negative; "greater" means closer. */
    private fun maxOfNullable(a: Int?, b: Int?): Int? = when {
        a == null -> b
        b == null -> a
        else -> max(a, b)
    }

    companion object {
        /** Builds the sighting a parsed advertisement implies, or null if it is not a tracker. */
        fun sightingFrom(
            address: String,
            advert: BleAdvertParser.Advert,
            rssi: Int?
        ): Sighting? {
            val tracker = advert.tracker ?: return null
            return Sighting(
                identity = address.uppercase(),
                label = tracker.label,
                trackerNetwork = tracker.network,
                vendor = advert.companyName,
                rssi = rssi,
                separatedFromOwner = tracker.separatedFromOwner
            )
        }

        private const val MIN_SCAN_GAP_MS = 60_000L        // 1 minute
        private const val MIN_SCANS_FOR_ALERT = 3
        private const val MIN_MINUTES_FOR_ALERT = 20L
        private const val CLOSE_RSSI = -60
        private const val RECENT_WINDOW_MS = 12 * 60 * 60 * 1000L  // 12 hours
        private const val HISTORY_WINDOW_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
