package models

import "time"

// DeviceFingerprint is the input from the Android client.
type DeviceFingerprint struct {
	OuiPrefix           string   `json:"ouiPrefix"`
	OpenPorts           []int    `json:"openPorts"`
	HttpBanner          *string  `json:"httpBanner"`
	SsdpResponse        *string  `json:"ssdpResponse"`
	MdnsServices        []string `json:"mDnsServices"`
	BleManufacturerData *string  `json:"bleManufacturerData"`
	PageTitle           *string  `json:"pageTitle"`
	RespondsTuya        bool     `json:"respondsTuya"`
	RespondsXmeye       bool     `json:"respondsXmeye"`
}

// MatchResult is the full response from the matching engine.
type MatchResult struct {
	Match            *DeviceMatch  `json:"match"`
	CommunityReports CommunityData `json:"communityReports"`
	Vendor           *VendorInfo   `json:"vendor"`
}

type DeviceMatch struct {
	DeviceType     string  `json:"deviceType"`
	Category       string  `json:"category"`
	ThreatLevel    string  `json:"threatLevel"`
	Confidence     float64 `json:"confidence"`
	Description    string  `json:"description"`
	KnownModel     *string `json:"knownModel"`
	Recommendation string  `json:"recommendation"`
	SignatureName  string  `json:"signatureName"`
}

type CommunityData struct {
	TotalReports int            `json:"totalReports"`
	ClassifiedAs map[string]int `json:"classifiedAs"`
}

type VendorInfo struct {
	Name                 string `json:"name"`
	Category             string `json:"category"`
	ThreatWeight         int    `json:"threatWeight"`
	IsSurveillanceCommon bool   `json:"isSurveillanceCommon"`
}

// --- DB row types ---

type Signature struct {
	ID                int        `json:"id"`
	Name              string     `json:"name"`
	Description       *string    `json:"description"`
	Category          string     `json:"category"`
	ThreatLevel       string     `json:"threatLevel"`
	Confidence        float64    `json:"confidence"`
	OuiVendors        *string    `json:"-"`
	PortPatterns      *string    `json:"-"`
	BannerPatterns    *string    `json:"-"`
	ProtocolResponses *string    `json:"-"`
	MdnsServices      *string    `json:"-"`
	Source            string     `json:"source"`
	ReportCount       int        `json:"reportCount"`
	Version           int        `json:"version"`
	UpdatedAt         *time.Time `json:"updatedAt"`
	CreatedAt         *time.Time `json:"createdAt"`
}

type OuiEntry struct {
	Prefix               string  `json:"prefix"`
	Vendor               string  `json:"vendor"`
	Category             string  `json:"category"`
	ThreatWeight         int     `json:"threatWeight"`
	Aliases              *string `json:"aliases,omitempty"`
	IsSurveillanceCommon bool    `json:"isSurveillanceCommon"`
}

type CommunityReport struct {
	ID                 int        `json:"id"`
	FingerprintHash    string     `json:"fingerprintHash"`
	OuiPrefix          *string    `json:"ouiPrefix"`
	OpenPorts          *string    `json:"openPorts"`
	HttpBanner         *string    `json:"httpBanner"`
	UserClassification string     `json:"userClassification"`
	UserDescription    *string    `json:"userDescription"`
	ReportedAt         *time.Time `json:"reportedAt"`
	IpHash             *string    `json:"ipHash"`
	Verified           bool       `json:"verified"`
}

type KnownDevice struct {
	ID              int        `json:"id"`
	FingerprintHash string     `json:"fingerprintHash"`
	DeviceType      string     `json:"deviceType"`
	Brand           *string    `json:"brand"`
	Model           *string    `json:"model"`
	IsThreat        bool       `json:"isThreat"`
	ThreatCategory  *string    `json:"threatCategory"`
	Confidence      float64    `json:"confidence"`
	ReportCount     int        `json:"reportCount"`
	LastSeen        *time.Time `json:"lastSeen"`
}
