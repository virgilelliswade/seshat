package com.seshat.writer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ai.AiChatMessage
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import kotlinx.coroutines.launch

@Composable
fun ScriptDoctorDialog(
    currentSceneHeading: String,
    currentSceneBlocks: List<ScreenplayBlock>,
    charactersInScene: List<String>,
    storyBibleContext: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config = remember { AiPreferences.load(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Chat, 1 = What-If, 2 = Scene Audit

    // Chat state
    val chatMessages = remember {
        mutableStateListOf(
            AiChatMessage(
                role = "assistant",
                content = if (storyBibleContext.isNotBlank()) {
                    "Greetings. I'm your Seshat Script Doctor. I have your scene ('$currentSceneHeading') and your active Story Bible & World Lore loaded into memory. How can we elevate this sequence today?"
                } else {
                    "Greetings. I'm your Seshat Script Doctor. I have your current scene ('$currentSceneHeading') loaded into memory. How can we elevate this sequence today?"
                }
            )
        )
    }
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }

    // What-If Complications state
    var complications by remember { mutableStateOf<List<String>>(emptyList()) }
    var isComplicationsLoading by remember { mutableStateOf(false) }

    // Scene Audit state
    var sceneAuditText by remember { mutableStateOf<String?>(null) }
    var isAuditLoading by remember { mutableStateOf(false) }

    val sceneContentText = remember(currentSceneBlocks) {
        currentSceneBlocks.joinToString("\n") { b ->
            if (b.type == com.seshat.writer.model.BlockType.CHARACTER) b.text.uppercase() else b.text
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || isChatLoading) return
        val userMsg = AiChatMessage(role = "user", content = text.trim())
        chatMessages.add(userMsg)
        chatInput = ""
        isChatLoading = true

        scope.launch {
            val scriptContext = "SCENE: $currentSceneHeading\n\n$sceneContentText"
            val result = AiClient.chatWithDoctor(config, chatMessages.toList(), scriptContext, storyBibleContext)
            isChatLoading = false
            result.fold(
                onSuccess = { reply ->
                    chatMessages.add(AiChatMessage(role = "assistant", content = reply))
                },
                onFailure = { err ->
                    chatMessages.add(
                        AiChatMessage(
                            role = "assistant",
                            content = "⚠️ Error: ${err.message ?: "Unable to complete request. Check API settings."}"
                        )
                    )
                }
            )
        }
    }

    fun generateComplications() {
        if (isComplicationsLoading) return
        isComplicationsLoading = true
        scope.launch {
            val result = AiClient.generateComplications(
                config,
                currentSceneHeading,
                sceneContentText,
                charactersInScene
            )
            isComplicationsLoading = false
            result.fold(
                onSuccess = { comps -> complications = comps },
                onFailure = { err ->
                    complications = listOf("⚠️ Error generating complications: ${err.message}")
                }
            )
        }
    }

    fun runSceneAudit() {
        if (isAuditLoading) return
        isAuditLoading = true
        scope.launch {
            val result = AiClient.auditScene(config, currentSceneHeading, sceneContentText)
            isAuditLoading = false
            result.fold(
                onSuccess = { audit -> sceneAuditText = audit },
                onFailure = { err -> sceneAuditText = "⚠️ Error running audit: ${err.message}" }
            )
        }
    }

    fun runLoreAudit() {
        if (isAuditLoading) return
        isAuditLoading = true
        scope.launch {
            val result = AiClient.auditSceneWithBible(
                config,
                sceneContentText,
                currentSceneHeading,
                storyBibleContext.ifBlank { "No world lore defined yet." }
            )
            isAuditLoading = false
            result.fold(
                onSuccess = { audit -> sceneAuditText = audit },
                onFailure = { err -> sceneAuditText = "⚠️ Error running lore audit: ${err.message}" }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                color = ObsidianBlack
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    ScriptDoctorHeader(
                        currentSceneHeading = currentSceneHeading,
                        blockCount = currentSceneBlocks.size,
                        storyBibleContext = storyBibleContext,
                        onDismiss = onDismiss
                    )

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = ObsidianSurface,
                        contentColor = ElectrumGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ElectrumGold
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "💬 SPARRING PARTNER",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                if (complications.isEmpty()) generateComplications()
                            },
                            text = {
                                Text(
                                    "⚡ WHAT-IF COMPLICATIONS",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                                if (sceneAuditText == null) runSceneAudit()
                            },
                            text = {
                                Text(
                                    "🔍 SCENE AUDIT",
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }

                    HorizontalDivider(color = ObsidianSurfaceVariant)

                    // Tab Content
                    when (selectedTab) {
                        0 -> ScriptDoctorChatTab(
                            chatMessages = chatMessages,
                            isChatLoading = isChatLoading,
                            chatInput = chatInput,
                            onChatInputChange = { chatInput = it },
                            onSendMessage = { sendChatMessage(it) },
                            modifier = Modifier.weight(1f)
                        )
                        1 -> ScriptDoctorWhatIfTab(
                            complications = complications,
                            isComplicationsLoading = isComplicationsLoading,
                            onRegenerate = { generateComplications() },
                            modifier = Modifier.weight(1f)
                        )
                        2 -> ScriptDoctorAuditTab(
                            sceneAuditText = sceneAuditText,
                            isAuditLoading = isAuditLoading,
                            onRunSceneAudit = { runSceneAudit() },
                            onRunLoreAudit = { runLoreAudit() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptDoctorHeader(
    currentSceneHeading: String,
    blockCount: Int,
    storyBibleContext: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "🩺 SCRIPT DOCTOR & SPARRING PARTNER",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectrumGold
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Active Context: $currentSceneHeading ($blockCount blocks)",
                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                )
                if (storyBibleContext.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFF1B5E20), shape = RoundedCornerShape(3.dp)) {
                        Text(
                            text = "🌍 Lore Connected",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF81C784), fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = ScreenplayTextMuted
            )
        }
    }
}

@Composable
private fun ScriptDoctorChatTab(
    chatMessages: List<AiChatMessage>,
    isChatLoading: Boolean,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isUser) ElectrumGold.copy(alpha = 0.15f) else ObsidianSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .border(
                                1.dp,
                                if (isUser) ElectrumGold.copy(alpha = 0.5f) else ObsidianSurfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "YOU" else "SCRIPT DOCTOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (isUser) ElectrumGold else LightGold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = if (isUser) FontFamily.Default else FontFamily.Monospace,
                                    color = ScreenplayTextDark,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = ElectrumGold,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Script Doctor is reviewing scene context...",
                            style = MaterialTheme.typography.bodySmall.copy(color = ElectrumGold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick suggestion chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val quickPrompts = listOf(
                "Raise the stakes",
                "Check pacing & drag",
                "Sharpen subtext",
                "Strengthen turn"
            )
            for (qp in quickPrompts) {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSendMessage(qp) }
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = qp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                placeholder = {
                    Text(
                        "Ask for dramaturgical advice, dialogue punch-ups, or plot fixes...",
                        color = ScreenplayTextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectrumGold,
                    unfocusedBorderColor = ObsidianSurfaceVariant,
                    focusedTextColor = ScreenplayTextDark,
                    unfocusedTextColor = ScreenplayTextDark
                ),
                maxLines = 3
            )

            IconButton(
                onClick = { onSendMessage(chatInput) },
                enabled = chatInput.isNotBlank() && !isChatLoading,
                modifier = Modifier
                    .background(if (chatInput.isNotBlank()) ElectrumGold else ObsidianSurface, RoundedCornerShape(8.dp))
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (chatInput.isNotBlank()) ObsidianBlack else ScreenplayTextMuted
                )
            }
        }
    }
}

@Composable
private fun ScriptDoctorWhatIfTab(
    complications: List<String>,
    isComplicationsLoading: Boolean,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UNEXPECTED OBSTACLES & COMPLICATIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectrumGold
                )
            )
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isComplicationsLoading) {
                        onRegenerate()
                    }
                    .border(1.dp, ElectrumGold, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = if (isComplicationsLoading) "GENERATING..." else "🔄 RE-GENERATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isComplicationsLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = ElectrumGold,
                    strokeWidth = 2.dp
                )
                Text("Synthesizing organic dramatic complications...", color = ElectrumGold)
            }
        } else {
            for ((idx, comp) in complications.withIndex()) {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "COMPLICATION #${idx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LightGold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comp,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ScreenplayTextDark,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(comp))
                            }
                        ) {
                            Text("📋", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptDoctorAuditTab(
    sceneAuditText: String?,
    isAuditLoading: Boolean,
    onRunSceneAudit: () -> Unit,
    onRunLoreAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DRAMATURGICAL SCENE AUDIT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectrumGold
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = ObsidianBlack,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = !isAuditLoading) {
                            onRunSceneAudit()
                        }
                        .border(1.dp, ElectrumGold, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = if (isAuditLoading) "AUDITING..." else "🔄 DRAMA AUDIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = ObsidianBlack,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = !isAuditLoading) {
                            onRunLoreAudit()
                        }
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "🌍 4-PILLAR LORE AUDIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isAuditLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = ElectrumGold,
                    strokeWidth = 2.dp
                )
                Text("Auditing scene structure, pacing, and subtext...", color = ElectrumGold)
            }
        } else {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = sceneAuditText ?: "(No audit performed yet)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = ScreenplayTextDark,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

