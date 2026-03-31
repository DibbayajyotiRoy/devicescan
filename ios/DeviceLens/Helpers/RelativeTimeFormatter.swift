import Foundation

struct RelativeTimeFormatter {
    func format(_ date: Date) -> String {
        let diff = Date().timeIntervalSince(date)

        switch diff {
        case ..<60:
            return "just now"
        case ..<3600:
            let mins = Int(diff / 60)
            return mins == 1 ? "1 minute ago" : "\(mins) minutes ago"
        case ..<86400:
            let hours = Int(diff / 3600)
            return hours == 1 ? "1 hour ago" : "\(hours) hours ago"
        case ..<172800:
            return "yesterday"
        case ..<604800:
            let days = Int(diff / 86400)
            return "\(days) days ago"
        case ..<2592000:
            let weeks = Int(diff / 604800)
            return weeks == 1 ? "1 week ago" : "\(weeks) weeks ago"
        default:
            let months = Int(diff / 2592000)
            return months == 1 ? "1 month ago" : "\(months) months ago"
        }
    }
}
