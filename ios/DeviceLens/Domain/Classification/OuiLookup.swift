import Foundation

final class OuiLookup {
    static let shared = OuiLookup()
    private var table: [String: String] = [:]

    private init() {
        loadTable()
    }

    private func loadTable() {
        guard let url = Bundle.main.url(forResource: "oui", withExtension: "txt"),
              let contents = try? String(contentsOf: url, encoding: .utf8) else { return }

        contents.enumerateLines { line, _ in
            let parts = line.split(separator: "\t", maxSplits: 1)
            if parts.count >= 2 {
                let prefix = String(parts[0]).trimmingCharacters(in: .whitespaces).uppercased()
                let vendor = String(parts[1]).trimmingCharacters(in: .whitespaces)
                self.table[prefix] = vendor
            }
        }
    }

    func lookup(mac: String) -> String {
        guard mac.count >= 8 else { return "Unknown" }
        let prefix = String(mac.prefix(8)).uppercased()
        return table[prefix] ?? "Unknown"
    }
}
