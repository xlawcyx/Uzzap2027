package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForestThemeTest {
    @Test
    fun lightUsesWhiteAndDarkUsesBlackForCanvasAndSurfaces() {
        assertEquals(Color.White, LightForestColorScheme.background)
        assertEquals(Color.White, LightForestColorScheme.surface)
        assertEquals(Color.Black, DarkForestColorScheme.background)
        assertEquals(Color.Black, DarkForestColorScheme.surface)
    }

    @Test
    fun bothThemeContentPairsMeetNormalTextContrast() {
        assertSchemeContrast("light", LightForestColorScheme)
        assertSchemeContrast("dark", DarkForestColorScheme)
    }

    @Test
    fun sharedForestAccentIsReadableOnBothCanvases() {
        assertContrast("Forest accent on white", Color.White, ForestGreen)
        assertContrast("Forest accent on black", Color.Black, ForestGreen)
        assertContrast("light outline", Color.White, BorderLight, minimum = 3.0)
        assertContrast("dark outline", Color.Black, BorderDark, minimum = 3.0)
    }

    private fun assertSchemeContrast(label: String, scheme: ColorScheme) {
        assertContrast("$label primary", scheme.primary, scheme.onPrimary)
        assertContrast("$label secondary", scheme.secondary, scheme.onSecondary)
        assertContrast("$label primary container", scheme.primaryContainer, scheme.onPrimaryContainer)
        assertContrast("$label secondary container", scheme.secondaryContainer, scheme.onSecondaryContainer)
        assertContrast("$label background", scheme.background, scheme.onBackground)
        assertContrast("$label surface", scheme.surface, scheme.onSurface)
        assertContrast("$label surface variant", scheme.surfaceVariant, scheme.onSurfaceVariant)
    }

    private fun assertContrast(
        label: String,
        first: Color,
        second: Color,
        minimum: Double = 4.5
    ) {
        val ratio = contrastRatio(first, second)
        assertTrue("$label contrast was $ratio; expected at least $minimum", ratio >= minimum)
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(relativeLuminance(first), relativeLuminance(second))
        val darker = minOf(relativeLuminance(first), relativeLuminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(component: Float): Double {
            val value = component.toDouble()
            return if (value <= 0.03928) value / 12.92
            else Math.pow((value + 0.055) / 1.055, 2.4)
        }

        return 0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)
    }
}
