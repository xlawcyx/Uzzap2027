package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Forest palette: deep foliage, pale sage, and fresh leaf green.
// Balanced to retain at least 4.5:1 contrast against both white and black canvases.
val ForestGreen = Color(0xFF31844D)
val ForestGreenDark = Color(0xFF1F5B35)
val ForestLeaf = Color(0xFF70B65A)
val ForestSage = Color(0xFFE7F0DF)

// Compatibility names used throughout the existing UI now resolve to Forest colors.
val UzzapOrange = ForestGreen
val UzzapOrangeDark = ForestGreenDark
val UzzapOrangeLight = ForestLeaf
val UzzapOrangeContainer = ForestSage

val UzzapNavy = Color(0xFF0E1A12)
val UzzapNavyCard = Color(0xFF172A1D)
val UzzapNavySurface = Color(0xFF213927)
val UzzapNavyBorder = Color(0xFF3B5B43)

// Legacy accent name. Use the deeper green so white icon/text pairings stay accessible.
val UzzapCyan = ForestGreen
val UzzapCyanContainer = ForestSage

// Presence Colors
val PresenceOnline = ForestGreen
val PresenceAway = Color(0xFFF59E0B)
val PresenceBusy = Color(0xFFEF4444)
val PresenceOffline = Color(0xFF94A3B8)

// Neutral Canvas
val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = ForestSage
val TextPrimary = Color(0xFF17231A)
val TextSecondary = Color(0xFF536457)
val TextMuted = Color(0xFF647568)
val BorderLight = Color(0xFF7F9283)

// Dark surfaces stay neutral black while controls retain the Forest identity.
val BackgroundDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF000000)
val SurfaceVariantDark = Color(0xFF17231A)
val TextPrimaryDark = Color(0xFFF2F6F0)
val TextSecondaryDark = Color(0xFFC7D4C5)
val BorderDark = Color(0xFF8AA28D)
