import Foundation
import CoreData

@objc(DeviceRecord)
public class DeviceRecord: NSManagedObject, Identifiable {
    @NSManaged public var id: UUID
    @NSManaged public var compositeKey: String
    @NSManaged public var macAddress: String?
    @NSManaged public var deviceName: String
    @NSManaged public var vendor: String
    @NSManaged public var detectionMethod: String
    @NSManaged public var firstSeen: Date
    @NSManaged public var lastSeen: Date
    @NSManaged public var seenCount: Int32
    @NSManaged public var isTrustedByUser: Bool
    @NSManaged public var riskLevel: String
    @NSManaged public var rssiLastSeen: Int32
    @NSManaged public var ipAddress: String?
    @NSManaged public var notes: String?
}

extension DeviceRecord {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<DeviceRecord> {
        return NSFetchRequest<DeviceRecord>(entityName: "DeviceRecord")
    }
}
