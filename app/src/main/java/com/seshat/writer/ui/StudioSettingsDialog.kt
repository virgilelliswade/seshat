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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiConfig
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.ai.AiProvider
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun StudioSettingsDialog(
    currentThemeMode: ThemeMode = ThemeMode.DARK,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    onDismiss: () -> Unit,
    onConfigChanged: (AiConfig) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var themeMode by remember(currentThemeMode) { mutableStateOf(currentThemeMode) }
    var config by remember { mutableStateOf(AiPreferences.load(context)) }
    var showApiKey by remember { mutableStateOf(false) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isTestSuccess by remember { mutableStateOf(false) }

    fun updateConfig(newConfig: AiConfig) {
        config = newConfig
        AiPreferences.save(context, newConfig)
        onConfigChanged(newConfig)
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
                                text = "⚙️ STUDIO SETTINGS & AI ASSISTANT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = "Configure AI Script Doctor, BYOK API credentials, or toggle offline mode anytime.",
                                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ScreenplayTextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = ObsidianSurfaceVariant)

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        // 0. STUDIO APPEARANCE & THEME CARD
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🎨 STUDIO APPEARANCE & THEME",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = ElectrumGold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Switch between deep obsidian dark mode and warm papyrus daylight writing atmosphere.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (mode in ThemeMode.values()) {
                                        val isSelected = themeMode == mode
                                        Surface(
                                            color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) ElectrumGold else ObsidianSurfaceVariant
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    themeMode = mode
                                                    onThemeModeChanged(mode)
                                                }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = mode.icon,
                                                    fontSize = 20.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = mode.displayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) ElectrumGold else ScreenplayTextDark
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = when (mode) {
                                                        ThemeMode.DARK -> "Obsidian & Gold"
                                                        ThemeMode.LIGHT -> "Warm Papyrus"
                                                        ThemeMode.SYSTEM -> "System Auto"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 10.sp,
                                                        color = ScreenplayTextMuted
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. MASTER AI TOGGLE CARD
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (config.isEnabled) ElectrumGold.copy(alpha = 0.5f) else ObsidianSurfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text(
                                        text = "Enable AI Assistant & Script Doctor Features",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ScreenplayTextDark
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Turn this OFF anytime to completely hide all AI menus, pills, dialogue reports, and disable all background network requests.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                    )
                                }
                                Switch(
                                    checked = config.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        updateConfig(config.copy(isEnabled = isChecked))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ElectrumGold,
                                        checkedTrackColor = ObsidianSurfaceVariant,
                                        uncheckedThumbColor = ScreenplayTextMuted,
                                        uncheckedTrackColor = ObsidianBlack
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (config.isEnabled) {
                            // 2. PROVIDER SELECTION CARD
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "AI MODEL PROVIDER (BYOK)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = ElectrumGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Provider chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (prov in AiProvider.values()) {
                                            val isSelected = config.provider == prov
                                            Surface(
                                                color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        updateConfig(
                                                            config.copy(
                                                                provider = prov,
                                                                modelName = prov.defaultModel
                                                            )
                                                        )
                                                    }
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) ElectrumGold else ObsidianSurfaceVariant,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = prov.displayName,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) ElectrumGold else ScreenplayTextDark
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // API Key Field (if required)
                                    if (config.provider.requiresKey) {
                                        Text(
                                            text = "${config.provider.displayName} API Key",
                                            style = MaterialTheme.typography.labelMedium.copy(color = ScreenplayTextDark)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = config.apiKey,
                                            onValueChange = { updateConfig(config.copy(apiKey = it)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = {
                                                Text(
                                                    text = "Paste your ${config.provider.displayName} API key...",
                                                    color = ScreenplayTextMuted,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            trailingIcon = {
                                                Text(
                                                    text = if (showApiKey) "HIDE" else "SHOW",
                                                    modifier = Modifier
                                                        .clickable { showApiKey = !showApiKey }
                                                        .padding(horizontal = 8.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = ElectrumGold
                                                    )
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ElectrumGold,
                                                unfocusedBorderColor = ObsidianSurfaceVariant,
                                                focusedTextColor = ScreenplayTextDark,
                                                unfocusedTextColor = ScreenplayTextDark
                                            ),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }

                                    // Custom Endpoint (for Local / Ollama)
                                    if (config.provider == AiProvider.CUSTOM_LOCAL) {
                                        Text(
                                            text = "Local Endpoint URL",
                                            style = MaterialTheme.typography.labelMedium.copy(color = ScreenplayTextDark)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = config.customEndpoint,
                                            onValueChange = { updateConfig(config.copy(customEndpoint = it)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = {
                                                Text("e.g. http://localhost:11434/v1", color = ScreenplayTextMuted)
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ElectrumGold,
                                                unfocusedBorderColor = ObsidianSurfaceVariant,
                                                focusedTextColor = ScreenplayTextDark,
                                                unfocusedTextColor = ScreenplayTextDark
                                            ),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }

                                    // Model Name Field & Presets
                                    Text(
                                        text = "Model Identifier",
                                        style = MaterialTheme.typography.labelMedium.copy(color = ScreenplayTextDark)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = config.modelName,
                                        onValueChange = { updateConfig(config.copy(modelName = it)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElectrumGold,
                                            unfocusedBorderColor = ObsidianSurfaceVariant,
                                            focusedTextColor = ScreenplayTextDark,
                                            unfocusedTextColor = ScreenplayTextDark
                                        ),
                                        singleLine = true
                                    )

                                    // Preset Model Pills
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val presets = when (config.provider) {
                                            AiProvider.GOOGLE_GEMINI -> listOf("gemini-3.8-flash", "gemini-3.8-pro", "gemini-4-preview", "gemini-3.0-flash")
                                            AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "o3-mini", "o1")
                                            AiProvider.ANTHROPIC_CLAUDE -> listOf("claude-3-7-sonnet", "claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022")
                                            AiProvider.CUSTOM_LOCAL -> listOf("llama3.3", "qwen2.5", "mistral-nemo")
                                        }
                                        for (preset in presets) {
                                            Surface(
                                                color = if (config.modelName == preset) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable { updateConfig(config.copy(modelName = preset)) }
                                                    .border(
                                                        1.dp,
                                                        if (config.modelName == preset) ElectrumGold else ObsidianSurfaceVariant,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            ) {
                                                Text(
                                                    text = preset,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (config.modelName == preset) ElectrumGold else ScreenplayTextMuted,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Temperature Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Creativity (Temperature): ${String.format(java.util.Locale.US, "%.1f", config.temperature)}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                        Text(
                                            text = if (config.temperature < 0.4f) "Strict & Grounded" else if (config.temperature > 0.8f) "Highly Creative" else "Balanced",
                                            style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold)
                                        )
                                    }
                                    Slider(
                                        value = config.temperature,
                                        onValueChange = { updateConfig(config.copy(temperature = it)) },
                                        valueRange = 0.0f..1.2f,
                                        steps = 11,
                                        colors = SliderDefaults.colors(
                                            thumbColor = ElectrumGold,
                                            activeTrackColor = ElectrumGold,
                                            inactiveTrackColor = ObsidianSurfaceVariant
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Test Connection Button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = ObsidianBlack,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable(enabled = !isTesting) {
                                                    isTesting = true
                                                    testStatus = null
                                                    scope.launch {
                                                        val result = AiClient.testConnection(config)
                                                        isTesting = false
                                                        result.fold(
                                                            onSuccess = { resp ->
                                                                isTestSuccess = true
                                                                testStatus = "✅ Connection Verified: $resp"
                                                            },
                                                            onFailure = { err ->
                                                                isTestSuccess = false
                                                                testStatus = "❌ ${err.message ?: "Connection failed"}"
                                                            }
                                                        )
                                                    }
                                                }
                                                .border(1.dp, ElectrumGold, RoundedCornerShape(6.dp))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isTesting) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        color = ElectrumGold,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                Text(
                                                    text = if (isTesting) "TESTING..." else "TEST CONNECTION",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = ElectrumGold
                                                    )
                                                )
                                            }
                                        }

                                        if (testStatus != null) {
                                            Text(
                                                text = testStatus!!,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (isTestSuccess) Color(0xFF4CAF50) else Color(0xFFE57373)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. FEATURE TOGGLES CARD
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ACTIVE AI FEATURES",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = ElectrumGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    FeatureToggleRow(
                                        title = "✨ Inline Polish & Subtext Tool",
                                        subtitle = "Contextual floating tool on Action & Dialogue blocks to punch up verbs and deepen subtext.",
                                        checked = config.enableInlineAssistant,
                                        onCheckedChange = { updateConfig(config.copy(enableInlineAssistant = it)) }
                                    )

                                    HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                                    FeatureToggleRow(
                                        title = "🩺 Script Doctor & Sparring Partner",
                                        subtitle = "Sidebar drawer with dramaturgical chat, scene continuity audits, and What-If complications.",
                                        checked = config.enableScriptDoctor,
                                        onCheckedChange = { updateConfig(config.copy(enableScriptDoctor = it)) }
                                    )

                                    HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                                    FeatureToggleRow(
                                        title = "📊 Studio Reader Coverage Report",
                                        subtitle = "One-click 3-page industry coverage report with synopsis, ratings, and PDF export in Production Reports.",
                                        checked = config.enableStudioCoverage,
                                        onCheckedChange = { updateConfig(config.copy(enableStudioCoverage = it)) }
                                    )

                                    HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                                    FeatureToggleRow(
                                        title = "🎙️ Interactive Table Read (Text-To-Speech)",
                                        subtitle = "Reads screenplay aloud with distinct character voices and rehearsal mode for rehearsing your dialogue lines.",
                                        checked = config.enableTableRead,
                                        onCheckedChange = { updateConfig(config.copy(enableTableRead = it)) }
                                    )

                                    HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                                    FeatureToggleRow(
                                        title = "🎙️ Gemini Screenplay Voice Dictation",
                                        subtitle = "Dictate dialogue and action hands-free via Googlebook Mic key or Ctrl+D. Gemini 3.8 structures spoken words into standard Fountain blocks.",
                                        checked = config.enableVoiceDictation,
                                        onCheckedChange = { updateConfig(config.copy(enableVoiceDictation = it)) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 4. PRIVACY & WGA COMPLIANCE PLEDGE
                        Surface(
                            color = ObsidianSurface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🛡️ PRIVACY & SCRIPT SECURITY PLEDGE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = LightGold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Zero Cloud Middlemen: Seshat communicates directly from your device to your chosen AI provider using encrypted HTTPS.\n" +
                                           "• Direct Key Storage: Your API key is stored locally in private application sandbox storage and never transmitted to Seshat developers.\n" +
                                           "• No Model Training: Commercial API calls (Google Gemini API, OpenAI API, Anthropic API) are explicitly exempted by default from model training.\n" +
                                           "• 100% Offline Control: Toggling 'Enable AI Features' OFF instantly cuts all outbound requests, ensuring absolute NDA and festival compliance.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ScreenplayTextMuted,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ScreenplayTextDark
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElectrumGold,
                checkedTrackColor = ObsidianSurfaceVariant,
                uncheckedThumbColor = ScreenplayTextMuted,
                uncheckedTrackColor = ObsidianBlack
            )
        )
    }
}
