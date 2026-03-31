import Foundation
import CoreData

final class DeviceRepository {
    private let context: NSManagedObjectContext

    init(context: NSManagedObjectContext = PersistenceController.shared.viewContext) {
        self.context = context
    }

    func findByCompositeKey(_ key: String) -> DeviceRecord? {
        let request = DeviceRecord.fetchRequest()
        request.predicate = NSPredicate(format: "compositeKey == %@", key)
        request.fetchLimit = 1
        return try? context.fetch(request).first
    }

    func findById(_ id: UUID) -> DeviceRecord? {
        let request = DeviceRecord.fetchRequest()
        request.predicate = NSPredicate(format: "id == %@", id as CVarArg)
        request.fetchLimit = 1
        return try? context.fetch(request).first
    }

    func fetchAll() -> [DeviceRecord] {
        let request = DeviceRecord.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "lastSeen", ascending: false)]
        return (try? context.fetch(request)) ?? []
    }

    func upsertAll(_ devices: [DeviceSummary]) {
        for device in devices {
            if let existing = findByCompositeKey(device.compositeKey) {
                existing.lastSeen = Date()
                existing.seenCount += 1
                existing.riskLevel = device.riskLevel
                if let rssi = device.rssi { existing.rssiLastSeen = Int32(rssi) }
                existing.deviceName = device.deviceName
                existing.vendor = device.vendor
            } else {
                let record = DeviceRecord(context: context)
                record.id = UUID()
                record.compositeKey = device.compositeKey
                record.deviceName = device.deviceName
                record.vendor = device.vendor
                record.detectionMethod = device.detectionMethod
                record.firstSeen = Date()
                record.lastSeen = Date()
                record.seenCount = 1
                record.isTrustedByUser = false
                record.riskLevel = device.riskLevel
                record.rssiLastSeen = Int32(device.rssi ?? -100)
            }
        }
        save()
    }

    func markTrusted(compositeKey: String) {
        guard let record = findByCompositeKey(compositeKey) else { return }
        record.isTrustedByUser = true
        record.riskLevel = "SAFE"
        save()
    }

    func markTrustedById(_ id: UUID) {
        guard let record = findById(id) else { return }
        record.isTrustedByUser = true
        record.riskLevel = "SAFE"
        save()
    }

    func dismissById(_ id: UUID) {
        guard let record = findById(id) else { return }
        record.riskLevel = "UNKNOWN"
        save()
    }

    func deleteAll() {
        let request = NSFetchRequest<NSFetchRequestResult>(entityName: "DeviceRecord")
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: request)
        try? context.execute(deleteRequest)
        save()
    }

    private func save() {
        guard context.hasChanges else { return }
        try? context.save()
    }
}
