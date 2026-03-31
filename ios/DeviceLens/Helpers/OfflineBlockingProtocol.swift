import Foundation

final class OfflineBlockingProtocol: URLProtocol {
    override class func canInit(with request: URLRequest) -> Bool { true }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    override func startLoading() {
        assertionFailure(
            "⛔ Device Lens made a network call to \(request.url?.absoluteString ?? "unknown"). " +
            "This violates the offline contract."
        )
    }

    override func stopLoading() {}
}
