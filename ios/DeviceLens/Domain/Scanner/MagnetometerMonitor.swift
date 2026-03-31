import Foundation
import CoreMotion

final class MagnetometerMonitor {
    struct MagnetometerReading {
        let baselineMagnitude: Double
        let peakMagnitude: Double
        let anomalyDetected: Bool
    }

    private let motionManager = CMMotionManager()

    func sample(duration: TimeInterval = 3.0) async -> MagnetometerReading {
        guard motionManager.isMagnetometerAvailable else {
            return MagnetometerReading(baselineMagnitude: 0, peakMagnitude: 0, anomalyDetected: false)
        }

        return await withCheckedContinuation { continuation in
            var readings: [Double] = []

            motionManager.magnetometerUpdateInterval = 0.05
            motionManager.startMagnetometerUpdates(to: .main) { data, _ in
                guard let data = data else { return }
                let magnitude = sqrt(
                    data.magneticField.x * data.magneticField.x +
                    data.magneticField.y * data.magneticField.y +
                    data.magneticField.z * data.magneticField.z
                )
                readings.append(magnitude)
            }

            DispatchQueue.global().asyncAfter(deadline: .now() + duration) { [weak self] in
                self?.motionManager.stopMagnetometerUpdates()

                let baseline = readings.isEmpty ? 0 : readings.reduce(0, +) / Double(readings.count)
                let peak = readings.max() ?? 0

                continuation.resume(returning: MagnetometerReading(
                    baselineMagnitude: baseline,
                    peakMagnitude: peak,
                    anomalyDetected: peak > 80
                ))
            }
        }
    }
}
