package com.seshat.writer.ui.theme

import com.seshat.writer.screenplay.ScreenplayUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeTest {

    @Test
    fun testThemeModeEnumValuesAndDisplayNames() {
        assertEquals("Dark Mode", ThemeMode.DARK.displayName)
        assertEquals("🌙", ThemeMode.DARK.icon)

        assertEquals("Light Mode", ThemeMode.LIGHT.displayName)
        assertEquals("☀️", ThemeMode.LIGHT.icon)

        assertEquals("System Default", ThemeMode.SYSTEM.displayName)
        assertEquals("⚙️", ThemeMode.SYSTEM.icon)

        val values = ThemeMode.values()
        assertEquals(3, values.size)
    }

    @Test
    fun testDefaultUiStateHasDarkTheme() {
        val state = ScreenplayUiState()
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(StudioColorPalette.SCRIBESTAR_CRIMSON, state.studioPalette)
    }

    @Test
    fun testStudioColorPaletteValues() {
        val palettes = StudioColorPalette.values()
        assertEquals(6, palettes.size)
        assertEquals("ScribeStar Crimson", StudioColorPalette.SCRIBESTAR_CRIMSON.displayName)
        assertEquals("Swiss Cobalt", StudioColorPalette.SWISS_COBALT.displayName)
        assertEquals("Warm Atelier", StudioColorPalette.WARM_ATELIER.displayName)
        assertEquals("Nocturne Emerald", StudioColorPalette.NOCTURNE_EMERALD.displayName)
        assertEquals("Astral Amethyst", StudioColorPalette.ASTRAL_VIOLET.displayName)
        assertEquals("Monolith Titanium", StudioColorPalette.MONOLITH_TITANIUM.displayName)
    }

    @Test
    fun testScribeStarAndNeonPaletteValues() {
        assertNotNull(ScribeStarNeonRed)
        assertNotNull(ScribeStarCrimson)
        assertNotNull(NeonCoralLight)
        assertNotNull(RubyCoreDark)
        assertNotNull(ElectricWhite)
        assertNotNull(CircuitIce)
        assertNotNull(CircuitCyan)
        assertNotNull(CircuitSlate)

        assertNotNull(RawObsidianBlack)
        assertNotNull(RawObsidianSurface)
        assertNotNull(RawObsidianSurfaceVariant)
        assertNotNull(RawScreenplayTextDark)
        assertNotNull(RawScreenplayTextMuted)
        assertNotNull(RawPapyrusLight)
        assertNotNull(RawPapyrusDark)
        assertNotNull(ElectrumGold)
        assertNotNull(LightGold)
        assertNotNull(DarkGold)
        assertNotNull(LapisLazuli)
        assertNotNull(PapyrusLight)
        assertNotNull(PapyrusDark)
        assertNotNull(RawLightDeskBackground)

        assertNotNull(MontserratFontFamily)
        assertNotNull(MainAppFontFamily)
    }
}
