package engine

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"math"
	"sort"
	"strings"

	"devicelens-backend/internal/models"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Matcher struct {
	pool *pgxpool.Pool
}

func NewMatcher(pool *pgxpool.Pool) *Matcher {
	return &Matcher{pool: pool}
}

// HashFingerprint produces a SHA-256 hash of the normalized fingerprint.
// Must match the TypeScript implementation exactly for cross-compatibility.
func HashFingerprint(fp models.DeviceFingerprint) string {
	oui := strings.ToUpper(fp.OuiPrefix)
	if len(oui) > 8 {
		oui = oui[:8]
	}

	ports := make([]int, len(fp.OpenPorts))
	copy(ports, fp.OpenPorts)
	sort.Ints(ports)

	banner := ""
	if fp.HttpBanner != nil {
		banner = strings.ToLower(strings.TrimSpace(*fp.HttpBanner))
	}

	// Must match: JSON.stringify({oui, ports: [...].sort(), banner: (banner||'').toLowerCase().trim()})
	portsJSON, _ := json.Marshal(ports)
	normalized := fmt.Sprintf(`{"oui":"%s","ports":%s,"banner":"%s"}`, oui, string(portsJSON), banner)

	hash := sha256.Sum256([]byte(normalized))
	return fmt.Sprintf("%x", hash)
}

// MatchFingerprint runs the full matching pipeline against a device fingerprint.
func (m *Matcher) MatchFingerprint(ctx context.Context, fp models.DeviceFingerprint) (*models.MatchResult, error) {
	fpHash := HashFingerprint(fp)

	// 1. Check known devices (exact hash match = instant answer)
	known, err := m.getKnownDevice(ctx, fpHash)
	if err == nil && known != nil {
		communityData := m.getCommunityData(ctx, fpHash)
		vendorData := m.getVendorData(ctx, fp.OuiPrefix)

		threatLevel := "LOW"
		if known.IsThreat {
			threatLevel = "HIGH"
		}

		desc := "Verified device"
		if known.Brand != nil || known.Model != nil {
			parts := []string{}
			if known.Brand != nil {
				parts = append(parts, *known.Brand)
			}
			if known.Model != nil {
				parts = append(parts, *known.Model)
			}
			desc = "Verified device: " + strings.Join(parts, " ")
		}

		rec := "This device has been verified by the community."
		if known.IsThreat {
			rec = "This device has been verified as a surveillance device. Investigate immediately."
		}

		return &models.MatchResult{
			Match: &models.DeviceMatch{
				DeviceType:     known.DeviceType,
				Category:       ptrOr(known.ThreatCategory, "UNKNOWN"),
				ThreatLevel:    threatLevel,
				Confidence:     known.Confidence,
				Description:    desc,
				KnownModel:     known.Model,
				Recommendation: rec,
				SignatureName:  "known_device_match",
			},
			CommunityReports: communityData,
			Vendor:           vendorData,
		}, nil
	}

	// 2. Match against signatures (rule-based)
	sigs, err := m.getAllSignatures(ctx)
	if err != nil {
		return nil, fmt.Errorf("query signatures: %w", err)
	}

	var bestMatch *models.Signature
	bestScore := 0.0
	for i := range sigs {
		score := scoreSignature(&sigs[i], &fp)
		if score > bestScore && score >= 0.3 {
			bestScore = score
			bestMatch = &sigs[i]
		}
	}

	communityData := m.getCommunityData(ctx, fpHash)
	vendorData := m.getVendorData(ctx, fp.OuiPrefix)

	if bestMatch != nil {
		finalConfidence := bestMatch.Confidence * bestScore * (1 + float64(communityData.TotalReports)*0.01)
		if finalConfidence > 0.99 {
			finalConfidence = 0.99
		}
		finalConfidence = math.Round(finalConfidence*100) / 100

		desc := ""
		if bestMatch.Description != nil {
			desc = *bestMatch.Description
		}

		return &models.MatchResult{
			Match: &models.DeviceMatch{
				DeviceType:     bestMatch.Name,
				Category:       bestMatch.Category,
				ThreatLevel:    bestMatch.ThreatLevel,
				Confidence:     finalConfidence,
				Description:    desc,
				KnownModel:     nil,
				Recommendation: getRecommendation(bestMatch.ThreatLevel, bestMatch.Category),
				SignatureName:  bestMatch.Name,
			},
			CommunityReports: communityData,
			Vendor:           vendorData,
		}, nil
	}

	// 3. No signature match — check if vendor itself is suspicious
	if vendorData != nil && vendorData.ThreatWeight >= 50 {
		threatLevel := "MEDIUM"
		if vendorData.ThreatWeight >= 80 {
			threatLevel = "HIGH"
		}
		confidence := math.Round(float64(vendorData.ThreatWeight)/100*0.7*100) / 100

		rec := "This device uses an IoT chipset. Verify it belongs to you."
		if vendorData.IsSurveillanceCommon {
			rec = "This chipset is commonly used in surveillance cameras. If you don't recognize this device, investigate."
		}

		return &models.MatchResult{
			Match: &models.DeviceMatch{
				DeviceType:     fmt.Sprintf("Unknown %s Device", vendorData.Name),
				Category:       vendorData.Category,
				ThreatLevel:    threatLevel,
				Confidence:     confidence,
				Description:    fmt.Sprintf("This device uses a %s chipset, which is commonly found in IoT and surveillance devices.", vendorData.Name),
				KnownModel:     nil,
				Recommendation: rec,
				SignatureName:  "oui_vendor_match",
			},
			CommunityReports: communityData,
			Vendor:           vendorData,
		}, nil
	}

	return &models.MatchResult{
		Match:            nil,
		CommunityReports: communityData,
		Vendor:           vendorData,
	}, nil
}

// --- Signature scoring ---

func scoreSignature(sig *models.Signature, fp *models.DeviceFingerprint) float64 {
	score := 0.0
	maxScore := 0.0

	// 1. OUI vendor match (weight: 3)
	if sig.OuiVendors != nil {
		var vendors []string
		json.Unmarshal([]byte(*sig.OuiVendors), &vendors)
		if len(vendors) > 0 {
			maxScore += 3
		}
	}

	// 2. Port patterns (weight: 2 required, 2 suspicious)
	if sig.PortPatterns != nil {
		var patterns struct {
			Required   []int `json:"required"`
			Suspicious []int `json:"suspicious"`
		}
		json.Unmarshal([]byte(*sig.PortPatterns), &patterns)

		if len(patterns.Required) > 0 {
			maxScore += 2
			allRequired := true
			for _, p := range patterns.Required {
				if !containsInt(fp.OpenPorts, p) {
					allRequired = false
					break
				}
			}
			if allRequired {
				score += 2
			} else {
				return 0 // Required ports are mandatory
			}
		}

		if len(patterns.Suspicious) > 0 {
			maxScore += 2
			matched := 0
			for _, p := range patterns.Suspicious {
				if containsInt(fp.OpenPorts, p) {
					matched++
				}
			}
			score += math.Min(float64(matched)*0.5, 2)
		}
	}

	// 3. Banner patterns (weight: 3 — strongest signal)
	if sig.BannerPatterns != nil {
		var patterns []string
		json.Unmarshal([]byte(*sig.BannerPatterns), &patterns)
		if len(patterns) > 0 {
			maxScore += 3
			banner := ""
			if fp.HttpBanner != nil {
				banner = strings.ToLower(*fp.HttpBanner)
			}
			pageTitle := ""
			if fp.PageTitle != nil {
				pageTitle = strings.ToLower(*fp.PageTitle)
			}
			combined := banner + " " + pageTitle

			for _, p := range patterns {
				if strings.Contains(combined, strings.ToLower(p)) {
					score += 3
					break
				}
			}
		}
	}

	// 4. Protocol responses (weight: 2)
	if sig.ProtocolResponses != nil {
		var responses map[string]bool
		json.Unmarshal([]byte(*sig.ProtocolResponses), &responses)
		if len(responses) > 0 {
			maxScore += 2
			if responses["tuya_udp"] && fp.RespondsTuya {
				score += 2
			}
			if responses["xmeye_udp"] && fp.RespondsXmeye {
				score += 2
			}
		}
	}

	// 5. mDNS services (weight: 2)
	if sig.MdnsServices != nil {
		var services []string
		json.Unmarshal([]byte(*sig.MdnsServices), &services)
		if len(services) > 0 {
			maxScore += 2
			for _, s := range services {
				for _, m := range fp.MdnsServices {
					if strings.Contains(m, s) {
						score += 2
						goto donemdns
					}
				}
			}
		donemdns:
		}
	}

	if maxScore == 0 {
		return 0
	}
	return score / maxScore
}

// --- DB helpers ---

func (m *Matcher) getKnownDevice(ctx context.Context, fpHash string) (*models.KnownDevice, error) {
	var kd models.KnownDevice
	err := m.pool.QueryRow(ctx,
		`SELECT id, fingerprint_hash, device_type, brand, model, is_threat, threat_category, confidence, report_count, last_seen
		 FROM known_devices WHERE fingerprint_hash = $1 LIMIT 1`, fpHash,
	).Scan(&kd.ID, &kd.FingerprintHash, &kd.DeviceType, &kd.Brand, &kd.Model,
		&kd.IsThreat, &kd.ThreatCategory, &kd.Confidence, &kd.ReportCount, &kd.LastSeen)
	if err == pgx.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return &kd, nil
}

func (m *Matcher) getAllSignatures(ctx context.Context) ([]models.Signature, error) {
	rows, err := m.pool.Query(ctx,
		`SELECT id, name, description, category, threat_level, confidence,
		        oui_vendors, port_patterns, banner_patterns, protocol_responses, mdns_services,
		        source, report_count, version, updated_at, created_at
		 FROM signatures`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var sigs []models.Signature
	for rows.Next() {
		var s models.Signature
		if err := rows.Scan(&s.ID, &s.Name, &s.Description, &s.Category, &s.ThreatLevel, &s.Confidence,
			&s.OuiVendors, &s.PortPatterns, &s.BannerPatterns, &s.ProtocolResponses, &s.MdnsServices,
			&s.Source, &s.ReportCount, &s.Version, &s.UpdatedAt, &s.CreatedAt); err != nil {
			continue
		}
		sigs = append(sigs, s)
	}
	return sigs, nil
}

func (m *Matcher) getCommunityData(ctx context.Context, fpHash string) models.CommunityData {
	rows, err := m.pool.Query(ctx,
		`SELECT user_classification FROM community_reports WHERE fingerprint_hash = $1`, fpHash)
	if err != nil {
		return models.CommunityData{TotalReports: 0, ClassifiedAs: map[string]int{}}
	}
	defer rows.Close()

	classifiedAs := map[string]int{}
	total := 0
	for rows.Next() {
		var cls string
		if rows.Scan(&cls) == nil {
			classifiedAs[cls]++
			total++
		}
	}
	return models.CommunityData{TotalReports: total, ClassifiedAs: classifiedAs}
}

func (m *Matcher) getVendorData(ctx context.Context, ouiPrefix string) *models.VendorInfo {
	normalized := strings.ToUpper(ouiPrefix)
	if len(normalized) > 8 {
		normalized = normalized[:8]
	}

	var v models.OuiEntry
	err := m.pool.QueryRow(ctx,
		`SELECT prefix, vendor, category, threat_weight, is_surveillance_common
		 FROM oui_database WHERE prefix = $1 LIMIT 1`, normalized,
	).Scan(&v.Prefix, &v.Vendor, &v.Category, &v.ThreatWeight, &v.IsSurveillanceCommon)
	if err != nil {
		return nil
	}

	return &models.VendorInfo{
		Name:                 v.Vendor,
		Category:             v.Category,
		ThreatWeight:         v.ThreatWeight,
		IsSurveillanceCommon: v.IsSurveillanceCommon,
	}
}

// --- Utility helpers ---

func getRecommendation(threatLevel, category string) string {
	if category == "CAMERA" || category == "SURVEILLANCE" {
		switch threatLevel {
		case "CRITICAL":
			return "This device is almost certainly a surveillance camera. If you did not install it, take immediate action: disconnect it from the network and investigate its physical location."
		case "HIGH":
			return "This device has strong camera/surveillance characteristics. If you don't recognize it, investigate immediately."
		case "MEDIUM":
			return "This device may be a camera or IoT device. Verify it belongs to you or someone you trust."
		default:
			return "This device has some characteristics of surveillance equipment. Monitor it."
		}
	}
	if category == "IOT" {
		return "This is an IoT device. If you don't recognize it, it could be a smart home device someone else placed, or a hidden surveillance device."
	}
	return "Unable to determine the exact nature of this device. If you don't recognize it, investigate."
}

func ptrOr(p *string, fallback string) string {
	if p != nil {
		return *p
	}
	return fallback
}

func containsInt(slice []int, val int) bool {
	for _, v := range slice {
		if v == val {
			return true
		}
	}
	return false
}
