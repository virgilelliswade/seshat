package com.seshat.writer.ai

import android.content.Context
import android.content.SharedPreferences

enum class AiProvider(val displayName: String, val defaultModel: String, val requiresKey: Boolean) {
    GOOGLE_GEMINI("Google Gemini", "gemini-3.8-flash", true),
    OPENAI("OpenAI (ChatGPT)", "gpt-4o", true),
    ANTHROPIC_CLAUDE("Anthropic Claude", "claude-3-7-sonnet", true),
    CUSTOM_LOCAL("Custom / Local (Ollama)", "llama3.3", false)
}

data class AiConfig(
    val isEnabled: Boolean = false,
    val provider: AiProvider = AiProvider.GOOGLE_GEMINI,
    val apiKey: String = "",
    val modelName: String = AiProvider.GOOGLE_GEMINI.defaultModel,
    val customEndpoint: String = "http://localhost:11434/v1",
    val temperature: Float = 0.7f,
    val enableInlineAssistant: Boolean = true,
    val enableScriptDoctor: Boolean = true,
    val enableStudioCoverage: Boolean = true,
    val enableTableRead: Boolean = true,
    val enableVoiceDictation: Boolean = true
) {
    val isConfigured: Boolean
        get() = isEnabled && (!provider.requiresKey || apiKey.isNotBlank())
}

object AiPreferences {
    private const val PREFS_NAME = "seshat_ai_preferences"
    private const val KEY_ENABLED = "ai_enabled"
    private const val KEY_PROVIDER = "ai_provider"
    private const val KEY_API_KEY = "ai_api_key"
    private const val KEY_MODEL = "ai_model"
    private const val KEY_ENDPOINT = "ai_custom_endpoint"
    private const val KEY_TEMPERATURE = "ai_temperature"
    private const val KEY_INLINE_ENABLED = "ai_inline_enabled"
    private const val KEY_SCRIPT_DOCTOR_ENABLED = "ai_script_doctor_enabled"
    private const val KEY_STUDIO_COVERAGE_ENABLED = "ai_studio_coverage_enabled"
    private const val KEY_TABLE_READ_ENABLED = "ai_table_read_enabled"
    private const val KEY_VOICE_DICTATION_ENABLED = "ai_voice_dictation_enabled"

    @Volatile
    private var cachedConfig: AiConfig? = null

    fun load(context: Context): AiConfig {
        cachedConfig?.let { return it }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val providerStr = prefs.getString(KEY_PROVIDER, AiProvider.GOOGLE_GEMINI.name) ?: AiProvider.GOOGLE_GEMINI.name
        val provider = try {
            AiProvider.valueOf(providerStr)
        } catch (_: Exception) {
            AiProvider.GOOGLE_GEMINI
        }

        val config = AiConfig(
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            provider = provider,
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            modelName = prefs.getString(KEY_MODEL, provider.defaultModel) ?: provider.defaultModel,
            customEndpoint = prefs.getString(KEY_ENDPOINT, "http://localhost:11434/v1") ?: "http://localhost:11434/v1",
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f),
            enableInlineAssistant = prefs.getBoolean(KEY_INLINE_ENABLED, true),
            enableScriptDoctor = prefs.getBoolean(KEY_SCRIPT_DOCTOR_ENABLED, true),
            enableStudioCoverage = prefs.getBoolean(KEY_STUDIO_COVERAGE_ENABLED, true),
            enableTableRead = prefs.getBoolean(KEY_TABLE_READ_ENABLED, true),
            enableVoiceDictation = prefs.getBoolean(KEY_VOICE_DICTATION_ENABLED, true)
        )
        cachedConfig = config
        return config
    }

    fun save(context: Context, config: AiConfig) {
        cachedConfig = config
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putString(KEY_PROVIDER, config.provider.name)
            .putString(KEY_API_KEY, config.apiKey)
            .putString(KEY_MODEL, config.modelName)
            .putString(KEY_ENDPOINT, config.customEndpoint)
            .putFloat(KEY_TEMPERATURE, config.temperature)
            .putBoolean(KEY_INLINE_ENABLED, config.enableInlineAssistant)
            .putBoolean(KEY_SCRIPT_DOCTOR_ENABLED, config.enableScriptDoctor)
            .putBoolean(KEY_STUDIO_COVERAGE_ENABLED, config.enableStudioCoverage)
            .putBoolean(KEY_TABLE_READ_ENABLED, config.enableTableRead)
            .putBoolean(KEY_VOICE_DICTATION_ENABLED, config.enableVoiceDictation)
            .apply()
    }

    fun clearCache() {
        cachedConfig = null
    }
}
