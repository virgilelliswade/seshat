package com.seshat.writer.firstdraft

import android.content.Context
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Strict one-way sequential state machine for the "First Draft" Anti-Procrastination Engine.
 */
enum class FirstDraftStage(val label: String, val badge: String) {
    SPARK("SPARK", "⚡ SPARK"),
    BIBLE("BIBLE", "📖 BIBLE"),
    OUTLINE("OUTLINE", "📌 OUTLINE"),
    DRAFT("DRAFT", "🔥 DRAFT"),
    STUDIO_UNLOCKED("STUDIO UNLOCKED", "🔓 STUDIO UNLOCKED");

    fun nextStage(): FirstDraftStage = when (this) {
        SPARK -> BIBLE
        BIBLE -> OUTLINE
        OUTLINE -> DRAFT
        DRAFT -> STUDIO_UNLOCKED
        STUDIO_UNLOCKED -> STUDIO_UNLOCKED
    }
}

/**
 * XP Shop items purchasable strictly with forward-momentum XP earned in DRAFT stage.
 */
enum class XpShopItem(
    val id: String,
    val title: String,
    val costXp: Int,
    val description: String,
    val durationMs: Long
) {
    AIRLOCK_PASS(
        id = "airlock_pass",
        title = "Airlock Pass",
        costXp = 2500,
        description = "Unlocks BIBLE for 15 minutes of active session time, then re-locks.",
        durationMs = 15L * 60L * 1000L
    ),
    MULLIGAN(
        id = "mulligan",
        title = "Mulligan",
        costXp = 5000,
        description = "Unlocks preceding locked scenes/chapters for 20 minutes of active session time.",
        durationMs = 20L * 60L * 1000L
    ),
    FREEZE_TOKEN(
        id = "freeze_token",
        title = "Freeze Token",
        costXp = 10000,
        description = "Pauses the 48-hour Decay clock for 48 hours.",
        durationMs = 48L * 60L * 60L * 1000L
    )
}

/**
 * Single weekly work/commute time block in the "Day Job" Duty Roster.
 * [dayOfWeek] follows Calendar constants: Calendar.SUNDAY (1) .. Calendar.SATURDAY (7).
 */
data class WorkShiftBlock(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: Int = Calendar.MONDAY,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val label: String = "Day Job / Commute"
) {
    fun durationMinutes(): Int {
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute
        return if (endTotal >= startTotal) {
            endTotal - startTotal
        } else {
            (24 * 60 - startTotal) + endTotal
        }
    }

    fun isActiveAt(calDayOfWeek: Int, hour: Int, minute: Int): Boolean {
        if (calDayOfWeek != dayOfWeek) return false
        val currentTotal = hour * 60 + minute
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute
        return if (endTotal >= startTotal) {
            currentTotal in startTotal until endTotal
        } else {
            currentTotal >= startTotal || currentTotal < endTotal
        }
    }

    fun formattedDayName(): String = when (dayOfWeek) {
        Calendar.SUNDAY -> "SUN"
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        Calendar.SATURDAY -> "SAT"
        else -> "MON"
    }

    fun formattedStartTime(): String = String.format(Locale.US, "%02d:%02d", startHour, startMinute)
    fun formattedEndTime(): String = String.format(Locale.US, "%02d:%02d", endHour, endMinute)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("dayOfWeek", dayOfWeek)
        put("startHour", startHour)
        put("startMinute", startMinute)
        put("endHour", endHour)
        put("endMinute", endMinute)
        put("label", label)
    }

    companion object {
        fun fromJson(json: JSONObject): WorkShiftBlock = WorkShiftBlock(
            id = json.optString("id", UUID.randomUUID().toString()),
            dayOfWeek = json.optInt("dayOfWeek", Calendar.MONDAY),
            startHour = json.optInt("startHour", 9),
            startMinute = json.optInt("startMinute", 0),
            endHour = json.optInt("endHour", 17),
            endMinute = json.optInt("endMinute", 0),
            label = json.optString("label", "Day Job / Commute")
        )
    }
}

/**
 * The "Day Job" Duty Roster (WorkShiftFreeze).
 * Enforces Sunday Roster Lock (initial project creation or Sundays 08:00-20:00 local time)
 * and a strict 50-hour (3,000-minute) weekly hard cap.
 */
data class WorkShiftFreeze(
    val shifts: List<WorkShiftBlock> = emptyList(),
    val isReadOnly: Boolean = false,
    val lockedUntilTimestamp: Long = 0L,
    val isInitialSetupComplete: Boolean = false
) {
    fun totalMinutesScheduled(): Int = shifts.sumOf { it.durationMinutes() }
    fun totalHoursScheduled(): Float = totalMinutesScheduled() / 60f

    fun canEditRoster(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isInitialSetupComplete && !isReadOnly) return true
        if (nowMs < lockedUntilTimestamp) return false
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return day == Calendar.SUNDAY && hour in 8 until 20
    }

    fun getActiveShift(nowMs: Long = System.currentTimeMillis()): WorkShiftBlock? {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return shifts.firstOrNull { it.isActiveAt(day, hour, minute) }
    }

    fun activeShiftAt(nowMs: Long = System.currentTimeMillis()): WorkShiftBlock? = getActiveShift(nowMs)

    fun toJson(): JSONObject = JSONObject().apply {
        val arr = JSONArray()
        shifts.forEach { arr.put(it.toJson()) }
        put("shifts", arr)
        put("isReadOnly", isReadOnly)
        put("lockedUntilTimestamp", lockedUntilTimestamp)
        put("isInitialSetupComplete", isInitialSetupComplete)
    }

    companion object {
        const val MAX_WEEKLY_MINUTES = 50 * 60 // 50 hours hard cap

        fun fromJson(json: JSONObject?): WorkShiftFreeze {
            if (json == null) return WorkShiftFreeze()
            val arr = json.optJSONArray("shifts") ?: JSONArray()
            val list = mutableListOf<WorkShiftBlock>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(WorkShiftBlock.fromJson(obj))
            }
            return WorkShiftFreeze(
                shifts = list,
                isReadOnly = json.optBoolean("isReadOnly", false),
                lockedUntilTimestamp = json.optLong("lockedUntilTimestamp", 0L),
                isInitialSetupComplete = json.optBoolean("isInitialSetupComplete", false)
            )
        }
    }
}

/**
 * One-time Stage-Gate High-Five Receipt payload generated at the exact millisecond
 * a user permanently locks SPARK, BIBLE, or OUTLINE into isReadOnly = true.
 */
data class StageGateReceiptData(
    val stageSealed: FirstDraftStage,
    val timeRemainingFormatted: String,
    val nextStageDeadlineFormatted: String,
    val sealedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("stageSealed", stageSealed.name)
        put("timeRemainingFormatted", timeRemainingFormatted)
        put("nextStageDeadlineFormatted", nextStageDeadlineFormatted)
        put("sealedAtTimestamp", sealedAtTimestamp)
    }

    companion object {
        fun fromJson(json: JSONObject?): StageGateReceiptData? {
            if (json == null) return null
            val stageName = json.optString("stageSealed", "")
            val stage = FirstDraftStage.entries.find { it.name == stageName } ?: return null
            return StageGateReceiptData(
                stageSealed = stage,
                timeRemainingFormatted = json.optString("timeRemainingFormatted", "00:00:00"),
                nextStageDeadlineFormatted = json.optString("nextStageDeadlineFormatted", "N/A"),
                sealedAtTimestamp = json.optLong("sealedAtTimestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Complete persistent state of the "First Draft" Anti-Procrastination Engine.
 * Serialized into .seshat project bundle metadata and local storage.
 */
data class FirstDraftState(
    val isFirstDraftMode: Boolean = true,
    val stage: FirstDraftStage = FirstDraftStage.SPARK,
    val projectFormat: ProjectFormat = ProjectFormat.SCREENPLAY,
    val screenplayType: ScreenplayType = ScreenplayType.FEATURE,

    // 1. SPARK Stage State & Active Timer
    val sparkLogline: String = "",
    val sparkPilotLogline: String = "",
    val sparkActiveRemainingMs: Long = 30L * 60L * 1000L,
    val isSparkReadOnly: Boolean = false,

    // 2. Wall-Clock Stage Deadlines & One-Way Airlock Flags
    val stageStartWallClockMs: Long = System.currentTimeMillis(),
    val stageDeadlineWallClockMs: Long = System.currentTimeMillis() + 48L * 3600L * 1000L,
    val isBibleReadOnly: Boolean = false,
    val isOutlineReadOnly: Boolean = false,

    // 3. XP Currency & Shop Timers
    val xpBalance: Int = 0,
    val airlockPassRemainingMs: Long = 0L,
    val mulliganPassRemainingMs: Long = 0L,
    val freezeTokenUntilWallClockMs: Long = 0L,
    val loreExpansionPassUntilWallClockMs: Long = 0L,
    val activeSprintRemainingMs: Long = 25L * 60L * 1000L,
    val isSprintRunning: Boolean = false,
    val currentSceneBackspaceCount: Int = 0,
    val totalBackspaces: Int = 0,
    val totalKeystrokes: Int = 0,
    val highestBlockCountRewarded: Int = 1,
    val highestSceneCountRewarded: Int = 1,

    // 4. 48-Hour Decay & Survival Quota Engine
    val lastQuotaMetTimestamp: Long = System.currentTimeMillis(),
    val wordsAtQuotaWindowStart: Int = 0,
    val wordsAtDayStart: Int = 0,
    val dayStartTimestamp: Long = System.currentTimeMillis(),
    val dailyQuotaMet: Boolean = false,
    val activeStreakDays: Int = 1,
    val isDecayed: Boolean = false,
    val penaltyScratchpadText: String = "",
    val penaltySprintRemainingMs: Long = 25L * 60L * 1000L,

    // 5. Gemini AI Bouncer / Idle Instigator
    val idleSecondsInDraft: Int = 0,
    val idleInstigatorBannerText: String? = null,
    val isIdleInstigatorLoading: Boolean = false,

    // 6. Medium-Specific Fog of War, Act-Gates, Chapter Airlocks & Loot Drops
    val unlockedCorkboardSceneSlots: Int = 5,
    val tvUnlockedActIndex: Int = 0,
    val tvEpisode101Unlocked: Boolean = false,
    val novelUnlockedChapterIndex: Int = 0,
    val isTtsLootUnlocked: Boolean = false,
    val isExtraDossiersAndPortraitsUnlocked: Boolean = false,
    val isSceneDoctorLootUnlocked: Boolean = false,
    val isNovelCoverPassAvailable: Boolean = false,
    val isNovelCoverPassUsed: Boolean = false,
    val isNovelPlotTwistAiUnlocked: Boolean = false,
    val lastLootToastMessage: String? = null,

    // 7. Social Accountability Receipts
    val lastShareTimestamp: Long = 0L,
    val pendingStageGateReceipt: StageGateReceiptData? = null,
    val pendingDecayReceipt: Boolean = false,

    // 8. Reference Drawer & Duty Roster
    val isReferenceDrawerOpen: Boolean = false,
    val workShiftFreeze: WorkShiftFreeze = WorkShiftFreeze()
) {
    /**
     * True whenever the project is in First Draft Mode and has not yet unlocked the full studio.
     */
    val isArcadeLocked: Boolean
        get() = isFirstDraftMode && stage != FirstDraftStage.STUDIO_UNLOCKED

    /**
     * Checks if the Story Bible is currently editable (either in BIBLE stage, STUDIO_UNLOCKED,
     * or temporarily unlocked via Airlock Pass or Lore Expansion Pass).
     */
    fun canEditBible(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isFirstDraftMode || stage == FirstDraftStage.STUDIO_UNLOCKED) return true
        if (stage == FirstDraftStage.BIBLE && !isBibleReadOnly) return true
        if (airlockPassRemainingMs > 0L) return true
        if (nowMs < loreExpansionPassUntilWallClockMs) return true
        return false
    }

    /**
     * Checks if the Corkboard Outline is currently editable.
     */
    fun canEditOutline(): Boolean {
        if (!isFirstDraftMode || stage == FirstDraftStage.STUDIO_UNLOCKED) return true
        if (stage == FirstDraftStage.OUTLINE && !isOutlineReadOnly) return true
        if (stage == FirstDraftStage.DRAFT) return true // Fog of war slots are editable in DRAFT
        return false
    }

    /**
     * Checks if preceding blocks/chapters are temporarily unlocked via Mulligan Pass.
     */
    fun isMulliganActive(): Boolean = !isArcadeLocked || mulliganPassRemainingMs > 0L

    val idleInstigatorPrompt: String?
        get() = idleInstigatorBannerText

    fun maxTotalLoreWords(): Int {
        if (!isArcadeLocked) return 999999
        return if (projectFormat == ProjectFormat.NOVEL) 2000 else 1000
    }

    /**
     * Maximum Character Dossier slots allowed for the current medium & loot progress.
     */
    fun maxCharacterDossierSlots(): Int {
        if (!isArcadeLocked) return 999
        return when {
            projectFormat == ProjectFormat.TV_SERIES ||
                screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
                screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY -> 5
            projectFormat == ProjectFormat.NOVEL -> 4
            else -> if (isExtraDossiersAndPortraitsUnlocked) 5 else 3
        }
    }

    /**
     * Maximum words per Character Dossier in First Draft mode.
     */
    fun maxWordsPerCharacterDossier(): Int {
        if (!isArcadeLocked) return 99999
        return if (projectFormat == ProjectFormat.NOVEL) 200 else 150
    }

    /**
     * Maximum words per Lore Pillar in First Draft mode.
     */
    fun maxWordsPerLorePillar(): Int {
        if (!isArcadeLocked) return 99999
        return if (projectFormat == ProjectFormat.NOVEL) 500 else 250
    }

    /**
     * Maximum words for TV Season 1 Arc summary.
     */
    fun maxWordsTvSeasonArc(): Int {
        if (!isArcadeLocked) return 99999
        return 300
    }

    /**
     * Whether AI Character Portraits are unlocked.
     */
    fun canUseAiCharacterPortraits(): Boolean {
        if (!isArcadeLocked) return true
        return isExtraDossiersAndPortraitsUnlocked
    }

    /**
     * Whether Multi-Voice Table Read (TTS) is unlocked.
     */
    fun canUseTableRead(): Boolean {
        if (!isArcadeLocked) return true
        return isTtsLootUnlocked
    }

    /**
     * Whether AI Scene Doctor / Plot Twist Brainstormer is unlocked.
     */
    fun canUseAiDoctor(): Boolean {
        if (!isArcadeLocked) return true
        return if (projectFormat == ProjectFormat.NOVEL) {
            isNovelPlotTwistAiUnlocked
        } else {
            isSceneDoctorLootUnlocked
        }
    }

    /**
     * Whether Novel Cover Generator is unlocked.
     */
    fun canUseNovelCoverGenerator(): Boolean {
        if (!isArcadeLocked) return true
        return isNovelCoverPassAvailable && !isNovelCoverPassUsed
    }

    /**
     * Survival quota required every 48 hours in DRAFT state.
     */
    fun survivalQuotaWords(): Int = when {
        projectFormat == ProjectFormat.TV_SERIES ||
            screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
            screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY -> 500
        projectFormat == ProjectFormat.NOVEL -> 1000
        else -> 600
    }

    /**
     * Spark logline max words for current medium.
     */
    fun maxSparkWords(): Int = if (projectFormat == ProjectFormat.NOVEL) 100 else 50

    /**
     * Remaining hours until 48-hour Decay triggers.
     */
    fun hoursUntilDecay(nowMs: Long = System.currentTimeMillis()): Float {
        if (nowMs < freezeTokenUntilWallClockMs) {
            val freezeRem = freezeTokenUntilWallClockMs - nowMs
            return (48f + (freezeRem / 3600000f))
        }
        val elapsedMs = (nowMs - lastQuotaMetTimestamp).coerceAtLeast(0L)
        val totalWindowMs = 48L * 3600L * 1000L
        val remainingMs = (totalWindowMs - elapsedMs).coerceAtLeast(0L)
        return remainingMs / 3600000f
    }

    /**
     * Backspace / Shame Ratio percentage.
     */
    fun backspaceShameRatioPercent(): Float {
        if (totalKeystrokes <= 0) return 0f
        return ((totalBackspaces.toFloat() / totalKeystrokes.toFloat()) * 100f).coerceIn(0f, 100f)
    }

    /**
     * Whether the Daily Draft Receipt Share button can be clicked (requires dailyQuotaMet and 24h cooldown).
     */
    fun canShareDailyReceipt(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (stage != FirstDraftStage.DRAFT && stage != FirstDraftStage.STUDIO_UNLOCKED) return false
        if (!dailyQuotaMet) return false
        return (nowMs - lastShareTimestamp) >= 86_400_000L
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("isFirstDraftMode", isFirstDraftMode)
        put("stage", stage.name)
        put("projectFormat", projectFormat.name)
        put("screenplayType", screenplayType.name)
        put("sparkLogline", sparkLogline)
        put("sparkPilotLogline", sparkPilotLogline)
        put("sparkActiveRemainingMs", sparkActiveRemainingMs)
        put("isSparkReadOnly", isSparkReadOnly)
        put("stageStartWallClockMs", stageStartWallClockMs)
        put("stageDeadlineWallClockMs", stageDeadlineWallClockMs)
        put("isBibleReadOnly", isBibleReadOnly)
        put("isOutlineReadOnly", isOutlineReadOnly)
        put("xpBalance", xpBalance)
        put("airlockPassRemainingMs", airlockPassRemainingMs)
        put("mulliganPassRemainingMs", mulliganPassRemainingMs)
        put("freezeTokenUntilWallClockMs", freezeTokenUntilWallClockMs)
        put("loreExpansionPassUntilWallClockMs", loreExpansionPassUntilWallClockMs)
        put("activeSprintRemainingMs", activeSprintRemainingMs)
        put("isSprintRunning", isSprintRunning)
        put("currentSceneBackspaceCount", currentSceneBackspaceCount)
        put("totalBackspaces", totalBackspaces)
        put("totalKeystrokes", totalKeystrokes)
        put("highestBlockCountRewarded", highestBlockCountRewarded)
        put("highestSceneCountRewarded", highestSceneCountRewarded)
        put("lastQuotaMetTimestamp", lastQuotaMetTimestamp)
        put("wordsAtQuotaWindowStart", wordsAtQuotaWindowStart)
        put("wordsAtDayStart", wordsAtDayStart)
        put("dayStartTimestamp", dayStartTimestamp)
        put("dailyQuotaMet", dailyQuotaMet)
        put("activeStreakDays", activeStreakDays)
        put("isDecayed", isDecayed)
        put("penaltyScratchpadText", penaltyScratchpadText)
        put("penaltySprintRemainingMs", penaltySprintRemainingMs)
        put("unlockedCorkboardSceneSlots", unlockedCorkboardSceneSlots)
        put("tvUnlockedActIndex", tvUnlockedActIndex)
        put("tvEpisode101Unlocked", tvEpisode101Unlocked)
        put("novelUnlockedChapterIndex", novelUnlockedChapterIndex)
        put("isTtsLootUnlocked", isTtsLootUnlocked)
        put("isExtraDossiersAndPortraitsUnlocked", isExtraDossiersAndPortraitsUnlocked)
        put("isSceneDoctorLootUnlocked", isSceneDoctorLootUnlocked)
        put("isNovelCoverPassAvailable", isNovelCoverPassAvailable)
        put("isNovelCoverPassUsed", isNovelCoverPassUsed)
        put("isNovelPlotTwistAiUnlocked", isNovelPlotTwistAiUnlocked)
        put("lastShareTimestamp", lastShareTimestamp)
        if (pendingStageGateReceipt != null) {
            put("pendingStageGateReceipt", pendingStageGateReceipt.toJson())
        }
        put("pendingDecayReceipt", pendingDecayReceipt)
        put("isReferenceDrawerOpen", isReferenceDrawerOpen)
        put("workShiftFreeze", workShiftFreeze.toJson())
    }

    companion object {
        fun fromJson(json: JSONObject?): FirstDraftState {
            if (json == null) return FirstDraftState()
            val stageStr = json.optString("stage", FirstDraftStage.SPARK.name)
            val stage = FirstDraftStage.entries.find { it.name == stageStr } ?: FirstDraftStage.SPARK
            val fmtStr = json.optString("projectFormat", ProjectFormat.SCREENPLAY.name)
            val format = ProjectFormat.entries.find { it.name == fmtStr } ?: ProjectFormat.SCREENPLAY
            val stStr = json.optString("screenplayType", ScreenplayType.FEATURE.name)
            val sType = ScreenplayType.entries.find { it.name == stStr } ?: ScreenplayType.FEATURE

            return FirstDraftState(
                isFirstDraftMode = json.optBoolean("isFirstDraftMode", true),
                stage = stage,
                projectFormat = format,
                screenplayType = sType,
                sparkLogline = json.optString("sparkLogline", ""),
                sparkPilotLogline = json.optString("sparkPilotLogline", ""),
                sparkActiveRemainingMs = json.optLong("sparkActiveRemainingMs", 30L * 60L * 1000L),
                isSparkReadOnly = json.optBoolean("isSparkReadOnly", false),
                stageStartWallClockMs = json.optLong("stageStartWallClockMs", System.currentTimeMillis()),
                stageDeadlineWallClockMs = json.optLong("stageDeadlineWallClockMs", System.currentTimeMillis() + 48L * 3600L * 1000L),
                isBibleReadOnly = json.optBoolean("isBibleReadOnly", false),
                isOutlineReadOnly = json.optBoolean("isOutlineReadOnly", false),
                xpBalance = json.optInt("xpBalance", 0),
                airlockPassRemainingMs = json.optLong("airlockPassRemainingMs", 0L),
                mulliganPassRemainingMs = json.optLong("mulliganPassRemainingMs", 0L),
                freezeTokenUntilWallClockMs = json.optLong("freezeTokenUntilWallClockMs", 0L),
                loreExpansionPassUntilWallClockMs = json.optLong("loreExpansionPassUntilWallClockMs", 0L),
                activeSprintRemainingMs = json.optLong("activeSprintRemainingMs", 25L * 60L * 1000L),
                isSprintRunning = json.optBoolean("isSprintRunning", false),
                currentSceneBackspaceCount = json.optInt("currentSceneBackspaceCount", 0),
                totalBackspaces = json.optInt("totalBackspaces", 0),
                totalKeystrokes = json.optInt("totalKeystrokes", 0),
                highestBlockCountRewarded = json.optInt("highestBlockCountRewarded", 1),
                highestSceneCountRewarded = json.optInt("highestSceneCountRewarded", 1),
                lastQuotaMetTimestamp = json.optLong("lastQuotaMetTimestamp", System.currentTimeMillis()),
                wordsAtQuotaWindowStart = json.optInt("wordsAtQuotaWindowStart", 0),
                wordsAtDayStart = json.optInt("wordsAtDayStart", 0),
                dayStartTimestamp = json.optLong("dayStartTimestamp", System.currentTimeMillis()),
                dailyQuotaMet = json.optBoolean("dailyQuotaMet", false),
                activeStreakDays = json.optInt("activeStreakDays", 1),
                isDecayed = json.optBoolean("isDecayed", false),
                penaltyScratchpadText = json.optString("penaltyScratchpadText", ""),
                penaltySprintRemainingMs = json.optLong("penaltySprintRemainingMs", 25L * 60L * 1000L),
                unlockedCorkboardSceneSlots = json.optInt("unlockedCorkboardSceneSlots", 5),
                tvUnlockedActIndex = json.optInt("tvUnlockedActIndex", 0),
                tvEpisode101Unlocked = json.optBoolean("tvEpisode101Unlocked", false),
                novelUnlockedChapterIndex = json.optInt("novelUnlockedChapterIndex", 0),
                isTtsLootUnlocked = json.optBoolean("isTtsLootUnlocked", false),
                isExtraDossiersAndPortraitsUnlocked = json.optBoolean("isExtraDossiersAndPortraitsUnlocked", false),
                isSceneDoctorLootUnlocked = json.optBoolean("isSceneDoctorLootUnlocked", false),
                isNovelCoverPassAvailable = json.optBoolean("isNovelCoverPassAvailable", false),
                isNovelCoverPassUsed = json.optBoolean("isNovelCoverPassUsed", false),
                isNovelPlotTwistAiUnlocked = json.optBoolean("isNovelPlotTwistAiUnlocked", false),
                lastShareTimestamp = json.optLong("lastShareTimestamp", 0L),
                pendingStageGateReceipt = StageGateReceiptData.fromJson(json.optJSONObject("pendingStageGateReceipt")),
                pendingDecayReceipt = json.optBoolean("pendingDecayReceipt", false),
                isReferenceDrawerOpen = json.optBoolean("isReferenceDrawerOpen", false),
                workShiftFreeze = WorkShiftFreeze.fromJson(json.optJSONObject("workShiftFreeze"))
            )
        }
    }
}

/**
 * Core State Machine & Business Rules Controller for the First Draft Anti-Procrastination Engine.
 */
object FirstDraftController {

    private const val FIRST_DRAFT_DIR = "first_draft_states"

    fun createForNewProject(
        format: ProjectFormat,
        screenplayType: ScreenplayType = ScreenplayType.FEATURE,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        val isTv = format == ProjectFormat.TV_SERIES ||
            screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
            screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY
        val sparkDurationMs = if (isTv) 60L * 60L * 1000L else 30L * 60L * 1000L
        val initialSceneSlots = when {
            isTv -> 3
            format == ProjectFormat.NOVEL -> 3
            else -> 5
        }
        return FirstDraftState(
            isFirstDraftMode = true,
            stage = FirstDraftStage.SPARK,
            projectFormat = format,
            screenplayType = screenplayType,
            sparkActiveRemainingMs = sparkDurationMs,
            stageStartWallClockMs = nowMs,
            stageDeadlineWallClockMs = nowMs + sparkDurationMs,
            lastQuotaMetTimestamp = nowMs,
            dayStartTimestamp = nowMs,
            unlockedCorkboardSceneSlots = initialSceneSlots
        )
    }

    fun getBibleDurationMs(format: ProjectFormat, screenplayType: ScreenplayType): Long {
        val isTv = format == ProjectFormat.TV_SERIES ||
            screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
            screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY
        return when {
            isTv -> 72L * 3600L * 1000L
            format == ProjectFormat.NOVEL -> 96L * 3600L * 1000L
            else -> 48L * 3600L * 1000L
        }
    }

    fun getOutlineDurationMs(format: ProjectFormat): Long {
        return if (format == ProjectFormat.NOVEL) {
            48L * 3600L * 1000L
        } else {
            24L * 3600L * 1000L
        }
    }

    fun formatDurationHhMmSs(ms: Long): String {
        val clamped = ms.coerceAtLeast(0L)
        val totalSeconds = clamped / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatTimestamp(ms: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
        return sdf.format(Date(ms))
    }

    /**
     * Permanently locks the current stage (`isReadOnly = true`) and transitions one-way to the next stage,
     * generating a Stage-Gate High-Five Receipt at the exact millisecond of lock.
     */
    fun advanceStage(
        state: FirstDraftState,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        if (state.stage == FirstDraftStage.STUDIO_UNLOCKED) return state

        val remainingMs = when (state.stage) {
            FirstDraftStage.SPARK -> state.sparkActiveRemainingMs
            FirstDraftStage.BIBLE, FirstDraftStage.OUTLINE -> (state.stageDeadlineWallClockMs - nowMs).coerceAtLeast(0L)
            else -> 0L
        }
        val remFormatted = formatDurationHhMmSs(remainingMs)

        return when (state.stage) {
            FirstDraftStage.SPARK -> {
                val bibleDur = getBibleDurationMs(state.projectFormat, state.screenplayType)
                val nextDeadline = nowMs + bibleDur
                state.copy(
                    stage = FirstDraftStage.BIBLE,
                    isSparkReadOnly = true,
                    stageStartWallClockMs = nowMs,
                    stageDeadlineWallClockMs = nextDeadline,
                    pendingStageGateReceipt = StageGateReceiptData(
                        stageSealed = FirstDraftStage.SPARK,
                        timeRemainingFormatted = remFormatted,
                        nextStageDeadlineFormatted = formatTimestamp(nextDeadline),
                        sealedAtTimestamp = nowMs
                    )
                )
            }
            FirstDraftStage.BIBLE -> {
                val outlineDur = getOutlineDurationMs(state.projectFormat)
                val nextDeadline = nowMs + outlineDur
                state.copy(
                    stage = FirstDraftStage.OUTLINE,
                    isSparkReadOnly = true,
                    isBibleReadOnly = true,
                    stageStartWallClockMs = nowMs,
                    stageDeadlineWallClockMs = nextDeadline,
                    pendingStageGateReceipt = StageGateReceiptData(
                        stageSealed = FirstDraftStage.BIBLE,
                        timeRemainingFormatted = remFormatted,
                        nextStageDeadlineFormatted = formatTimestamp(nextDeadline),
                        sealedAtTimestamp = nowMs
                    )
                )
            }
            FirstDraftStage.OUTLINE -> {
                val nextQuotaDeadline = nowMs + 48L * 3600L * 1000L
                state.copy(
                    stage = FirstDraftStage.DRAFT,
                    isSparkReadOnly = true,
                    isBibleReadOnly = true,
                    isOutlineReadOnly = true,
                    stageStartWallClockMs = nowMs,
                    stageDeadlineWallClockMs = nextQuotaDeadline,
                    lastQuotaMetTimestamp = nowMs,
                    dayStartTimestamp = nowMs,
                    pendingStageGateReceipt = StageGateReceiptData(
                        stageSealed = FirstDraftStage.OUTLINE,
                        timeRemainingFormatted = remFormatted,
                        nextStageDeadlineFormatted = formatTimestamp(nextQuotaDeadline),
                        sealedAtTimestamp = nowMs
                    )
                )
            }
            FirstDraftStage.DRAFT -> {
                state.copy(
                    stage = FirstDraftStage.STUDIO_UNLOCKED,
                    isSparkReadOnly = false,
                    isBibleReadOnly = false,
                    isOutlineReadOnly = false,
                    isDecayed = false
                )
            }
            FirstDraftStage.STUDIO_UNLOCKED -> state
        }
    }

    /**
     * 1-second heartbeat tick driving active session timers, wall-clock airlocks,
     * WorkShiftFreeze pauses, 48-hour Decay evaluation, and medium-specific Loot Drops.
     */
    fun onSecondTick(
        state: FirstDraftState,
        isWindowFocused: Boolean,
        currentWordCount: Int,
        currentSceneCount: Int,
        hasFadeOut: Boolean,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        if (!state.isFirstDraftMode || state.stage == FirstDraftStage.STUDIO_UNLOCKED) {
            return state
        }

        // 1. Check Day Job Duty Roster (WorkShiftFreeze)
        val activeShift = state.workShiftFreeze.getActiveShift(nowMs)
        if (activeShift != null) {
            // Pause all wall-clock macro-timers (BIBLE, OUTLINE, 48-Hour Decay) by shifting deadlines +1000ms
            return state.copy(
                stageDeadlineWallClockMs = state.stageDeadlineWallClockMs + 1000L,
                lastQuotaMetTimestamp = state.lastQuotaMetTimestamp + 1000L
            )
        }

        var updated = state

        // 2. Decrement temporary XP Shop pass timers during active session
        if (isWindowFocused) {
            if (updated.airlockPassRemainingMs > 0L) {
                updated = updated.copy(
                    airlockPassRemainingMs = (updated.airlockPassRemainingMs - 1000L).coerceAtLeast(0L)
                )
            }
            if (updated.mulliganPassRemainingMs > 0L) {
                updated = updated.copy(
                    mulliganPassRemainingMs = (updated.mulliganPassRemainingMs - 1000L).coerceAtLeast(0L)
                )
            }
        }

        // 3. Stage-specific timer enforcement
        when (updated.stage) {
            FirstDraftStage.SPARK -> {
                if (isWindowFocused) {
                    val nextRem = (updated.sparkActiveRemainingMs - 1000L).coerceAtLeast(0L)
                    updated = updated.copy(sparkActiveRemainingMs = nextRem)
                    if (nextRem <= 0L) {
                        return advanceStage(updated, nowMs)
                    }
                }
            }
            FirstDraftStage.BIBLE, FirstDraftStage.OUTLINE -> {
                if (nowMs >= updated.stageDeadlineWallClockMs) {
                    return advanceStage(updated, nowMs)
                }
            }
            FirstDraftStage.DRAFT -> {
                // Handle Penalty Sprint countdown if Decayed
                if (updated.isDecayed) {
                    if (isWindowFocused) {
                        val remPenalty = (updated.penaltySprintRemainingMs - 1000L).coerceAtLeast(0L)
                        updated = if (remPenalty <= 0L) {
                            // 25 minutes elapsed without completing 500 words -> reset counter to 0
                            updated.copy(
                                penaltyScratchpadText = "",
                                penaltySprintRemainingMs = 25L * 60L * 1000L
                            )
                        } else {
                            updated.copy(penaltySprintRemainingMs = remPenalty)
                        }
                    }
                    return updated
                }

                // Handle 25-minute XP Sprint
                if (updated.isSprintRunning && isWindowFocused) {
                    val remSprint = (updated.activeSprintRemainingMs - 1000L).coerceAtLeast(0L)
                    updated = if (remSprint <= 0L) {
                        updated.copy(
                            xpBalance = updated.xpBalance + 1000,
                            activeSprintRemainingMs = 25L * 60L * 1000L,
                            isSprintRunning = false,
                            lastLootToastMessage = "🏆 25-Minute Sprint Complete! +1,000 XP Awarded!"
                        )
                    } else {
                        updated.copy(activeSprintRemainingMs = remSprint)
                    }
                }

                // Roll 24-hour day window if >= 24h elapsed
                if (nowMs - updated.dayStartTimestamp >= 86_400_000L) {
                    updated = updated.copy(
                        dayStartTimestamp = nowMs,
                        wordsAtDayStart = currentWordCount,
                        activeStreakDays = updated.activeStreakDays + 1
                    )
                }

                // Check Survival Quota
                val netWordsSinceQuota = (currentWordCount - updated.wordsAtQuotaWindowStart).coerceAtLeast(0)
                val quotaRequired = updated.survivalQuotaWords()
                if (netWordsSinceQuota >= quotaRequired) {
                    updated = updated.copy(
                        lastQuotaMetTimestamp = nowMs,
                        wordsAtQuotaWindowStart = currentWordCount,
                        dailyQuotaMet = true
                    )
                }

                // Check 48-Hour Decay Clock (unless Freeze Token is active)
                if (nowMs >= updated.freezeTokenUntilWallClockMs) {
                    val elapsedSinceQuota = nowMs - updated.lastQuotaMetTimestamp
                    if (elapsedSinceQuota >= 48L * 3600L * 1000L) {
                        updated = updated.copy(
                            isDecayed = true,
                            pendingDecayReceipt = true,
                            penaltyScratchpadText = "",
                            penaltySprintRemainingMs = 25L * 60L * 1000L
                        )
                        return updated
                    }
                }

                // Idle Instigator (60s of zero keystroke delta with active window focus)
                if (isWindowFocused) {
                    updated = updated.copy(idleSecondsInDraft = updated.idleSecondsInDraft + 1)
                }

                // Evaluate Medium-Specific Loot Drops & STUDIO_UNLOCKED transitions
                updated = evaluateLootDropsAndProgression(
                    state = updated,
                    currentWordCount = currentWordCount,
                    currentSceneCount = currentSceneCount,
                    hasFadeOut = hasFadeOut,
                    nowMs = nowMs
                )
            }
            FirstDraftStage.STUDIO_UNLOCKED -> {}
        }

        return updated
    }

    /**
     * Evaluates Medium-Specific Rulesets, Rolling Fog of War, Act-Gates, and Loot Drops.
     */
    fun evaluateLootDropsAndProgression(
        state: FirstDraftState,
        currentWordCount: Int,
        currentSceneCount: Int,
        hasFadeOut: Boolean,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        if (state.stage != FirstDraftStage.DRAFT) return state
        var s = state

        val isTv = s.projectFormat == ProjectFormat.TV_SERIES ||
            s.screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
            s.screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY

        when {
            isTv -> {
                // TV Series & Screenplay Universe Loot Drops
                val isHalfHour = s.screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY
                val teaserWords = if (isHalfHour) 750 else 1500
                val actTwoWords = if (isHalfHour) 2750 else 5000
                val pilotTargetWords = if (isHalfHour) 5500 else 10000

                // Act-Gate Progression
                val actWordStep = if (isHalfHour) 1350 else 2000
                val calculatedAct = (currentWordCount / actWordStep).coerceAtLeast(0)
                if (calculatedAct > s.tvUnlockedActIndex) {
                    s = s.copy(
                        tvUnlockedActIndex = calculatedAct,
                        unlockedCorkboardSceneSlots = maxOf(s.unlockedCorkboardSceneSlots, (calculatedAct + 1) * 4),
                        lastLootToastMessage = "🔒 Act ${calculatedAct} Sealed! Act ${calculatedAct + 1} Corkboard Slots Unlocked!"
                    )
                }

                if (!s.isTtsLootUnlocked && currentWordCount >= teaserWords) {
                    s = s.copy(
                        isTtsLootUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP: End of Teaser/Cold Open! Multi-Voice Table Read (TTS) Unlocked!"
                    )
                }
                if (!s.isExtraDossiersAndPortraitsUnlocked && currentWordCount >= actTwoWords) {
                    s = s.copy(
                        isExtraDossiersAndPortraitsUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP: End of Act II! AI Character Portraits Unlocked!"
                    )
                }
                if (currentWordCount >= pilotTargetWords) {
                    s = s.copy(
                        stage = FirstDraftStage.STUDIO_UNLOCKED,
                        tvEpisode101Unlocked = true,
                        isSparkReadOnly = false,
                        isBibleReadOnly = false,
                        isOutlineReadOnly = false,
                        loreExpansionPassUntilWallClockMs = nowMs + 30L * 60L * 1000L,
                        lastLootToastMessage = "🏆 PILOT COMPLETED! Episode 101 STUDIO UNLOCKED + Episode 102 Outline & 30m Series Bible Pass Granted!"
                    )
                }
            }

            s.projectFormat == ProjectFormat.NOVEL -> {
                // Prose Novel & Book Manuscript Loot Drops (Target: 60,000 Words)
                // Sequential Chapter Airlock (3,500 words per chapter)
                val unlockedCh = (currentWordCount / 3500).coerceIn(0, 11)
                if (unlockedCh > s.novelUnlockedChapterIndex) {
                    s = s.copy(
                        novelUnlockedChapterIndex = unlockedCh,
                        unlockedCorkboardSceneSlots = (unlockedCh + 1) * 3,
                        lastLootToastMessage = "🔒 Chapter $unlockedCh Airlocked! Chapter ${unlockedCh + 1} & Scene Cards Unlocked!"
                    )
                }

                if (!s.isNovelCoverPassAvailable && currentWordCount >= 10000) {
                    s = s.copy(
                        isNovelCoverPassAvailable = true,
                        lastLootToastMessage = "🎁 LOOT DROP (10,000 Words): Novel Cover Generator (1 Pass) Unlocked!"
                    )
                }
                if (s.loreExpansionPassUntilWallClockMs == 0L && currentWordCount >= 25000) {
                    s = s.copy(
                        loreExpansionPassUntilWallClockMs = nowMs + 24L * 3600L * 1000L,
                        lastLootToastMessage = "🎁 LOOT DROP (25,000 Words): 24-Hour 'Lore Expansion Pass' on Story Bible Unlocked!"
                    )
                }
                if (!s.isNovelPlotTwistAiUnlocked && currentWordCount >= 40000) {
                    s = s.copy(
                        isNovelPlotTwistAiUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP (40,000 Words): AI Co-Writer Plot-Twist Brainstormer Unlocked!"
                    )
                }
                if (currentWordCount >= 60000) {
                    s = s.copy(
                        stage = FirstDraftStage.STUDIO_UNLOCKED,
                        isSparkReadOnly = false,
                        isBibleReadOnly = false,
                        isOutlineReadOnly = false,
                        lastLootToastMessage = "🏆 60,000 WORDS REACHED! STUDIO UNLOCKED — All Chapters, Shunn & EPUB 3 Compilers Enabled!"
                    )
                }
            }

            else -> {
                // Feature Screenplay (Target: 90 Pages / ~18,000 Words)
                // Rolling Fog of War: Completing script text for Scenes N to N+2 unlocks Corkboard Scene Card slots N+5 to N+7 (max 5 scenes ahead)
                val desiredSlots = (currentSceneCount + 5).coerceAtLeast(5)
                if (desiredSlots > s.unlockedCorkboardSceneSlots) {
                    s = s.copy(unlockedCorkboardSceneSlots = desiredSlots)
                }

                if (!s.isTtsLootUnlocked && currentWordCount >= 3000) {
                    s = s.copy(
                        isTtsLootUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP (3,000 Words / Page 15): Multi-Voice Table Read (TTS) Unlocked!"
                    )
                }
                if (!s.isExtraDossiersAndPortraitsUnlocked && currentWordCount >= 5000) {
                    s = s.copy(
                        isExtraDossiersAndPortraitsUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP (5,000 Words / Page 25): +2 Character Dossier Slots & AI Character Portraits Unlocked!"
                    )
                }
                if (!s.isSceneDoctorLootUnlocked && currentWordCount >= 12000) {
                    s = s.copy(
                        isSceneDoctorLootUnlocked = true,
                        lastLootToastMessage = "🎁 LOOT DROP (12,000 Words / Page 60): AI Scene Doctor (Forward-Escalation) Unlocked!"
                    )
                }
                if (currentWordCount >= 17000 && hasFadeOut) {
                    s = s.copy(
                        stage = FirstDraftStage.STUDIO_UNLOCKED,
                        isSparkReadOnly = false,
                        isBibleReadOnly = false,
                        isOutlineReadOnly = false,
                        lastLootToastMessage = "🏆 FADE OUT AT 17,000+ WORDS! STUDIO UNLOCKED — Full Seshat v2.0.0 Unlocked!"
                    )
                }
            }
        }

        return s
    }

    /**
     * Records keystroke activity in DRAFT mode, resets the 60s Idle Instigator timer,
     * and tracks backspaces for the XP engine and Shame Ratio.
     */
    fun onKeystrokeRecorded(
        state: FirstDraftState,
        isBackspace: Boolean,
        totalBlockCount: Int,
        totalSceneCount: Int
    ): FirstDraftState {
        if (!state.isFirstDraftMode) return state

        var updated = state.copy(
            idleSecondsInDraft = 0,
            totalKeystrokes = state.totalKeystrokes + 1,
            totalBackspaces = if (isBackspace) state.totalBackspaces + 1 else state.totalBackspaces,
            currentSceneBackspaceCount = if (isBackspace) state.currentSceneBackspaceCount + 1 else state.currentSceneBackspaceCount
        )

        // XP Currency Engine: Award XP strictly in DRAFT state
        if (updated.stage == FirstDraftStage.DRAFT) {
            // +10 XP per net-new line/block of Action/Dialogue/Prose
            if (totalBlockCount > updated.highestBlockCountRewarded) {
                val netNewLines = totalBlockCount - updated.highestBlockCountRewarded
                updated = updated.copy(
                    xpBalance = updated.xpBalance + (netNewLines * 10),
                    highestBlockCountRewarded = totalBlockCount
                )
            }
            // +500 XP per completed scene with < 5 backspaces
            if (totalSceneCount > updated.highestSceneCountRewarded) {
                val bonusXp = if (updated.currentSceneBackspaceCount < 5) 500 else 0
                updated = updated.copy(
                    xpBalance = updated.xpBalance + bonusXp,
                    highestSceneCountRewarded = totalSceneCount,
                    currentSceneBackspaceCount = 0,
                    lastLootToastMessage = if (bonusXp > 0) "⚡ Flawless Scene (<5 Backspaces)! +500 XP!" else updated.lastLootToastMessage
                )
            }
        }

        return updated
    }

    /**
     * Purchases an item from the XP Shop.
     */
    fun purchaseXpItem(
        state: FirstDraftState,
        item: XpShopItem,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        if (state.xpBalance < item.costXp) return state
        val newBalance = state.xpBalance - item.costXp
        return when (item) {
            XpShopItem.AIRLOCK_PASS -> state.copy(
                xpBalance = newBalance,
                airlockPassRemainingMs = state.airlockPassRemainingMs + item.durationMs,
                lastLootToastMessage = "🔓 Airlock Pass Activated! BIBLE unlocked for 15 minutes of active session time."
            )
            XpShopItem.MULLIGAN -> state.copy(
                xpBalance = newBalance,
                mulliganPassRemainingMs = state.mulliganPassRemainingMs + item.durationMs,
                lastLootToastMessage = "⏪ Mulligan Activated! Preceding scenes/chapters unlocked for 20 minutes."
            )
            XpShopItem.FREEZE_TOKEN -> {
                val baseTime = maxOf(nowMs, state.freezeTokenUntilWallClockMs)
                state.copy(
                    xpBalance = newBalance,
                    freezeTokenUntilWallClockMs = baseTime + item.durationMs,
                    isDecayed = false,
                    lastLootToastMessage = "❄️ Freeze Token Activated! 48-Hour Decay Clock paused for 48 hours."
                )
            }
        }
    }

    /**
     * Handles window focus loss / app minimization:
     * Penalizes active 25m sprints and resets the 500-word Penalty Sprint counter to 0.
     */
    fun onWindowFocusLost(state: FirstDraftState): FirstDraftState {
        var updated = state
        if (updated.isSprintRunning) {
            updated = updated.copy(
                isSprintRunning = false,
                activeSprintRemainingMs = 25L * 60L * 1000L
            )
        }
        if (updated.isDecayed && updated.penaltyScratchpadText.isNotEmpty()) {
            updated = updated.copy(
                penaltyScratchpadText = "",
                penaltySprintRemainingMs = 25L * 60L * 1000L
            )
        }
        return updated
    }

    /**
     * Updates the Penalty Sprint scratchpad text and unlocks the project once 500 net-new words are typed.
     */
    fun updatePenaltyScratchpad(
        state: FirstDraftState,
        newText: String,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        val wordCount = countWords(newText)
        return if (wordCount >= 500) {
            state.copy(
                isDecayed = false,
                penaltyScratchpadText = "",
                penaltySprintRemainingMs = 25L * 60L * 1000L,
                lastQuotaMetTimestamp = nowMs,
                lastLootToastMessage = "🔥 500-Word Penalty Sprint Completed! Draft Restored from Decay!"
            )
        } else {
            state.copy(penaltyScratchpadText = newText)
        }
    }

    /**
     * Saves and locks the weekly WorkShiftFreeze roster for 7 days, enforcing the 50-hour hard cap.
     */
    fun saveAndLockWorkRoster(
        state: FirstDraftState,
        shifts: List<WorkShiftBlock>,
        nowMs: Long = System.currentTimeMillis()
    ): FirstDraftState {
        if (!state.workShiftFreeze.canEditRoster(nowMs)) return state
        // Enforce 50-hour (3,000 minute) weekly hard cap
        val cappedShifts = mutableListOf<WorkShiftBlock>()
        var accumulatedMinutes = 0
        for (shift in shifts) {
            val dur = shift.durationMinutes()
            if (accumulatedMinutes + dur <= WorkShiftFreeze.MAX_WEEKLY_MINUTES) {
                cappedShifts.add(shift)
                accumulatedMinutes += dur
            }
        }
        val lockedRoster = WorkShiftFreeze(
            shifts = cappedShifts,
            isReadOnly = true,
            lockedUntilTimestamp = nowMs + 7L * 24L * 3600L * 1000L,
            isInitialSetupComplete = true
        )
        return state.copy(
            workShiftFreeze = lockedRoster,
            lastLootToastMessage = "🗓️ Duty Roster Locked for 7 Days (${String.format(Locale.US, "%.1f", accumulatedMinutes / 60f)} / 50.0 hrs)"
        )
    }

    fun countWords(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(Regex("\\s+")).count { it.isNotBlank() }
    }

    fun trimToWordLimit(text: String, maxWords: Int): String {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= maxWords) return text
        return words.take(maxWords).joinToString(" ")
    }

    fun countSentences(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(Regex("[.!?]+")).count { it.trim().isNotEmpty() }
    }

    fun trimToSentenceLimit(text: String, maxSentences: Int): String {
        val regex = Regex("[^.!?]+[.!?]*")
        val matches = regex.findAll(text).map { it.value }.toList()
        if (matches.size <= maxSentences) return text
        return matches.take(maxSentences).joinToString("").trimEnd()
    }

    // Local Persistence Helpers
    private fun sanitizeTitle(title: String): String {
        val clean = title.trim().lowercase(Locale.US).replace(Regex("[^a-z0-9._-]"), "_")
        return if (clean.isBlank()) "untitled_project" else clean
    }

    fun saveState(context: Context, projectTitle: String, state: FirstDraftState) {
        try {
            val dir = File(context.filesDir, FIRST_DRAFT_DIR)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${sanitizeTitle(projectTitle)}_first_draft.json")
            file.writeText(state.toJson().toString(2))
        } catch (_: Exception) {}
    }

    fun loadState(
        context: Context,
        projectTitle: String,
        format: ProjectFormat,
        screenplayType: ScreenplayType
    ): FirstDraftState {
        return try {
            val dir = File(context.filesDir, FIRST_DRAFT_DIR)
            val file = File(dir, "${sanitizeTitle(projectTitle)}_first_draft.json")
            if (file.exists() && file.canRead()) {
                FirstDraftState.fromJson(JSONObject(file.readText()))
            } else {
                createForNewProject(format, screenplayType)
            }
        } catch (_: Exception) {
            createForNewProject(format, screenplayType)
        }
    }
}

object FirstDraftTextMetrics {
    fun countWords(text: String): Int = FirstDraftController.countWords(text)
    fun clipToWordLimit(text: String, maxWords: Int): String = FirstDraftController.trimToWordLimit(text, maxWords)
    fun countSentences(text: String): Int = FirstDraftController.countSentences(text)
    fun clipToSentenceLimit(text: String, maxSentences: Int): String = FirstDraftController.trimToSentenceLimit(text, maxSentences)
}
