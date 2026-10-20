package com.seshat.writer.ai

import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import android.util.Base64

data class AiCoverCopyResult(
    val tagline: String,
    val backCoverBlurb: String,
    val praiseQuote: String,
    val frontFlapTeaser: String,
    val backFlapAuthorBio: String,
    val artPrompt: String
)

data class AiChatMessage(
    val role: String, // "user" or "model" / "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AiClient {

    private const val CONNECT_TIMEOUT_MS = 15000
    private const val READ_TIMEOUT_MS = 60000

    private const val HOLLYWOOD_SYSTEM_INSTRUCTION =
        "You are an elite Hollywood script doctor, award-winning dramaturg, and veteran studio reader for Seshat Screenplay Studio. " +
        "You uphold standard industry formatting, concise visual action lines (present tense, active verbs, eliminating unfilmable internal states), " +
        "sharp multi-layered dialogue with rich subtext, distinct character cadences, and compelling narrative escalation. " +
        "Provide constructive, precise, and practical feedback without fluff."

    suspend fun testConnection(config: AiConfig): Result<String> = withContext(Dispatchers.IO) {
        if (!config.isEnabled) {
            return@withContext Result.failure(IllegalStateException("AI features are disabled in Studio Settings."))
        }
        val prompt = "Reply in exactly one sentence: confirm that the Seshat Screenplay AI connection is active and ready."
        callLlm(config, systemPrompt = "You are a connectivity test responder.", userPrompt = prompt)
    }

    suspend fun generateCoverCopy(
        config: AiConfig,
        title: String,
        subtitle: String = "",
        author: String = "",
        genre: String = "Fiction",
        synopsis: String = "",
        storySample: String = ""
    ): Result<AiCoverCopyResult> = withContext(Dispatchers.IO) {
        val systemPrompt = "You are an elite creative director, bestselling book publicist, and art director for Seshat Publishing Studio. Return ONLY valid JSON."
        val userPrompt = buildString {
            append("CREATE COMPLETE BOOK COVER AND DUST JACKET COPY FOR THE FOLLOWING NOVEL:\n\n")
            append("TITLE: $title\n")
            if (subtitle.isNotBlank()) append("SUBTITLE: $subtitle\n")
            append("AUTHOR: ${author.ifBlank { "Author Name" }}\n")
            append("GENRE: $genre\n")
            if (synopsis.isNotBlank()) append("SYNOPSIS / PREMISE:\n$synopsis\n\n")
            if (storySample.isNotBlank()) append("STORY SAMPLE:\n${storySample.take(1500)}\n\n")
            append("OUTPUT MUST BE STRICTLY VALID JSON WITH NO SURROUNDING MARKDOWN OR COMMENTARY:\n")
            append("{\n")
            append("  \"tagline\": \"A punchy 1-sentence marketing hook or book tagline\",\n")
            append("  \"backCoverBlurb\": \"Compelling 2-3 paragraph back-cover sales pitch ending with gripping stakes\",\n")
            append("  \"praiseQuote\": \"\\\"An electrifying, unputdownable tour de force.\\\" — The Literary Review\",\n")
            append("  \"frontFlapTeaser\": \"A captivating inside front-flap story synopsis that hooks the reader\",\n")
            append("  \"backFlapAuthorBio\": \"A polished, prestigious 2-sentence author bio\",\n")
            append("  \"artPrompt\": \"A vivid, high-concept visual prompt for generative AI describing the book cover illustration (mood, central focal subject, color palette, lighting, textures, no text)\"\n")
            append("}\n")
        }

        callLlm(config, systemPrompt, userPrompt).mapCatching { rawText ->
            val clean = cleanJsonString(rawText)
            val json = JSONObject(clean)
            AiCoverCopyResult(
                tagline = json.optString("tagline", "A gripping tale of mystery and fate."),
                backCoverBlurb = json.optString("backCoverBlurb", "When shadows rise, a lone seeker must uncover the truth before time runs out."),
                praiseQuote = json.optString("praiseQuote", "\"An extraordinary and dazzling triumph.\" — Seshat Literary Review"),
                frontFlapTeaser = json.optString("frontFlapTeaser", synopsis.ifBlank { "An epic journey into the unknown begins." }),
                backFlapAuthorBio = json.optString("backFlapAuthorBio", "${author.ifBlank { "The author" }} crafts rich, atmospheric stories exploring the depths of humanity."),
                artPrompt = json.optString("artPrompt", "$genre book cover illustration, dramatic cinematic lighting, rich textures, central iconic symbol, atmospheric masterpiece, 8k")
            )
        }
    }

    suspend fun generateImageWithAi(
        config: AiConfig,
        prompt: String,
        outputFile: File,
        aspectRatio: String = "1:1"
    ): Result<File> = withContext(Dispatchers.IO) {
        if (!config.isEnabled || config.apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("AI API key is missing. Configure it in Studio Settings."))
        }

        when (config.provider) {
            AiProvider.GOOGLE_GEMINI -> {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=${config.apiKey.trim()}"
                val reqJson = JSONObject().apply {
                    val instancesArr = JSONArray().put(JSONObject().put("prompt", prompt))
                    put("instances", instancesArr)
                    val paramsObj = JSONObject().apply {
                        put("sampleCount", 1)
                        put("aspectRatio", aspectRatio)
                        put("outputMimeType", "image/jpeg")
                    }
                    put("parameters", paramsObj)
                }

                val (statusCode, responseText) = executeHttpPost(endpoint, reqJson.toString(), emptyMap())
                if (statusCode in 200..299) {
                    val respObj = JSONObject(responseText)
                    val predictions = respObj.optJSONArray("predictions")
                    if (predictions != null && predictions.length() > 0) {
                        val pred = predictions.getJSONObject(0)
                        val b64 = pred.optString("bytesBase64Encoded", "")
                        if (b64.isNotBlank()) {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { it.write(bytes) }
                            return@withContext Result.success(outputFile)
                        }
                    }
                }
                Result.failure(Exception("Gemini Imagen error ($statusCode): ${extractErrorMessage(responseText)}"))
            }
            AiProvider.OPENAI -> {
                val endpoint = "https://api.openai.com/v1/images/generations"
                val reqJson = JSONObject().apply {
                    put("model", "dall-e-3")
                    put("prompt", prompt)
                    put("n", 1)
                    put("size", if (aspectRatio == "1:1") "1024x1024" else "1024x1792")
                    put("response_format", "b64_json")
                }
                val headers = mapOf("Authorization" to "Bearer ${config.apiKey.trim()}")
                val (statusCode, responseText) = executeHttpPost(endpoint, reqJson.toString(), headers)
                if (statusCode in 200..299) {
                    val respObj = JSONObject(responseText)
                    val dataArr = respObj.optJSONArray("data")
                    if (dataArr != null && dataArr.length() > 0) {
                        val b64 = dataArr.getJSONObject(0).optString("b64_json", "")
                        if (b64.isNotBlank()) {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { it.write(bytes) }
                            return@withContext Result.success(outputFile)
                        }
                    }
                }
                Result.failure(Exception("OpenAI Image error ($statusCode): ${extractErrorMessage(responseText)}"))
            }
            else -> {
                Result.failure(UnsupportedOperationException("AI image generation is supported with Gemini and OpenAI."))
            }
        }
    }

    suspend fun generateCoverArtWithAi(
        config: AiConfig,
        prompt: String,
        outputFile: File
    ): Result<File> = generateImageWithAi(config, prompt, outputFile, "3:4")

    suspend fun generateCharacterPortraitWithAi(
        config: AiConfig,
        characterName: String,
        role: String,
        physicalDescription: String,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("Cinematic character portrait of $characterName")
            if (role.isNotBlank()) append(", $role")
            if (physicalDescription.isNotBlank()) {
                append(". Appearance: $physicalDescription")
            } else {
                append(". Visually striking cinematic character design with expressive facial features")
            }
            append(". Expressive eyes, detailed facial features, character concept art, dramatic studio lighting, 8k resolution, masterpiece, centered composition, high-end production art.")
        }
        generateImageWithAi(config, prompt, outputFile, "1:1")
    }

    suspend fun punchUpAction(
        config: AiConfig,
        originalText: String,
        style: String = "Active Verbs & Visual Punch",
        sceneContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("PUNCH UP this screenplay action line according to industry standards ($style).\n")
            if (sceneContext.isNotBlank()) {
                append("SCENE CONTEXT:\n$sceneContext\n\n")
            }
            append("ORIGINAL ACTION LINE:\n\"$originalText\"\n\n")
            append("RULES:\n")
            append("1. Maximize visual immediacy, tension, and kinetic energy.\n")
            append("2. Use strong, active transitive verbs. Eliminate passive voice ('is walking' -> 'strides').\n")
            append("3. Eliminate unfilmable thoughts and internal states.\n")
            append("4. Keep it concise (2-4 lines max, matching screenplay block length).\n")
            append("5. Output ONLY the rewritten action block with no commentary, quotation marks, or explanations.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, userPrompt).map { cleanGeneratedBlock(it) }
    }

    suspend fun deepenSubtext(
        config: AiConfig,
        character: String,
        originalDialogue: String,
        sceneContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("REWRITE this dialogue line for $character with deeper dramatic subtext.\n")
            if (sceneContext.isNotBlank()) {
                append("SCENE CONTEXT:\n$sceneContext\n\n")
            }
            append("CHARACTER: $character\n")
            append("ORIGINAL SPOKEN LINE: \"$originalDialogue\"\n\n")
            append("RULES:\n")
            append("1. Eliminate 'on-the-nose' exposition where the character literally states what they feel or narrate the plot.\n")
            append("2. Hide the true intention beneath deflection, subtext, irony, guarded vulnerability, or subtle passive-aggression.\n")
            append("3. Match natural human speech cadence and character personality.\n")
            append("4. Output ONLY the rewritten dialogue line with no quotes, character cue, or conversational explanation.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, userPrompt).map { cleanGeneratedBlock(it) }
    }

    suspend fun checkCharacterVoice(
        config: AiConfig,
        character: String,
        sampleDialogue: String,
        otherDialogues: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("AUDIT the distinct vocal cadence and originality of character $character.\n\n")
            append("CHARACTER TO AUDIT: $character\n")
            append("SAMPLED SPEECH:\n\"$sampleDialogue\"\n\n")
            if (otherDialogues.isNotBlank()) {
                append("OTHER CHARACTERS' SPEECHES IN SCENE:\n$otherDialogues\n\n")
            }
            append("DELIVER A CONCISE AUDIT (under 120 words):\n")
            append("1. Distinctiveness Rating (1-10)\n")
            append("2. Key Cadence Traits (rhythm, slang, sentence length, vocabulary)\n")
            append("3. Risk of sounding like generic filler or other characters\n")
            append("4. One specific punch-up tip to make this voice unforgettable.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, userPrompt)
    }

    suspend fun auditScene(
        config: AiConfig,
        sceneHeading: String,
        sceneBody: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("PERFORM A SCRIPT DOCTOR SCENE AUDIT for the following scene:\n\n")
            append("HEADING: $sceneHeading\n\n")
            append("BODY:\n$sceneBody\n\n")
            append("PROVIDE A BULLET-POINT REPORT ON:\n")
            append("• Dramatic Objective: Does the protagonist enter with a clear goal?\n")
            append("• Conflict & Turn: Where does the power dynamic shift or reverse?\n")
            append("• Pacing & Exposition: Any sluggish beats or unfilmable descriptions?\n")
            append("• Recommended Fixes: 2 sharp, actionable changes to elevate this scene.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, userPrompt)
    }

    suspend fun generateComplications(
        config: AiConfig,
        sceneHeading: String,
        sceneBody: String,
        characters: List<String>,
        count: Int = 4
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val userPrompt = buildString {
            append("Generate $count unexpected, organic complications or obstacles for this scene.\n")
            append("SCENE: $sceneHeading\n")
            if (characters.isNotEmpty()) {
                append("CHARACTERS PRESENT: ${characters.joinToString(", ")}\n")
            }
            append("SCENE DRAFT:\n$sceneBody\n\n")
            append("RULES:\n")
            append("1. Do not rely on cliché coincidences. Ground obstacles in character flaws, physical environments, or ticking clocks.\n")
            append("2. Provide exactly $count numbered items (1., 2., 3., 4.).\n")
            append("3. Each item should be 1-2 punchy sentences describing the complication.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, userPrompt).map { response ->
            response.lines()
                .filter { it.trim().matches(Regex("^\\d+[\\.\\)]\\s+.*")) }
                .map { it.replaceFirst(Regex("^\\d+[\\.\\)]\\s+"), "").trim() }
                .ifEmpty { listOf(response) }
        }
    }

    suspend fun chatWithDoctor(
        config: AiConfig,
        messages: List<AiChatMessage>,
        scriptContext: String,
        storyBibleContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val systemPrompt = buildString {
            append(HOLLYWOOD_SYSTEM_INSTRUCTION)
            append("\n\nYou are in an active creative collaboration session with the screenwriter. ")
            if (scriptContext.isNotBlank()) {
                append("\nCURRENT SCRIPT WORKING CONTEXT:\n$scriptContext\n")
            }
            if (storyBibleContext.isNotBlank()) {
                append("\nWORLD STORY BIBLE & LORE DOCUMENT (COSMOLOGY, ANTHROPOLOGY, TECHNOLOGY, CHRONOLOGY):\n")
                append(storyBibleContext)
                append("\n\nCONTINUITY DIRECTIVE: Actively track and cross-reference all dialogue, actions, and character choices against this Story Bible. Enforce cosmological rules, technology limits, faction protocols, and chronological history.\n")
            }
        }
        callLlmChat(config, systemPrompt, messages)
    }

    suspend fun auditSceneWithBible(
        config: AiConfig,
        sceneText: String,
        sceneHeading: String,
        storyBibleContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("AUDIT THIS SCENE AGAINST THE WORLD STORY BIBLE & LORE DOCUMENT.\n\n")
            append("SCENE HEADING: $sceneHeading\n\n")
            append("SCENE TEXT:\n$sceneText\n\n")
            append("STORY BIBLE & LORE REFERENCE:\n$storyBibleContext\n\n")
            append("PROVIDE A DETAILED 4-PILLAR CONTINUITY AUDIT (Concise, Hollywood Reader style):\n")
            append("1. ✨ COSMOLOGY: Are universe rules, metaphysical laws, and atmospheric conditions respected?\n")
            append("2. 🏛️ ANTHROPOLOGY: Are faction loyalties, cultural etiquette, and status dynamics honored?\n")
            append("3. ⚡ TECHNOLOGY: Are tools, weapons, and communications constrained by the lore's tech level?\n")
            append("4. ⏳ CHRONOLOGY: Does the scene's history and character age/backstory fit the timeline?\n")
            append("5. 💡 LORE INTEGRATION TIP: One way to seamlessly show (not tell) world lore through action or dialogue.")
        }
        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, prompt).map { cleanGeneratedBlock(it) }
    }

    suspend fun generateStudioCoverage(
        config: AiConfig,
        scriptText: String,
        title: String,
        author: String,
        storyBibleContext: String = ""
    ): Result<StudioCoverageReport> = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("Generate a professional Hollywood Studio Reader Coverage Report for the screenplay '$title' by $author.\n\n")
            if (storyBibleContext.isNotBlank()) {
                append("WORLD STORY BIBLE & LORE REFERENCE:\n$storyBibleContext\n\n")
            }
            append("SCRIPT EXCERPT / TEXT:\n$scriptText\n\n")
            append("OUTPUT STRICT JSON FORMAT ONLY (no markdown fences, raw JSON):\n")
            append("{\n")
            append("  \"logline\": \"One compelling sentence logline including protagonist, inciting incident, goal, and stakes.\",\n")
            append("  \"premiseScore\": \"X/10 - Short explanation\",\n")
            append("  \"synopsis\": \"3-4 paragraph narrative synopsis broken down into Act I, Act II, and Act III.\",\n")
            append("  \"dialogueReview\": \"Assessment of dialogue quality, voice distinction, and subtext.\",\n")
            append("  \"pacingReview\": \"Assessment of structure, act breaks, and dramatic momentum.\",\n")
            append("  \"commercialProspects\": \"Target audience, genre appeal, budget scope, and marketability.\",\n")
            append("  \"recommendation\": \"RECOMMEND\" | \"CONSIDER\" | \"PASS\",\n")
            append("  \"characterArcs\": [\n")
            append("    {\"name\": \"Character Name\", \"arc\": \"Summary of emotional flaw, crucible, and outcome\"}\n")
            append("  ]\n")
            append("}")
        }

        callLlm(config, HOLLYWOOD_SYSTEM_INSTRUCTION, prompt).map { rawJson ->
            parseStudioCoverageJson(rawJson, title, author)
        }
    }

    /**
     * Generates a concise 1-2 sentence dramatic summary for a corkboard index card.
     */
    suspend fun summarizeScene(
        config: AiConfig,
        sceneHeading: String,
        sceneText: String,
        storyBibleContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedHeading = sceneHeading.trim()
        val trimmedText = sceneText.trim()
        if (trimmedText.isBlank()) {
            return@withContext Result.success("Scene is currently being drafted.")
        }
        if (!config.isEnabled || !config.isConfigured) {
            return@withContext Result.success(generateHeuristicSceneSummary(trimmedHeading, trimmedText))
        }

        val systemInstruction = """
            You are a master Hollywood script doctor and story editor.
            Your job is to summarize a screenplay scene for a writer's corkboard index card.
            Provide a punchy, active 1-2 sentence dramatic summary.
            Focus on:
            1. Which character drives the scene and what objective they pursue.
            2. The immediate obstacle, conflict, or revelation.
            3. The dramatic turn, emotional beat, or cliffhanger that closes the scene.
            Keep it strictly under 35 words. Write in the present tense. Do NOT include meta-intro phrases like 'In this scene...'.
        """.trimIndent()

        val prompt = buildString {
            if (storyBibleContext.isNotBlank()) {
                append("STORY BIBLE & LORE REFERENCE:\n")
                append(storyBibleContext.take(1200))
                append("\n\n")
            }
            append("SCENE HEADING: $trimmedHeading\n\n")
            append("SCENE SCRIPT EXCERPT:\n")
            append(trimmedText.take(3500))
            append("\n\nOUTPUT: 1-2 sentence index card summary:")
        }

        callLlm(config, systemInstruction, prompt).map { raw ->
            val cleaned = raw.trim()
                .removeSurrounding("\"")
                .replace(Regex("^Summary:\\s*", RegexOption.IGNORE_CASE), "")
                .trim()
            if (cleaned.isNotBlank()) cleaned else generateHeuristicSceneSummary(trimmedHeading, trimmedText)
        }
    }

    fun generateHeuristicSceneSummary(sceneHeading: String, sceneText: String): String {
        val lines = sceneText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return "Scene set at $sceneHeading."
        val firstAction = lines.firstOrNull { !it.matches(Regex("^[A-Z0-9 .'-]+$")) && !it.startsWith("(") }
        if (firstAction != null && firstAction.length > 15) {
            return firstAction.take(140) + if (firstAction.length > 140) "..." else ""
        }
        return lines.take(2).joinToString(" ").take(140)
    }

    suspend fun formatDictatedSpeech(
        config: AiConfig,
        spokenText: String,
        sceneContext: String = "",
        activeCharacters: List<String> = emptyList()
    ): Result<List<ScreenplayBlock>> = withContext(Dispatchers.IO) {
        val trimmedSpeech = spokenText.trim()
        if (trimmedSpeech.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        if (!config.isEnabled || !config.enableVoiceDictation || !config.isConfigured) {
            // Offline / unconfigured fallback: format heuristically
            return@withContext Result.success(formatDictationOfflineFallback(trimmedSpeech, activeCharacters))
        }

        val userPrompt = buildString {
            append("FORMAT THIS RAW DICTATED SPEECH INTO INDUSTRY-STANDARD FOUNTAIN SCREENPLAY BLOCKS.\n\n")
            if (sceneContext.isNotBlank()) {
                append("PRECEDING SCRIPT CONTEXT:\n")
                append(sceneContext.takeLast(1000))
                append("\n\n")
            }
            if (activeCharacters.isNotEmpty()) {
                append("CHARACTERS IN THIS SCREENPLAY:\n")
                append(activeCharacters.take(15).joinToString(", "))
                append("\n\n")
            }
            append("RAW SPOKEN DICTATION:\n")
            append("\"").append(trimmedSpeech).append("\"\n\n")
            append("RULES:\n")
            append("1. Parse spoken words into screenplay elements: Scene Headings (e.g. INT. COFFEE SHOP - DAY), Action lines, Character cues, Parentheticals, and Spoken Dialogue.\n")
            append("2. When characters speak (e.g. 'Marcus says let's go' or 'Maya whispers don't look'), format character name in ALL CAPS on its own line, optional (parenthetical) on its own line, followed by the dialogue line.\n")
            append("3. Scene headings must start with INT. or EXT. or I/E. and be in ALL CAPS.\n")
            append("4. Clean up speech artifacts, apply proper capitalization, punctuation, and contractions.\n")
            append("5. Output ONLY clean Fountain text. Do not output conversational commentary, markdown fences (no ```), or quotes.")
        }

        val systemPrompt = "You are an elite Hollywood screenplay transcription engine for Seshat Studio. You convert raw spoken voice dictation into valid Fountain screenplay format with zero extra commentary."

        val responseResult = callLlm(config, systemPrompt, userPrompt)
        responseResult.map { rawResponse ->
            val cleanedFountain = cleanGeneratedBlock(rawResponse)
            val importedDoc = FountainSerializer.importFromFountain(cleanedFountain)
            if (importedDoc.blocks.isNotEmpty()) {
                importedDoc.blocks
            } else {
                formatDictationOfflineFallback(trimmedSpeech, activeCharacters)
            }
        }.recoverCatching {
            // Network, timeout, or API failure fallback
            formatDictationOfflineFallback(trimmedSpeech, activeCharacters)
        }
    }

    fun formatDictationOfflineFallback(
        spokenText: String,
        activeCharacters: List<String> = emptyList()
    ): List<ScreenplayBlock> {
        val trimmed = spokenText.trim()
        if (trimmed.isEmpty()) return emptyList()

        val upper = trimmed.uppercase(Locale.US)

        // 1. Scene Heading detection
        if (upper.startsWith("INT.") || upper.startsWith("EXT.") || upper.startsWith("INT ") ||
            upper.startsWith("EXT ") || upper.startsWith("I/E") || upper.startsWith("INTERIOR") ||
            upper.startsWith("EXTERIOR")) {
            val slug = when {
                upper.startsWith("INTERIOR ") -> "INT. " + upper.removePrefix("INTERIOR ")
                upper.startsWith("EXTERIOR ") -> "EXT. " + upper.removePrefix("EXTERIOR ")
                upper.startsWith("INT ") -> "INT. " + upper.removePrefix("INT ")
                upper.startsWith("EXT ") -> "EXT. " + upper.removePrefix("EXT ")
                else -> upper
            }
            return listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = slug))
        }

        // 2. Character dialogue detection
        for (char in activeCharacters) {
            val charUpper = char.uppercase(Locale.US)
            val colonPrefix = "$charUpper:"
            val saysPrefix = "$charUpper SAYS"
            val saidPrefix = "$charUpper SAID"

            if (upper.startsWith(colonPrefix)) {
                val dialogue = trimmed.drop(colonPrefix.length).trim()
                return listOf(
                    ScreenplayBlock(type = BlockType.CHARACTER, text = charUpper),
                    ScreenplayBlock(type = BlockType.DIALOGUE, text = dialogue.replaceFirstChar { it.uppercase() })
                )
            }
            if (upper.startsWith(saysPrefix)) {
                val dialogue = trimmed.drop(saysPrefix.length).trim()
                return listOf(
                    ScreenplayBlock(type = BlockType.CHARACTER, text = charUpper),
                    ScreenplayBlock(type = BlockType.DIALOGUE, text = dialogue.replaceFirstChar { it.uppercase() })
                )
            }
            if (upper.startsWith(saidPrefix)) {
                val dialogue = trimmed.drop(saidPrefix.length).trim()
                return listOf(
                    ScreenplayBlock(type = BlockType.CHARACTER, text = charUpper),
                    ScreenplayBlock(type = BlockType.DIALOGUE, text = dialogue.replaceFirstChar { it.uppercase() })
                )
            }
        }

        // 3. Fallback: Action line with punctuation
        val capitalized = trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        val withPeriod = if (!capitalized.endsWith(".") && !capitalized.endsWith("!") && !capitalized.endsWith("?")) {
            "$capitalized."
        } else {
            capitalized
        }
        return listOf(ScreenplayBlock(type = BlockType.ACTION, text = withPeriod))
    }

    // Core HTTP caller
    fun callLlm(config: AiConfig, systemPrompt: String, userPrompt: String): Result<String> {
        val singleMessage = listOf(AiChatMessage(role = "user", content = userPrompt))
        return callLlmChat(config, systemPrompt, singleMessage)
    }

    private fun callLlmChat(config: AiConfig, systemPrompt: String, messages: List<AiChatMessage>): Result<String> {
        return try {
            when (config.provider) {
                AiProvider.GOOGLE_GEMINI -> callGoogleGemini(config, systemPrompt, messages)
                AiProvider.OPENAI -> callOpenAiCompatible(config, "https://api.openai.com/v1", systemPrompt, messages)
                AiProvider.ANTHROPIC_CLAUDE -> callAnthropicClaude(config, systemPrompt, messages)
                AiProvider.CUSTOM_LOCAL -> callOpenAiCompatible(config, config.customEndpoint.trimEnd('/'), systemPrompt, messages)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGoogleGemini(
        config: AiConfig,
        systemPrompt: String,
        messages: List<AiChatMessage>
    ): Result<String> {
        if (config.apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Google Gemini API key is missing. Enter your key in Studio Settings."))
        }
        val model = if (config.modelName.isNotBlank()) config.modelName else "gemini-3.8-flash"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${config.apiKey.trim()}"

        val rootJson = JSONObject()

        // System Instruction
        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray().put(JSONObject().put("text", systemPrompt))
            sysObj.put("parts", sysParts)
            rootJson.put("system_instruction", sysObj)
        }

        // Contents
        val contentsArray = JSONArray()
        for (msg in messages) {
            val contentObj = JSONObject()
            val role = if (msg.role == "model" || msg.role == "assistant") "model" else "user"
            contentObj.put("role", role)
            val parts = JSONArray().put(JSONObject().put("text", msg.content))
            contentObj.put("parts", parts)
            contentsArray.put(contentObj)
        }
        rootJson.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", config.temperature)
        rootJson.put("generationConfig", genConfig)

        val (statusCode, responseText) = executeHttpPost(endpoint, rootJson.toString(), emptyMap())
        if (statusCode !in 200..299) {
            val errorMsg = extractErrorMessage(responseText)
            return Result.failure(Exception("Gemini API Error ($statusCode): $errorMsg"))
        }

        val respJson = JSONObject(responseText)
        val candidates = respJson.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return Result.failure(Exception("Gemini returned no response candidates."))
        }
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts == null || parts.length() == 0) {
            return Result.failure(Exception("Gemini returned empty content parts."))
        }
        val text = parts.getJSONObject(0).optString("text", "")
        return Result.success(text.trim())
    }

    private fun callOpenAiCompatible(
        config: AiConfig,
        baseEndpoint: String,
        systemPrompt: String,
        messages: List<AiChatMessage>
    ): Result<String> {
        val endpoint = if (baseEndpoint.endsWith("/chat/completions")) {
            baseEndpoint
        } else {
            "$baseEndpoint/chat/completions"
        }

        val rootJson = JSONObject()
        val model = if (config.modelName.isNotBlank()) config.modelName else "gpt-4o"
        rootJson.put("model", model)
        rootJson.put("temperature", config.temperature)

        val msgArray = JSONArray()
        if (systemPrompt.isNotBlank()) {
            msgArray.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        for (m in messages) {
            val role = if (m.role == "model" || m.role == "assistant") "assistant" else "user"
            msgArray.put(JSONObject().put("role", role).put("content", m.content))
        }
        rootJson.put("messages", msgArray)

        val headers = mutableMapOf<String, String>()
        if (config.apiKey.isNotBlank()) {
            headers["Authorization"] = "Bearer ${config.apiKey.trim()}"
        }

        val (statusCode, responseText) = executeHttpPost(endpoint, rootJson.toString(), headers)
        if (statusCode !in 200..299) {
            val errorMsg = extractErrorMessage(responseText)
            return Result.failure(Exception("API Error ($statusCode): $errorMsg"))
        }

        val respJson = JSONObject(responseText)
        val choices = respJson.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            return Result.failure(Exception("OpenAI returned no response choices."))
        }
        val messageObj = choices.getJSONObject(0).optJSONObject("message")
        val content = messageObj?.optString("content", "") ?: ""
        return Result.success(content.trim())
    }

    private fun callAnthropicClaude(
        config: AiConfig,
        systemPrompt: String,
        messages: List<AiChatMessage>
    ): Result<String> {
        if (config.apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Anthropic API key is missing. Enter your key in Studio Settings."))
        }
        val endpoint = "https://api.anthropic.com/v1/messages"
        val model = if (config.modelName.isNotBlank()) config.modelName else "claude-3-5-sonnet-20241022"

        val rootJson = JSONObject()
        rootJson.put("model", model)
        rootJson.put("max_tokens", 4096)
        rootJson.put("temperature", config.temperature)
        if (systemPrompt.isNotBlank()) {
            rootJson.put("system", systemPrompt)
        }

        val msgArray = JSONArray()
        for (m in messages) {
            val role = if (m.role == "model" || m.role == "assistant") "assistant" else "user"
            msgArray.put(JSONObject().put("role", role).put("content", m.content))
        }
        rootJson.put("messages", msgArray)

        val headers = mapOf(
            "x-api-key" to config.apiKey.trim(),
            "anthropic-version" to "2023-06-01"
        )

        val (statusCode, responseText) = executeHttpPost(endpoint, rootJson.toString(), headers)
        if (statusCode !in 200..299) {
            val errorMsg = extractErrorMessage(responseText)
            return Result.failure(Exception("Claude API Error ($statusCode): $errorMsg"))
        }

        val respJson = JSONObject(responseText)
        val contentArr = respJson.optJSONArray("content")
        if (contentArr == null || contentArr.length() == 0) {
            return Result.failure(Exception("Claude returned empty content."))
        }
        val firstBlock = contentArr.getJSONObject(0)
        val text = firstBlock.optString("text", "")
        return Result.success(text.trim())
    }

    private fun executeHttpPost(
        endpointUrl: String,
        jsonBody: String,
        customHeaders: Map<String, String>
    ): Pair<Int, String> {
        val url = URL(endpointUrl)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.doInput = true
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            for ((k, v) in customHeaders) {
                conn.setRequestProperty(k, v)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val statusCode = conn.responseCode
            val inputStream = if (statusCode in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
            val responseBody = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
            return Pair(statusCode, responseBody)
        } finally {
            conn.disconnect()
        }
    }

    private fun extractErrorMessage(responseJson: String): String {
        return try {
            val obj = JSONObject(responseJson)
            val err = obj.optJSONObject("error")
            if (err != null) {
                err.optString("message", responseJson)
            } else {
                obj.optString("message", responseJson)
            }
        } catch (_: Exception) {
            responseJson.take(200)
        }
    }

    private fun cleanGeneratedBlock(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```") && text.endsWith("```")) {
            val firstLine = text.indexOf('\n')
            val lastFence = text.lastIndexOf("```")
            if (firstLine != -1 && lastFence > firstLine) {
                text = text.substring(firstLine + 1, lastFence).trim()
            }
        }
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("“") && text.endsWith("”"))) {
            text = text.substring(1, text.length - 1).trim()
        }
        return text
    }

    fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.contains("```json")) {
            val start = text.indexOf("```json") + 7
            val end = text.indexOf("```", start)
            if (end != -1) text = text.substring(start, end).trim()
        } else if (text.contains("```")) {
            val start = text.indexOf("```") + 3
            val end = text.indexOf("```", start)
            if (end != -1) text = text.substring(start, end).trim()
        }
        return text
    }

    private fun parseStudioCoverageJson(raw: String, title: String, author: String): StudioCoverageReport {
        var cleanJson = raw.trim()
        if (cleanJson.contains("```json")) {
            val start = cleanJson.indexOf("```json") + 7
            val end = cleanJson.indexOf("```", start)
            if (end != -1) {
                cleanJson = cleanJson.substring(start, end).trim()
            }
        } else if (cleanJson.contains("```")) {
            val start = cleanJson.indexOf("```") + 3
            val end = cleanJson.indexOf("```", start)
            if (end != -1) {
                cleanJson = cleanJson.substring(start, end).trim()
            }
        }

        val json = try {
            JSONObject(cleanJson)
        } catch (_: Exception) {
            JSONObject()
        }

        val logline = json.optString("logline", "A compelling screenplay narrative following characters through high-stakes conflicts.")
        val premiseScore = json.optString("premiseScore", "8.0 / 10 - Strong High Concept")
        val synopsis = json.optString("synopsis", "Act I establishes the protagonists and world. Act II escalates complications. Act III brings a decisive climax.")
        val dialogueReview = json.optString("dialogueReview", "Dialogue shows distinct vocal rhythms with layered subtext across principal scenes.")
        val pacingReview = json.optString("pacingReview", "Story moves with effective momentum and clear sequence turning points.")
        val commercialProspects = json.optString("commercialProspects", "Strong market potential within its genre with clear target demographic appeal.")
        val recStr = json.optString("recommendation", "CONSIDER").uppercase()
        val recommendation = when {
            recStr.contains("RECOMMEND") -> CoverageVerdict.RECOMMEND
            recStr.contains("PASS") -> CoverageVerdict.PASS
            else -> CoverageVerdict.CONSIDER
        }

        val charArcs = mutableListOf<CharacterArc>()
        val arcsArray = json.optJSONArray("characterArcs")
        if (arcsArray != null) {
            for (i in 0 until arcsArray.length()) {
                val item = arcsArray.optJSONObject(i)
                if (item != null) {
                    val name = item.optString("name", "Character")
                    val arc = item.optString("arc", "Dynamic emotional transformation.")
                    charArcs.add(CharacterArc(name, arc))
                }
            }
        }

        return StudioCoverageReport(
            scriptTitle = title.ifBlank { "UNTITLED SCREENPLAY" },
            author = author.ifBlank { "ANONYMOUS" },
            logline = logline,
            premiseScore = premiseScore,
            synopsis = synopsis,
            dialogueReview = dialogueReview,
            pacingReview = pacingReview,
            commercialProspects = commercialProspects,
            recommendation = recommendation,
            characterArcs = charArcs
        )
    }
}
