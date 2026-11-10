package com.seshat.writer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalSeshatDarkTheme = staticCompositionLocalOf { true }
val LocalStudioPalette = staticCompositionLocalOf { StudioColorPalette.SCRIBESTAR_CRIMSON }

// =========================================================================
// DYNAMIC STUDIO PALETTE & PRECISION ARCHITECTURE
// ScribeStar Crimson default, plus Swiss Cobalt, Warm Atelier, Nocturne Emerald,
// Astral Amethyst, and Monolith Titanium.
// =========================================================================

// Core Dynamic Brand Accents (Backed by Compose Snapshot State for universal access)
val ScribeStarNeonRed: Color
    get() = ActiveStudioPaletteState.value.darkAccent

val ScribeStarCrimson: Color
    get() = ActiveStudioPaletteState.value.lightAccent

val NeonCoralLight: Color
    get() = ActiveStudioPaletteState.value.secondaryAccent

val RubyCoreDark: Color
    get() = ActiveStudioPaletteState.value.lightAccent.copy(alpha = 0.65f)

val ElectricWhite = Color(0xFFFFFFFF)
val CircuitIce = Color(0xFFF1F5F9)
val CircuitCyan = Color(0xFF38BDF8)
val CircuitSlate = Color(0xFF1E293B)

// Raw Dark Mode Surfaces (Dynamic per selected Studio Palette)
val RawObsidianBlack: Color
    get() = ActiveStudioPaletteState.value.darkBg

val RawObsidianSurface: Color
    get() = ActiveStudioPaletteState.value.darkSurface

val RawObsidianSurfaceVariant: Color
    get() = ActiveStudioPaletteState.value.darkBorder

val RawScreenplayTextDark = Color(0xFFF8FAFC)
val RawScreenplayTextMuted = Color(0xFF94A3B8)

// Raw Light Mode Surfaces (Clean Architectural Alabaster & Slate)
val RawPapyrusLight: Color
    get() = ActiveStudioPaletteState.value.lightBg

val RawPapyrusDark: Color
    get() = ActiveStudioPaletteState.value.lightBorder

val RawLightBackground: Color
    get() = ActiveStudioPaletteState.value.lightBg

val RawLightSurface: Color
    get() = ActiveStudioPaletteState.value.lightSurface

val RawLightSurfaceVariant: Color
    get() = ActiveStudioPaletteState.value.lightBorder

val RawScreenplayTextLight = Color(0xFF0F172A)
val RawScreenplayTextMutedLight = Color(0xFF475569)

// Always-White Physical Writing Page Surface (Used in both Dark Mode and Light Mode)
val StudioPaperWhite = Color(0xFFFFFFFF)
val StudioPaperInk = Color(0xFF0F172A)
val StudioPaperBorder = Color(0xFFCBD5E1)

// Semantic tokens mapped to calm studio slate (90/10 neutral discipline: reserve accent for 10% active selections)
val ElectrumGold: Color
    get() = if (ActiveDarkThemeState.value) Color(0xFFCBD5E1) else Color(0xFF334155)

val LightGold: Color
    get() = if (ActiveDarkThemeState.value) Color(0xFFF1F5F9) else Color(0xFF0F172A)

val DarkGold: Color
    get() = if (ActiveDarkThemeState.value) Color(0xFF94A3B8) else Color(0xFF475569)

val LapisLazuli = CircuitCyan
val LapisLight = Color(0xFF7DD3FC)
val LapisDark = Color(0xFF0284C7)

val PapyrusLight: Color
    get() = RawPapyrusLight

val PapyrusDark: Color
    get() = RawPapyrusDark

val SluglineBlue: Color
    get() = ActiveStudioPaletteState.value.secondaryAccent

val CharacterGold: Color
    get() = ActiveStudioPaletteState.value.darkAccent

val TransitionPurple = Color(0xFFC084FC)

// Dynamic brand accents adapting to Dark Mode vs Light Mode & Active Palette
val BrandAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) LocalStudioPalette.current.darkAccent else LocalStudioPalette.current.lightAccent

val BrandAccentSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) ElectricWhite else Color(0xFF0F172A)

// Dynamic semantic colors adapting to Dark Mode vs Light Mode & Active Palette
val ObsidianBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) LocalStudioPalette.current.darkBg else LocalStudioPalette.current.lightBg

val ObsidianSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) LocalStudioPalette.current.darkSurface else LocalStudioPalette.current.lightSurface

val ObsidianSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) LocalStudioPalette.current.darkBorder else LocalStudioPalette.current.lightBorder

val ScreenplayTextDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawScreenplayTextDark else RawScreenplayTextLight

val ScreenplayTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawScreenplayTextMuted else RawScreenplayTextMutedLight

val RawLightDeskBackground: Color
    get() = ActiveStudioPaletteState.value.lightDesk

val DeskBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) LocalStudioPalette.current.darkBg else LocalStudioPalette.current.lightDesk
