package com.seshat.writer.novel

import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Intelligent Dialogue Attribution Engine for novelists.
 * Recommends literary dialogue verbs dynamically based on punctuation
 * and narrative context.
 */
object DialogueVerbSuggester {

    private val QUESTION_VERBS = listOf(
        "asked", "inquired", "demanded", "wondered", "pressed",
        "challenged", "whispered", "probed", "hesitated", "pleaded"
    )

    private val EXCLAMATION_VERBS = listOf(
        "exclaimed", "shouted", "cried", "barked", "snapped",
        "gasped", "roared", "bellowed", "warned", "insisted"
    )

    private val PERIOD_VERBS = listOf(
        "said", "murmured", "replied", "admitted", "observed",
        "stated", "noted", "confessed", "agreed", "conceded"
    )

    private val COMMA_VERBS = listOf(
        "whispered", "muttered", "ventured", "continued", "sighed",
        "cautioned", "teased", "mused", "chuckled", "breathed"
    )

    private val DASH_VERBS = listOf(
        "interrupted", "stammered", "hesitated", "faltered",
        "trailed off", "choked out", "cut in", "stumbled"
    )

    /**
     * Returns curated verbs based on the terminal punctuation used in the dialogue quote.
     */
    fun getVerbsForPunctuation(punctuation: Char): List<String> {
        return when (punctuation) {
            '?' -> QUESTION_VERBS
            '!' -> EXCLAMATION_VERBS
            '.' -> PERIOD_VERBS
            ',' -> COMMA_VERBS
            '—', '-' -> DASH_VERBS
            else -> PERIOD_VERBS
        }
    }

    /**
     * Inspects a paragraph string and detects if it ends with dialogue punctuation
     * requiring a closing quote and dialogue tag verbs.
     * Returns the punctuation char if detected, or null.
     */
    fun detectTerminalQuotePunctuation(text: String): Char? {
        var trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return null
        if (trimmed.endsWith("”") || trimmed.endsWith("\"")) {
            trimmed = trimmed.dropLast(1).trimEnd()
        }
        if (trimmed.isEmpty()) return null
        val lastChar = trimmed.last()
        return when (lastChar) {
            '?', '!', '.', ',', '—' -> lastChar
            '-' -> if (trimmed.endsWith("--")) '—' else null
            else -> null
        }
    }

    /**
     * Generates context-aware literary verbs from Gemini AI when configured.
     */
    suspend fun suggestVerbsWithAi(
        config: AiConfig,
        dialogueQuote: String,
        punctuation: Char,
        storyBibleContext: String = ""
    ): List<String> = withContext(Dispatchers.IO) {
        if (!config.isEnabled || !config.isConfigured || dialogueQuote.isBlank()) {
            return@withContext getVerbsForPunctuation(punctuation)
        }

        val prompt = buildString {
            if (storyBibleContext.isNotBlank()) {
                append("STORY CONTEXT:\n${storyBibleContext.take(800)}\n\n")
            }
            append("SPOKEN LINE: \"$dialogueQuote\"\n")
            append("TERMINAL PUNCTUATION: '$punctuation'\n\n")
            append("List exactly 6 creative, expressive dialogue attribution verbs (past tense, single word each) ")
            append("that fit this line instead of 'said'. Output format: comma-separated list of verbs only.")
        }

        val systemInstruction = "You are a master fiction editor helping a novelist vary dialogue tags with evocative verbs."

        val result = AiClient.callLlm(config, systemInstruction, prompt)
        result.map { raw ->
            val verbs = raw.replace("\n", " ")
                .split(",")
                .map { it.trim().lowercase().removePrefix("-").removePrefix("•").trim() }
                .filter { it.isNotBlank() && it.length < 20 && it.all { c -> c.isLetter() || c == ' ' } }
            if (verbs.isNotEmpty()) verbs.take(8) else getVerbsForPunctuation(punctuation)
        }.getOrElse {
            getVerbsForPunctuation(punctuation)
        }
    }
}
