import AVFoundation
import UIKit
import CoreVideo

final class IrDetector: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    private var captureSession: AVCaptureSession?
    private let brightSpotThreshold: UInt8 = 220
    var onBrightSpotsDetected: (([CGPoint]) -> Void)?

    func startAnalysis() {
        let session = AVCaptureSession()
        session.sessionPreset = .medium

        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let input = try? AVCaptureDeviceInput(device: device) else { return }

        if session.canAddInput(input) {
            session.addInput(input)
        }

        let output = AVCaptureVideoDataOutput()
        output.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_420YpCbCr8BiPlanarFullRange]
        output.setSampleBufferDelegate(self, queue: DispatchQueue(label: "ir.analysis"))

        if session.canAddOutput(output) {
            session.addOutput(output)
        }

        captureSession = session
        DispatchQueue.global(qos: .userInitiated).async {
            session.startRunning()
        }
    }

    func stopAnalysis() {
        captureSession?.stopRunning()
        captureSession = nil
    }

    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        CVPixelBufferLockBaseAddress(pixelBuffer, .readOnly)
        defer { CVPixelBufferUnlockBaseAddress(pixelBuffer, .readOnly) }

        let width = CVPixelBufferGetWidth(pixelBuffer)
        let height = CVPixelBufferGetHeight(pixelBuffer)
        guard let baseAddress = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0) else { return }

        let bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0)
        let luminancePlane = baseAddress.assumingMemoryBound(to: UInt8.self)

        var spots: [CGPoint] = []

        for y in stride(from: 0, to: height, by: 8) {
            for x in stride(from: 0, to: width, by: 8) {
                let index = y * bytesPerRow + x
                let luminance = luminancePlane[index]
                if luminance > brightSpotThreshold {
                    spots.append(CGPoint(
                        x: CGFloat(x) / CGFloat(width),
                        y: CGFloat(y) / CGFloat(height)
                    ))
                }
            }
        }

        DispatchQueue.main.async { [weak self] in
            self?.onBrightSpotsDetected?(spots)
        }
    }
}
