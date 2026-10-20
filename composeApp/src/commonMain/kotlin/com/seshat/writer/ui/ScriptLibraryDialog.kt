package com.seshat.writer.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScriptLibraryManager
import com.seshat.writer.library.ScriptProject
import com.seshat.writer.library.ScriptTemplate
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptLibraryDialog(
    viewModel: ScreenplayViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val projects = remember(refreshTrigger, uiState.title) {
        ScriptLibraryManager.listProjects(context, uiState.title)
    }

    // Modal Sub-dialogs
    var showNewTemplateDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<ScriptProject?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    var projectToDuplicate by remember { mutableStateOf<ScriptProject?>(null) }
    var duplicateInputText by remember { mutableStateOf("") }

    var projectToDelete by remember { mutableStateOf<ScriptProject?>(null) }

    val handleFileImport: (File?) -> Unit = { file ->
        if (file != null) {
            val importResult = ScriptImporter.importFromFile(file)
            if (importResult.isSuccess && importResult.document != null) {
                val doc = importResult.document
                viewModel.loadDocument(doc.blocks, doc.titlePage)
                // Also save a copy in scripts directory for persistence
                ScriptFileManager.saveScript(context, doc.blocks, doc.titlePage, "fountain")
                refreshTrigger++
                Toast.makeText(context, "Loaded '${doc.titlePage.title}' (${doc.blocks.size} blocks)", Toast.LENGTH_LONG).show()
                onDismiss()
            } else {
                Toast.makeText(context, importResult.errorMessage ?: "Failed to import script", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Top Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "📁 SCRIPT LIBRARY & PROJECTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ElectrumGold
                            )
                        )
                        Text(
                            text = "Manage screenplay projects, fork drafts & import Final Draft / Fountain files",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Import Button
                        OutlinedButton(
                            onClick = {
                                try {
                                    val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Import Screenplay (.fdx / .fountain)", java.awt.FileDialog.LOAD)
                                    dialog.isVisible = true
                                    val dir = dialog.directory
                                    val name = dialog.file
                                    if (dir != null && name != null) {
                                        handleFileImport(File(dir, name))
                                    }
                                } catch (_: Throwable) {}
                            },
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("📥 Import File (.fdx / .fountain)", style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // New Screenplay Button
                        Button(
                            onClick = { showNewTemplateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F1216), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Screenplay", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF0F1216), fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar & Filter Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianBlack)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 13.sp),
                        cursorBrush = SolidColor(ElectrumGold),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search screenplays by title or author...", color = ScreenplayTextMuted, fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = ScreenplayTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filtered List
                val filteredProjects = remember(projects, searchQuery) {
                    if (searchQuery.isBlank()) projects else {
                        projects.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.author.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                if (filteredProjects.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (projects.isEmpty()) "No saved screenplays yet." else "No scripts match '$searchQuery'",
                                style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create a new script using templates or import an existing .fdx / .fountain file.",
                                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted.copy(alpha = 0.6f), fontSize = 11.sp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        items(filteredProjects) { project ->
                            ScriptProjectCard(
                                project = project,
                                onOpen = {
                                    val imp = ScriptImporter.importFromFile(project.file)
                                    if (imp.isSuccess && imp.document != null) {
                                        viewModel.loadDocument(imp.document.blocks, imp.document.titlePage)
                                        Toast.makeText(context, "Opened '${project.title}'", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Failed to load script: ${imp.errorMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDuplicate = {
                                    projectToDuplicate = project
                                    duplicateInputText = "${project.title} (Draft 2)"
                                },
                                onRename = {
                                    projectToRename = project
                                    renameInputText = project.title
                                },
                                onExportPdf = {
                                    val imp = ScriptImporter.importFromFile(project.file)
                                    if (imp.isSuccess && imp.document != null) {
                                        val pdfFile = ScriptFileManager.exportPdf(
                                            context = context,
                                            blocks = imp.document.blocks,
                                            titlePageData = imp.document.titlePage
                                        )
                                        ScriptFileManager.shareFile(context, pdfFile, "application/pdf", "Share Screenplay")
                                    }
                                },
                                onDelete = {
                                    projectToDelete = project
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Sub-Dialogs ---

    // 1. Template Chooser Dialog
    if (showNewTemplateDialog) {
        NewScriptTemplateDialog(
            onDismiss = { showNewTemplateDialog = false },
            onCreate = { title, author, template ->
                val newFile = ScriptLibraryManager.createNewProject(context, title, author, template)
                val imp = ScriptImporter.importFromFile(newFile)
                if (imp.isSuccess && imp.document != null) {
                    viewModel.loadDocument(imp.document.blocks, imp.document.titlePage)
                    refreshTrigger++
                    showNewTemplateDialog = false
                    Toast.makeText(context, "Created new screenplay '$title'", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            }
        )
    }

    // 2. Rename Dialog
    if (projectToRename != null) {
        val proj = projectToRename!!
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Screenplay", color = ElectrumGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter new script title:", color = ScreenplayTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        singleLine = true,
                        textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 14.sp),
                        cursorBrush = SolidColor(ElectrumGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            ScriptLibraryManager.renameProject(context, proj.file, renameInputText.trim())
                            refreshTrigger++
                            projectToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold)
                ) {
                    Text("Rename", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = ScreenplayTextMuted)
                }
            },
            containerColor = ObsidianSurface
        )
    }

    // 3. Duplicate Dialog
    if (projectToDuplicate != null) {
        val proj = projectToDuplicate!!
        AlertDialog(
            onDismissRequest = { projectToDuplicate = null },
            title = { Text("Duplicate Screenplay Draft", color = ElectrumGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Fork a new working draft of '${proj.title}':", color = ScreenplayTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = duplicateInputText,
                        onValueChange = { duplicateInputText = it },
                        singleLine = true,
                        textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 14.sp),
                        cursorBrush = SolidColor(ElectrumGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (duplicateInputText.isNotBlank()) {
                            ScriptLibraryManager.duplicateProject(context, proj.file, duplicateInputText.trim())
                            refreshTrigger++
                            projectToDuplicate = null
                            Toast.makeText(context, "Draft created!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold)
                ) {
                    Text("Duplicate", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDuplicate = null }) {
                    Text("Cancel", color = ScreenplayTextMuted)
                }
            },
            containerColor = ObsidianSurface
        )
    }

    // 4. Delete Confirmation Dialog
    if (projectToDelete != null) {
        val proj = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Screenplay?", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${proj.title}'? This action cannot be undone.", color = ScreenplayTextDark, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        ScriptLibraryManager.deleteProject(proj.file)
                        refreshTrigger++
                        projectToDelete = null
                        Toast.makeText(context, "Screenplay deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = ScreenplayTextMuted)
                }
            },
            containerColor = ObsidianSurface
        )
    }
}

@Composable
private fun ScriptProjectCard(
    project: ScriptProject,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onExportPdf: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(project.lastModified) {
        SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.US).format(Date(project.lastModified))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (project.isActive) 1.5.dp else 0.5.dp,
            color = if (project.isActive) ElectrumGold else ObsidianSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (project.isActive) ElectrumGold else ScreenplayTextDark,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (project.format == "FDX") Color(0xFF1976D2).copy(alpha = 0.2f) else ElectrumGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.format,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (project.format == "FDX") Color(0xFF64B5F6) else ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    if (project.isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }

                if (project.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "by ${project.author}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.sceneCount} scenes  •  ~${project.pageCount} pgs  •  Modified $dateStr",
                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted.copy(alpha = 0.7f), fontSize = 10.sp)
                    )
                }
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!project.isActive) {
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Open", color = ObsidianBlack, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                OutlinedButton(
                    onClick = onDuplicate,
                    border = BorderStroke(0.8.dp, ElectrumGold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Fork", color = ElectrumGold, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onRename, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = ScreenplayTextMuted, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onExportPdf, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export PDF", tint = ScreenplayTextMuted, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373).copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun NewScriptTemplateDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, author: String, template: ScriptTemplate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(ProjectFormat.SCREENPLAY) }
    var selectedTemplate by remember { mutableStateOf(ScriptTemplate.BLANK) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "✨ NEW SCRIPT / PROJECT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Format Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val formats = listOf(
                        Pair(ProjectFormat.SCREENPLAY, "Screenplay"),
                        Pair(ProjectFormat.STAGE_PLAY, "Stage Play"),
                        Pair(ProjectFormat.COMIC_BOOK, "Comic"),
                        Pair(ProjectFormat.NOVEL, "Novel")
                    )
                    formats.forEach { (fmt, label) ->
                        val isSelected = selectedFormat == fmt
                        Surface(
                            color = if (isSelected) ElectrumGold else ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSelected) ElectrumGold else Color(0xFF334155)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedFormat = fmt
                                    selectedTemplate = ScriptTemplate.defaultBlankForFormat(fmt)
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ObsidianBlack else Color.White,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Project Title", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 14.sp),
                    cursorBrush = SolidColor(ElectrumGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack)
                        .padding(10.dp),
                    decorationBox = { inner ->
                        if (title.isEmpty()) Text("e.g. CASABLANCA", color = ScreenplayTextMuted, fontSize = 13.sp)
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Author", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = author,
                    onValueChange = { author = it },
                    singleLine = true,
                    textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 14.sp),
                    cursorBrush = SolidColor(ElectrumGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack)
                        .padding(10.dp),
                    decorationBox = { inner ->
                        if (author.isEmpty()) Text("Your Name", color = ScreenplayTextMuted, fontSize = 13.sp)
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Starter Template (Default: Blank Page)", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                Spacer(modifier = Modifier.height(6.dp))

                val availableTemplates = remember(selectedFormat) {
                    ScriptTemplate.templatesForFormat(selectedFormat)
                }

                availableTemplates.forEach { tmpl ->
                    val isSelected = selectedTemplate == tmpl
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0x35E11D48) else ObsidianBlack)
                            .border(1.dp, if (isSelected) ScribeStarNeonRed else Color(0x25334155), RoundedCornerShape(6.dp))
                            .clickable { selectedTemplate = tmpl }
                            .padding(vertical = 7.dp, horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) ScribeStarNeonRed else ScreenplayTextMuted, RoundedCornerShape(6.dp))
                                .background(if (isSelected) ScribeStarNeonRed else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (tmpl.isBlank) "${tmpl.displayName} (Blank Clean Start)" else tmpl.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) Color.White else ScreenplayTextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ScreenplayTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifBlank { "UNTITLED PROJECT" }
                            onCreate(finalTitle, author.trim(), selectedTemplate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold)
                    ) {
                        Text("Create Project", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
