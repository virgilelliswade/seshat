package com.seshat.writer

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.model.BlockType
import com.seshat.writer.platform.AppPlatform
import com.seshat.writer.platform.DesktopPlatformStorage
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.ScreenplayEditorScreen
import com.seshat.writer.ui.theme.SeshatTheme
import com.seshat.writer.ui.theme.ThemeMode
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun main() = application {
    // Ensure Desktop platform storage is active
    AppPlatform.storage = DesktopPlatformStorage()

    val context = remember { Context() }
    val viewModel = remember { ScreenplayViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val isDark = when (uiState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> true
    }

    val windowState = rememberWindowState(width = 1380.dp, height = 860.dp)

    fun formatBlock(type: BlockType) {
        val blockId = uiState.focusedBlockId.ifBlank {
            uiState.blocks.firstOrNull()?.id ?: return
        }
        viewModel.changeBlockType(blockId, type)
    }

    fun handleImportScript() {
        try {
            val dialog = FileDialog(null as Frame?, "Import Screenplay (.fdx / .fountain)", FileDialog.LOAD)
            dialog.isVisible = true
            val dir = dialog.directory
            val name = dialog.file
            if (dir != null && name != null) {
                val file = File(dir, name)
                val result = ScriptImporter.importFromFile(file)
                if (result.isSuccess && result.document != null) {
                    val doc = result.document
                    viewModel.loadDocument(doc.blocks, doc.titlePage)
                    ScriptFileManager.saveScript(context, doc.blocks, doc.titlePage, "fountain")
                    AppPlatform.showToast("Imported '${doc.titlePage.title}' (${doc.blocks.size} blocks)")
                } else {
                    AppPlatform.showToast(result.errorMessage ?: "Failed to import script")
                }
            }
        } catch (_: Throwable) {}
    }

    fun handleExportBreakdown() {
        try {
            val file = viewModel.exportProductionBreakdownPdf(context)
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            }
            AppPlatform.showToast("Exported AD Breakdown: ${file.name}")
        } catch (e: Throwable) {
            AppPlatform.showToast("Export failed: ${e.message}")
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Seshat Studio - ${uiState.title.ifBlank { "Untitled Screenplay" }}",
        icon = androidx.compose.ui.res.painterResource("seshat_emblem_logo.png")
    ) {
        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item(
                    text = "New Screenplay / Templates...",
                    shortcut = KeyShortcut(Key.N, meta = true),
                    onClick = { viewModel.openStartMenu() }
                )
                Item(
                    text = "Open Script Library...",
                    shortcut = KeyShortcut(Key.O, meta = true),
                    onClick = { viewModel.openScriptLibrary() }
                )
                Item(
                    text = "Import Script (.fdx / .fountain)...",
                    shortcut = KeyShortcut(Key.I, meta = true),
                    onClick = { handleImportScript() }
                )
                Separator()
                Item(
                    text = "Save Project",
                    shortcut = KeyShortcut(Key.S, meta = true),
                    onClick = {
                        viewModel.saveCurrentProject(context)
                        AppPlatform.showToast("Project Saved Successfully")
                    }
                )
                Item(
                    text = "Export Script (PDF / FDX / Fountain)...",
                    shortcut = KeyShortcut(Key.E, meta = true),
                    onClick = { viewModel.openExportDialog() }
                )
                Item(
                    text = "Export Production AD Breakdown PDF...",
                    onClick = { handleExportBreakdown() }
                )
                Separator()
                Item(
                    text = "Title Page Setup...",
                    shortcut = KeyShortcut(Key.T, meta = true, shift = true),
                    onClick = { viewModel.openTitlePage() }
                )
                Separator()
                Item(
                    text = "Close Window",
                    shortcut = KeyShortcut(Key.W, meta = true),
                    onClick = ::exitApplication
                )
            }

            Menu("Edit", mnemonic = 'E') {
                Item(
                    text = "Find in Script...",
                    shortcut = KeyShortcut(Key.F, meta = true),
                    onClick = { viewModel.openFind(replaceMode = false) }
                )
                Item(
                    text = "Find and Replace...",
                    shortcut = KeyShortcut(Key.F, meta = true, alt = true),
                    onClick = { viewModel.openFind(replaceMode = true) }
                )
                Item(
                    text = "Global Character Rename...",
                    shortcut = KeyShortcut(Key.R, meta = true, shift = true),
                    onClick = { viewModel.openCharacterRename() }
                )
                Separator()
                Item(
                    text = "Production Revisions & Colored Drafts...",
                    onClick = { viewModel.openRevisionDialog() }
                )
            }

            Menu("Format", mnemonic = 'M') {
                Item(
                    text = "Scene Heading (Slugline)",
                    shortcut = KeyShortcut(Key.One, meta = true),
                    onClick = { formatBlock(BlockType.SCENE_HEADING) }
                )
                Item(
                    text = "Action",
                    shortcut = KeyShortcut(Key.Two, meta = true),
                    onClick = { formatBlock(BlockType.ACTION) }
                )
                Item(
                    text = "Character Name",
                    shortcut = KeyShortcut(Key.Three, meta = true),
                    onClick = { formatBlock(BlockType.CHARACTER) }
                )
                Item(
                    text = "Parenthetical",
                    shortcut = KeyShortcut(Key.Four, meta = true),
                    onClick = { formatBlock(BlockType.PARENTHETICAL) }
                )
                Item(
                    text = "Dialogue",
                    shortcut = KeyShortcut(Key.Five, meta = true),
                    onClick = { formatBlock(BlockType.DIALOGUE) }
                )
                Item(
                    text = "Transition",
                    shortcut = KeyShortcut(Key.Six, meta = true),
                    onClick = { formatBlock(BlockType.TRANSITION) }
                )
            }

            Menu("View", mnemonic = 'V') {
                Item(
                    text = "Story Bible & World Building",
                    shortcut = KeyShortcut(Key.B, meta = true),
                    onClick = { viewModel.openStoryBible(context) }
                )
                Item(
                    text = "Corkboard & Index Cards",
                    shortcut = KeyShortcut(Key.O, meta = true, shift = true),
                    onClick = { viewModel.toggleCorkboard(context) }
                )
                Item(
                    text = "Character & Scene Matrix Reports...",
                    onClick = { viewModel.openReportsDialog() }
                )
                Item(
                    text = "Compare Drafts (Diff Engine)...",
                    onClick = { viewModel.openDraftDiff() }
                )
                Item(
                    text = "Studio Navigation Drawer",
                    onClick = { viewModel.toggleStudioDrawer() }
                )
                Separator()
                Item(
                    text = "Toggle Zen Mode (Distraction-Free)",
                    shortcut = KeyShortcut(Key.F11),
                    onClick = { viewModel.toggleZenMode() }
                )
                CheckboxItem(
                    text = "Show Scene Numbers",
                    checked = uiState.showSceneNumbers,
                    onCheckedChange = { viewModel.toggleSceneNumbers() }
                )
                CheckboxItem(
                    text = "Show Writer Notes",
                    checked = uiState.showWriterNotes,
                    onCheckedChange = { viewModel.toggleWriterNotes() }
                )
                Separator()
                Item(
                    text = "Zoom In",
                    shortcut = KeyShortcut(Key.Equals, meta = true),
                    onClick = { viewModel.zoomIn() }
                )
                Item(
                    text = "Zoom Out",
                    shortcut = KeyShortcut(Key.Minus, meta = true),
                    onClick = { viewModel.zoomOut() }
                )
                Item(
                    text = "Actual Size (100%)",
                    shortcut = KeyShortcut(Key.Zero, meta = true),
                    onClick = { viewModel.setZoom(1.0f) }
                )
                Separator()
                Menu("Theme") {
                    RadioButtonItem(
                        text = "Dark (Obsidian)",
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(context, ThemeMode.DARK) }
                    )
                    RadioButtonItem(
                        text = "Light",
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(context, ThemeMode.LIGHT) }
                    )
                    RadioButtonItem(
                        text = "System Default",
                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(context, ThemeMode.SYSTEM) }
                    )
                }
            }

            Menu("Tools", mnemonic = 'T') {
                Item(
                    text = "AI Script Doctor & Analysis",
                    shortcut = KeyShortcut(Key.J, meta = true),
                    onClick = { viewModel.openScriptDoctorDialog() }
                )
                Item(
                    text = "Table Read (Voice Synthesis)",
                    shortcut = KeyShortcut(Key.R, meta = true, alt = true),
                    onClick = { viewModel.openTableRead(context) }
                )
                Item(
                    text = if (uiState.isDictating) "Stop Voice Dictation" else "Start Voice Dictation",
                    shortcut = KeyShortcut(Key.D, meta = true),
                    onClick = {
                        if (uiState.isDictating) viewModel.cancelDictation() else viewModel.startDictation(context)
                    }
                )
                Separator()
                Item(
                    text = "Novel & Manuscript Compiler...",
                    onClick = { viewModel.openNovelCompiler() }
                )
                Item(
                    text = "Novel Book Cover Creator...",
                    onClick = { viewModel.openCoverCreator() }
                )
                Item(
                    text = "Writers' Room (Live Collaboration)...",
                    onClick = { viewModel.openWritersRoom() }
                )
                Separator()
                Item(
                    text = "Preferences & AI Settings...",
                    shortcut = KeyShortcut(Key.Comma, meta = true),
                    onClick = { viewModel.openSettingsDialog() }
                )
            }

            Menu("Window", mnemonic = 'W') {
                Item(
                    text = "Minimize",
                    shortcut = KeyShortcut(Key.M, meta = true),
                    onClick = { windowState.isMinimized = true }
                )
                Item(
                    text = if (windowState.placement == WindowPlacement.Fullscreen) "Exit Full Screen" else "Enter Full Screen",
                    shortcut = KeyShortcut(Key.F, meta = true, ctrl = true),
                    onClick = {
                        windowState.placement = if (windowState.placement == WindowPlacement.Fullscreen) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Fullscreen
                        }
                    }
                )
            }

            Menu("Help", mnemonic = 'H') {
                Item(
                    text = "Keyboard Shortcuts Reference...",
                    shortcut = KeyShortcut(Key.Slash, meta = true),
                    onClick = { viewModel.openShortcutsDialog() }
                )
                Separator()
                Item(
                    text = "About Seshat Writing Studio...",
                    onClick = { viewModel.openAboutDialog() }
                )
            }
        }

        SeshatTheme(darkTheme = isDark) {
            ScreenplayEditorScreen(
                viewModel = viewModel,
                onOpenTitlePage = { viewModel.openTitlePage() }
            )
        }
    }
}
