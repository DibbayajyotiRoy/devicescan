package models

import "time"

type ApiEvent struct {
	ID          int64     `json:"id"`
	UserID      *int      `json:"userId,omitempty"`
	Endpoint    string    `json:"endpoint"`
	Method      string    `json:"method"`
	StatusCode  int       `json:"statusCode"`
	LatencyMs   int       `json:"latencyMs"`
	Country     string    `json:"country,omitempty"`
	AppVersion  string    `json:"appVersion,omitempty"`
	OsVersion   string    `json:"osVersion,omitempty"`
	DeviceModel string    `json:"deviceModel,omitempty"`
	CreatedAt   time.Time `json:"createdAt"`
}

type ScanEvent struct {
	ID             int64     `json:"id"`
	UserID         *int      `json:"userId,omitempty"`
	DeviceCount    int       `json:"deviceCount"`
	ThreatCount    int       `json:"threatCount"`
	WarningCount   int       `json:"warningCount"`
	ScanDurationMs *int      `json:"scanDurationMs,omitempty"`
	AppVersion     string    `json:"appVersion,omitempty"`
	OsVersion      string    `json:"osVersion,omitempty"`
	DeviceModel    string    `json:"deviceModel,omitempty"`
	Country        string    `json:"country,omitempty"`
	CreatedAt      time.Time `json:"createdAt"`
}

type ScanTelemetryRequest struct {
	DeviceCount    int `json:"deviceCount"`
	ThreatCount    int `json:"threatCount"`
	WarningCount   int `json:"warningCount"`
	ScanDurationMs int `json:"scanDurationMs"`
}

// --- Admin dashboard types ---

type AdminDashboard struct {
	DAU                     int            `json:"dau"`
	MAU                     int            `json:"mau"`
	TotalUsers              int            `json:"totalUsers"`
	TotalScansToday         int            `json:"totalScansToday"`
	TotalScansAllTime       int            `json:"totalScansAllTime"`
	AvgDevicesPerScan       float64        `json:"avgDevicesPerScan"`
	AvgThreatsPerScan       float64        `json:"avgThreatsPerScan"`
	ActiveSignatures        int            `json:"activeSignatures"`
	CommunityReportsPending int            `json:"communityReportsPending"`
	TopCountries            []CountryStat  `json:"topCountries"`
	ThreatDistribution      map[string]int `json:"threatDistribution"`
	RequestsToday           int            `json:"requestsToday"`
}

type CountryStat struct {
	Country  string `json:"country"`
	Requests int    `json:"requests"`
}

type UsageTimeSeries struct {
	Date        string `json:"date"`
	Requests    int    `json:"requests"`
	UniqueUsers int    `json:"uniqueUsers"`
	Scans       int    `json:"scans"`
}

type DeviceAnalytics struct {
	Model string `json:"model"`
	Count int    `json:"count"`
}

type AdminUserEntry struct {
	ID          int       `json:"id"`
	Email       string    `json:"email"`
	Name        string    `json:"name"`
	PictureURL  *string   `json:"pictureUrl,omitempty"`
	Role        string    `json:"role"`
	CreatedAt   time.Time `json:"createdAt"`
	LastLoginAt time.Time `json:"lastLoginAt"`
	LoginCount  int       `json:"loginCount"`
	ScanCount   int       `json:"scanCount"`
	ReportCount int       `json:"reportCount"`
}
