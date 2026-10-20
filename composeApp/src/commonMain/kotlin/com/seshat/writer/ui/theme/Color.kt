package com.seshat.writer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalSeshatDarkTheme = staticCompositionLocalOf { true }

// =========================================================================
// SCRIBESTAR & NEON GODDESS SESHAT PALETTE
// Directly color-matched to the neon Goddess Seshat artwork & ScribeStar Media logo
// =========================================================================

// Core Luminous Neon & Crimson Accents
val ScribeStarNeonRed = Color(0xFFFF2A3D)       // Brilliant Glowing Neon Scarlet / Star Aura
val ScribeStarCrimson = Color(0xFFC8102E)       // Deep Prestigious ScribeStar Crimson (Light Mode)
val NeonCoralLight = Color(0xFFFF5C6E)          // Luminous Star Flare & Glow
val RubyCoreDark = Color(0xFF8B0015)            // Deep Ruby Star Center
val ElectricWhite = Color(0xFFFFFFFF)           // Crisp Glowing Neon White (Palm Frond, Staff, Ankh)
val CircuitIce = Color(0xFFF1F5F9)              // Electric Ice Highlight
val CircuitCyan = Color(0xFF38BDF8)             // Microchip Circuit Blue Accent
val CircuitSlate = Color(0xFF1E293B)            // Circuit Slate Fill

// Raw Dark Mode Surfaces (Midnight Obsidian & Circuit Board)
val RawObsidianBlack = Color(0xFF070A0F)        // Deep Midnight Circuit Pitch
val RawObsidianSurface = Color(0xFF0E131C)      // Circuit Slate Surface
val RawObsidianSurfaceVariant = Color(0xFF18202D) // Circuit Panel Border & Divider
val RawScreenplayTextDark = Color(0xFFFFFFFF)   // Crisp Pure White Starlight Text
val RawScreenplayTextMuted = Color(0xFFCBD5E1)  // Luminous Soft Silver (Slate 200 - High Contrast Legibility)

// Raw Light Mode Surfaces (Clean Slate & Carbon Reed Ink)
val RawPapyrusLight = Color(0xFFF8FAFC)        // Clean Circuit Canvas
val RawPapyrusDark = Color(0xFFE2E8F0)         // Soft Slate Parchment
val RawLightBackground = Color(0xFFF8FAFC)     // Crisp Light Background
val RawLightSurface = Color(0xFFFFFFFF)        // Pure White Card
val RawLightSurfaceVariant = Color(0xFFE2E8F0) // Subtle Divider
val RawScreenplayTextLight = Color(0xFF0F172A) // Deep Carbon Ink
val RawScreenplayTextMutedLight = Color(0xFF5A6678) // Circuit Slate Muted

// Backward-compatible tokens mapped to the ScribeStar aesthetic
// All existing UI components across the app seamlessly adopt the ScribeStar palette
val ElectrumGold = ScribeStarNeonRed            // ScribeStar Neon Red accent
val LightGold = NeonCoralLight                 // ScribeStar Star Flare
val DarkGold = RubyCoreDark                    // ScribeStar Deep Ruby

val LapisLazuli = CircuitCyan
val LapisLight = Color(0xFF7DD3FC)
val LapisDark = Color(0xFF0284C7)

val PapyrusLight = RawPapyrusLight
val PapyrusDark = RawPapyrusDark

val SluglineBlue = CircuitCyan                  // Circuit Electric Cyan
val CharacterGold = ScribeStarNeonRed           // Neon Scarlet Character Heading
val TransitionPurple = Color(0xFFC084FC)

// Dynamic brand accents adapting to Dark Mode vs Light Mode
val BrandAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) ScribeStarNeonRed else ScribeStarCrimson

val BrandAccentSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) ElectricWhite else Color(0xFF0F172A)

// Dynamic semantic colors adapting to Dark Mode vs Light Mode
val ObsidianBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawObsidianBlack else RawLightBackground

val ObsidianSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawObsidianSurface else RawLightSurface

val ObsidianSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawObsidianSurfaceVariant else RawLightSurfaceVariant

val ScreenplayTextDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawScreenplayTextDark else RawScreenplayTextLight

val ScreenplayTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) RawScreenplayTextMuted else RawScreenplayTextMutedLight

val RawLightDeskBackground = Color(0xFFCBD5E1) // Sophisticated contrast slate behind writing surfaces in Light Mode

val DeskBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalSeshatDarkTheme.current) Color(0xFF05070B) else RawLightDeskBackground
