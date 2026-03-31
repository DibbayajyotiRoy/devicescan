import Foundation
import CoreData
import Combine

final class PersistenceController {
    static let shared = PersistenceController()

    let container: NSPersistentContainer

    init() {
        container = NSPersistentContainer(name: "DeviceLens")

        // Create the Core Data model programmatically
        let model = NSManagedObjectModel()

        let entity = NSEntityDescription()
        entity.name = "DeviceRecord"
        entity.managedObjectClassName = "DeviceRecord"

        let id = NSAttributeDescription()
        id.name = "id"
        id.attributeType = .UUIDAttributeType
        id.isOptional = false

        let compositeKey = NSAttributeDescription()
        compositeKey.name = "compositeKey"
        compositeKey.attributeType = .stringAttributeType
        compositeKey.isOptional = false

        let macAddress = NSAttributeDescription()
        macAddress.name = "macAddress"
        macAddress.attributeType = .stringAttributeType
        macAddress.isOptional = true

        let deviceName = NSAttributeDescription()
        deviceName.name = "deviceName"
        deviceName.attributeType = .stringAttributeType
        deviceName.isOptional = false
        deviceName.defaultValue = "Unknown Device"

        let vendorAttr = NSAttributeDescription()
        vendorAttr.name = "vendor"
        vendorAttr.attributeType = .stringAttributeType
        vendorAttr.isOptional = false
        vendorAttr.defaultValue = "Unknown"

        let detectionMethod = NSAttributeDescription()
        detectionMethod.name = "detectionMethod"
        detectionMethod.attributeType = .stringAttributeType
        detectionMethod.isOptional = false

        let firstSeen = NSAttributeDescription()
        firstSeen.name = "firstSeen"
        firstSeen.attributeType = .dateAttributeType
        firstSeen.isOptional = false

        let lastSeen = NSAttributeDescription()
        lastSeen.name = "lastSeen"
        lastSeen.attributeType = .dateAttributeType
        lastSeen.isOptional = false

        let seenCount = NSAttributeDescription()
        seenCount.name = "seenCount"
        seenCount.attributeType = .integer32AttributeType
        seenCount.isOptional = false
        seenCount.defaultValue = 1

        let isTrustedByUser = NSAttributeDescription()
        isTrustedByUser.name = "isTrustedByUser"
        isTrustedByUser.attributeType = .booleanAttributeType
        isTrustedByUser.isOptional = false
        isTrustedByUser.defaultValue = false

        let riskLevel = NSAttributeDescription()
        riskLevel.name = "riskLevel"
        riskLevel.attributeType = .stringAttributeType
        riskLevel.isOptional = false
        riskLevel.defaultValue = "UNKNOWN"

        let rssiLastSeen = NSAttributeDescription()
        rssiLastSeen.name = "rssiLastSeen"
        rssiLastSeen.attributeType = .integer32AttributeType
        rssiLastSeen.isOptional = true

        let ipAddress = NSAttributeDescription()
        ipAddress.name = "ipAddress"
        ipAddress.attributeType = .stringAttributeType
        ipAddress.isOptional = true

        let notesAttr = NSAttributeDescription()
        notesAttr.name = "notes"
        notesAttr.attributeType = .stringAttributeType
        notesAttr.isOptional = true

        entity.properties = [
            id, compositeKey, macAddress, deviceName, vendorAttr,
            detectionMethod, firstSeen, lastSeen, seenCount,
            isTrustedByUser, riskLevel, rssiLastSeen, ipAddress, notesAttr
        ]

        model.entities = [entity]

        let container = NSPersistentContainer(name: "DeviceLens", managedObjectModel: model)
        container.loadPersistentStores { _, error in
            if let error = error {
                fatalError("Core Data store failed to load: \(error.localizedDescription)")
            }
        }
        container.viewContext.automaticallyMergesChangesFromParent = true
        self.container = container
    }

    var viewContext: NSManagedObjectContext {
        container.viewContext
    }
}
