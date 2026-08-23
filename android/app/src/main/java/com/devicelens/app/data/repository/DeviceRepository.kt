package com.devicelens.app.data.repository

import com.devicelens.app.data.db.DeviceDao
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.domain.model.DeviceSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao
) {
    private val IP_PATTERN = Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")

    fun observeAll(): Flow<List<DeviceEntity>> = deviceDao.observeAll()

    fun observeByNetwork(networkId: String): Flow<List<DeviceEntity>> =
        deviceDao.observeByNetwork(networkId)

    suspend fun getAll(): List<DeviceEntity> = deviceDao.getAll()

    suspend fun getAllByNetwork(networkId: String): List<DeviceEntity> =
        deviceDao.getAllByNetwork(networkId)

    suspend fun pruneStaleForNetwork(networkId: String, cutoffMs: Long) =
        deviceDao.pruneStaleForNetwork(networkId, cutoffMs)

    suspend fun findById(id: Long): DeviceEntity? = deviceDao.findById(id)

    suspend fun findByCompositeKey(key: String): DeviceEntity? =
        deviceDao.findByCompositeKey(key)

    /**
     * Writes one scan's results as a single transaction.
     *
     * The previous version looked up and wrote each device individually inside a
     * loop. On a busy network that meant several hundred separate writes, each
     * one waking every observer of the table and recomposing the whole device
     * list — which is what made the list churn and jump while a scan was
     * running. Now the reads are batched, the merge happens in memory, and there
     * is exactly one write and one emission.
     */
    suspend fun upsertAll(devices: List<DeviceSummary>) {
        if (devices.isEmpty()) return

        val now = System.currentTimeMillis()
        val existingByKey = deviceDao
            .findByCompositeKeys(devices.map { it.compositeKey })
            .associateBy { it.compositeKey }

        val inserts = mutableListOf<DeviceEntity>()
        val updates = mutableListOf<DeviceEntity>()

        for (device in devices) {
            val existing = existingByKey[device.compositeKey]
            if (existing != null) {
                updates += existing.copy(
                    lastSeen = now,
                    seenCount = existing.seenCount + 1,
                    riskLevel = if (existing.isTrustedByUser) "SAFE" else device.riskLevel,
                    rssiLastSeen = device.rssi ?: existing.rssiLastSeen,
                    // Never let a later scan downgrade a real name back to a bare
                    // IP address just because mDNS did not answer that time.
                    deviceName = if (device.deviceName.matches(IP_PATTERN) &&
                        existing.deviceName.isNotBlank() &&
                        !existing.deviceName.matches(IP_PATTERN)
                    ) existing.deviceName else device.deviceName,
                    vendor = if (device.vendor != "Unknown") device.vendor else existing.vendor,
                    macAddress = device.macAddress ?: existing.macAddress,
                    ipAddress = device.ipAddress ?: existing.ipAddress,
                    deviceType = if (!device.deviceType.isNullOrBlank()) device.deviceType else existing.deviceType,
                    openPorts = if (!device.openPorts.isNullOrBlank()) device.openPorts else existing.openPorts,
                    networkId = device.networkId
                )
            } else {
                inserts += DeviceEntity(
                    compositeKey = device.compositeKey,
                    deviceName = device.deviceName,
                    vendor = device.vendor,
                    detectionMethod = device.detectionMethod,
                    firstSeen = now,
                    lastSeen = now,
                    seenCount = 1,
                    isTrustedByUser = device.isTrustedByUser,
                    riskLevel = device.riskLevel,
                    rssiLastSeen = device.rssi,
                    macAddress = device.macAddress,
                    ipAddress = device.ipAddress,
                    deviceType = device.deviceType ?: "",
                    openPorts = device.openPorts ?: "",
                    networkId = device.networkId
                )
            }
        }

        deviceDao.applyScan(inserts = inserts, updates = updates)
    }

    suspend fun markTrusted(compositeKey: String) {
        val entity = deviceDao.findByCompositeKey(compositeKey) ?: return
        deviceDao.update(entity.copy(isTrustedByUser = true, riskLevel = "SAFE"))
    }

    suspend fun markTrustedById(id: Long) {
        val entity = deviceDao.findById(id) ?: return
        deviceDao.update(entity.copy(isTrustedByUser = true, riskLevel = "SAFE"))
    }

    suspend fun dismissById(id: Long) {
        val entity = deviceDao.findById(id) ?: return
        deviceDao.update(entity.copy(riskLevel = "UNKNOWN"))
    }

    suspend fun deleteAll() = deviceDao.deleteAll()
}
