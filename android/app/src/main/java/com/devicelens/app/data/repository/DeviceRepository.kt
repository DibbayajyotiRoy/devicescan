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
    fun observeAll(): Flow<List<DeviceEntity>> = deviceDao.observeAll()

    suspend fun getAll(): List<DeviceEntity> = deviceDao.getAll()

    suspend fun findById(id: Long): DeviceEntity? = deviceDao.findById(id)

    suspend fun findByCompositeKey(key: String): DeviceEntity? =
        deviceDao.findByCompositeKey(key)

    suspend fun upsertAll(devices: List<DeviceSummary>) {
        for (device in devices) {
            val existing = deviceDao.findByCompositeKey(device.compositeKey)
            if (existing != null) {
                val finalRisk = if (existing.isTrustedByUser) "SAFE" else device.riskLevel
                deviceDao.update(
                    existing.copy(
                        lastSeen = System.currentTimeMillis(),
                        seenCount = existing.seenCount + 1,
                        riskLevel = finalRisk,
                        rssiLastSeen = device.rssi ?: existing.rssiLastSeen,
                        deviceName = if (device.deviceName.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) && existing.deviceName.isNotBlank() && !existing.deviceName.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")))
                            existing.deviceName else device.deviceName,
                        vendor = if (device.vendor != "Unknown") device.vendor else existing.vendor,
                        macAddress = device.macAddress ?: existing.macAddress,
                        ipAddress = device.ipAddress ?: existing.ipAddress,
                        deviceType = if (!device.deviceType.isNullOrBlank()) device.deviceType else existing.deviceType,
                        openPorts = if (!device.openPorts.isNullOrBlank()) device.openPorts else existing.openPorts
                    )
                )
            } else {
                val now = System.currentTimeMillis()
                deviceDao.insert(
                    DeviceEntity(
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
                        openPorts = device.openPorts ?: ""
                    )
                )
            }
        }
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
