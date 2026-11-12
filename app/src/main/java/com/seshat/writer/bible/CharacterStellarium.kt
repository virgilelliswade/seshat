package com.seshat.writer.bible

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * The 8 Archetype Constellation Domains for the Character Stellarium.
 */
enum class StellariumDomain(
    val label: String,
    val shortLabel: String,
    val icon: String,
    val hexColor: Long
) {
    CREATIVITY_WIT("Creativity & Wit", "Wit", "🎭", 0xFFF97316),
    WISDOM_TRUTH("Wisdom & Truth", "Truth", "🧭", 0xFFF59E0B),
    LOVE_CONNECTION("Love & Connection", "Heart", "❤️", 0xFFEC4899),
    AUTHORITY_STRUCTURE("Authority & Structure", "Crown", "👑", 0xFF3B82F6),
    COURAGE_JUSTICE("Courage & Justice", "Valor", "⚔️", 0xFFEF4444),
    PASSION_DRIVE("Passion & Drive", "Drive", "🔥", 0xFFA855F7),
    SHADOW_FRINGE("Shadow & Fringe", "Shadow", "🦇", 0xFF6366F1),
    GROUNDING_SURVIVAL("Grounding & Survival", "Shield", "🛡️", 0xFF10B981)
}

/**
 * Each archetype star carries both a Light (positive) and Shadow (negative) expression,
 * or Dual tension when both pull on the character at once.
 */
enum class StarPolarity(val label: String, val badge: String) {
    POSITIVE("Light", "✦"),
    DUAL("Dual (Conflict)", "⚖️"),
    SHADOW("Shadow", "🌑")
}

data class ArchetypeStar(
    val id: String,
    val name: String,
    val domain: StellariumDomain,
    val positiveTrait: String,
    val shadowTrait: String
)

data class StellariumStarSelection(
    val id: String = UUID.randomUUID().toString(),
    val archetypeId: String = "",
    val name: String = "",
    val domain: StellariumDomain = StellariumDomain.WISDOM_TRUTH,
    val polarity: StarPolarity = StarPolarity.DUAL,
    val note: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("archetypeId", archetypeId)
        json.put("name", name)
        json.put("domain", domain.name)
        json.put("polarity", polarity.name)
        json.put("note", note)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): StellariumStarSelection {
            val domain = try {
                StellariumDomain.valueOf(json.optString("domain", StellariumDomain.WISDOM_TRUTH.name))
            } catch (_: Exception) {
                StellariumDomain.WISDOM_TRUTH
            }
            val polarity = try {
                StarPolarity.valueOf(json.optString("polarity", StarPolarity.DUAL.name))
            } catch (_: Exception) {
                StarPolarity.DUAL
            }
            return StellariumStarSelection(
                id = json.optString("id", UUID.randomUUID().toString()),
                archetypeId = json.optString("archetypeId", ""),
                name = json.optString("name", ""),
                domain = domain,
                polarity = polarity,
                note = json.optString("note", "")
            )
        }

        fun fromCatalogStar(
            star: ArchetypeStar,
            polarity: StarPolarity = StarPolarity.DUAL
        ): StellariumStarSelection = StellariumStarSelection(
            archetypeId = star.id,
            name = star.name,
            domain = star.domain,
            polarity = polarity
        )
    }
}

object StellariumCatalog {
    val stars: List<ArchetypeStar> = listOf(
        // 1. Creativity & Wit
        ArchetypeStar("comedian", "Comedian", StellariumDomain.CREATIVITY_WIT, "Disarms tension with sharp humor and truth.", "Uses jokes to deflect real intimacy or pain."),
        ArchetypeStar("trickster", "Trickster", StellariumDomain.CREATIVITY_WIT, "Outsmarts rigid systems through clever lateral moves.", "Sows chaos and betrays trust just to prove superiority."),
        ArchetypeStar("artisan", "Artisan", StellariumDomain.CREATIVITY_WIT, "Crafts beauty and precision under impossible pressure.", "Obsesses over perfection until life and relationships stall."),
        ArchetypeStar("visionary", "Visionary", StellariumDomain.CREATIVITY_WIT, "Sees futures no one else dares to imagine.", "Detaches from reality and sacrifices present people for an ideal."),
        ArchetypeStar("provocateur", "Provocateur", StellariumDomain.CREATIVITY_WIT, "Exposes hypocrisy by asking forbidden questions.", "Mistakes cruelty or shock value for profundity."),
        ArchetypeStar("storyteller", "Storyteller", StellariumDomain.CREATIVITY_WIT, "Weaves meaning that unites fractured groups.", "Rewrites history and spins comforting lies to avoid guilt."),

        // 2. Wisdom & Truth
        ArchetypeStar("seeker", "Seeker", StellariumDomain.WISDOM_TRUTH, "Relentlessly pursues hidden truth regardless of cost.", "Never satisfied; abandons people the moment a mystery ends."),
        ArchetypeStar("sage", "Sage", StellariumDomain.WISDOM_TRUTH, "Offers calm perspective when everyone else panics.", "Hides behind detached philosophy instead of taking a side."),
        ArchetypeStar("investigator", "Investigator", StellariumDomain.WISDOM_TRUTH, "Notices subtle clues and patterns others miss.", "Treats every relationship like an interrogation."),
        ArchetypeStar("oracle", "Oracle", StellariumDomain.WISDOM_TRUTH, "Senses coming consequences before they strike.", "Becomes fatalistic or speaks in cryptic riddles."),
        ArchetypeStar("mentor", "Mentor", StellariumDomain.WISDOM_TRUTH, "Lifts others up with hard-earned lessons.", "Projects past regrets onto protégés or refuses to let go."),
        ArchetypeStar("witness", "Witness", StellariumDomain.WISDOM_TRUTH, "Remembers what happened when power tries to erase it.", "Watches tragedy unfold without intervening."),

        // 3. Love & Connection
        ArchetypeStar("caregiver", "Caregiver", StellariumDomain.LOVE_CONNECTION, "Protects and nurtures the vulnerable selflessly.", "Smothers others or enables weakness to feel needed."),
        ArchetypeStar("lover", "Lover", StellariumDomain.LOVE_CONNECTION, "Commits wholly with fierce devotion and passion.", "Loses all boundaries and self-worth in pursuit of affection."),
        ArchetypeStar("peacemaker", "Peacemaker", StellariumDomain.LOVE_CONNECTION, "Bridges bitter enemies with genuine empathy.", "Compromises core truth just to keep surface harmony."),
        ArchetypeStar("devotee", "Devotee", StellariumDomain.LOVE_CONNECTION, "Stands loyal through exile, failure, and ruin.", "Follows a toxic person or cause off a cliff."),
        ArchetypeStar("host", "Host", StellariumDomain.LOVE_CONNECTION, "Creates sanctuary where strangers become family.", "Keeps score of every favor and demands silent obedience."),
        ArchetypeStar("empath", "Empath", StellariumDomain.LOVE_CONNECTION, "Reads unspoken wounds and heals isolation.", "Absorbs others' pain until paralyzed or resentful."),

        // 4. Authority & Structure
        ArchetypeStar("judge", "Judge", StellariumDomain.AUTHORITY_STRUCTURE, "Weighs every side fairly and upholds principles.", "Becomes cold, rigid, and merciless in verdicts."),
        ArchetypeStar("sovereign", "Sovereign", StellariumDomain.AUTHORITY_STRUCTURE, "Accepts heavy responsibility for the whole.", "Confuses control with safety; rules through fear."),
        ArchetypeStar("architect", "Architect", StellariumDomain.AUTHORITY_STRUCTURE, "Builds enduring plans that survive chaos.", "Treats human beings like replaceable blueprints."),
        ArchetypeStar("enforcer", "Enforcer", StellariumDomain.AUTHORITY_STRUCTURE, "Gets impossible jobs done with iron discipline.", "Uses brute authority to silence dissent."),
        ArchetypeStar("steward", "Steward", StellariumDomain.AUTHORITY_STRUCTURE, "Safeguards institutions and traditions for the next generation.", "Defends rotten traditions long after they harm the living."),
        ArchetypeStar("commander", "Commander", StellariumDomain.AUTHORITY_STRUCTURE, "Decisive under fire; inspires ranks to hold the line.", "Cannot switch off war-mode in peacetime."),

        // 5. Courage & Justice
        ArchetypeStar("rebel", "Rebel", StellariumDomain.COURAGE_JUSTICE, "Challenges corrupt authority and defends the powerless.", "Always defiant; breaks rules even when it hurts allies."),
        ArchetypeStar("protector", "Protector", StellariumDomain.COURAGE_JUSTICE, "Steps in front of danger without hesitation.", "Uses violence or overprotection to justify controlling others."),
        ArchetypeStar("martyr", "Martyr", StellariumDomain.COURAGE_JUSTICE, "Willing to pay the ultimate price for a righteous cause.", "Weaponizes suffering and guilt to manipulate others."),
        ArchetypeStar("whistleblower", "Whistleblower", StellariumDomain.COURAGE_JUSTICE, "Speaks dangerous truth inside a corrupt room.", "Burns every bridge and thrives on self-righteous isolation."),
        ArchetypeStar("champion", "Champion", StellariumDomain.COURAGE_JUSTICE, "Rises to face insurmountable odds head-on.", "Addicted to crisis; picks fights when life gets quiet."),
        ArchetypeStar("vanguard", "Vanguard", StellariumDomain.COURAGE_JUSTICE, "Walks first into unknown territory so others can follow.", "Outruns support and leaves the vulnerable behind."),

        // 6. Passion & Drive
        ArchetypeStar("maverick", "Maverick", StellariumDomain.PASSION_DRIVE, "Breaks stale orthodoxy to invent bold breakthroughs.", "Refuses teamwork and sabotages collective plans."),
        ArchetypeStar("conqueror", "Conqueror", StellariumDomain.PASSION_DRIVE, "Refuses defeat; bends obstacles to sheer willpower.", "Never knows when enough is enough."),
        ArchetypeStar("obsessive", "Obsessive", StellariumDomain.PASSION_DRIVE, "Focuses total energy until a breakthrough happens.", "Consumes self and loved ones in tunnel vision."),
        ArchetypeStar("gambler", "Gambler", StellariumDomain.PASSION_DRIVE, "Bets big when timid minds freeze.", "Addicted to stakes; risks everything for the rush."),
        ArchetypeStar("trailblazer", "Trailblazer", StellariumDomain.PASSION_DRIVE, "Hungry for frontier, reinvention, and growth.", "Abandons commitments the moment they feel settled."),
        ArchetypeStar("zealot", "Zealot", StellariumDomain.PASSION_DRIVE, "Unshakable conviction that moves mountains.", "Blind to collateral damage done in the name of belief."),

        // 7. Shadow & Fringe
        ArchetypeStar("vampire", "Vampire", StellariumDomain.SHADOW_FRINGE, "Magnetic, patient, and acutely aware of hidden desires.", "Parasitic; drains allies' energy and loyalty to survive."),
        ArchetypeStar("outcast", "Outcast", StellariumDomain.SHADOW_FRINGE, "Sees society clearly from outside its illusions.", "Pushes away belonging before others can reject them."),
        ArchetypeStar("saboteur", "Saboteur", StellariumDomain.SHADOW_FRINGE, "Spots fatal flaws in enemy machinery instantly.", "Quietly undermines own happiness when success gets close."),
        ArchetypeStar("opportunist", "Opportunist", StellariumDomain.SHADOW_FRINGE, "Adapts instantly and turns disaster into leverage.", "Has no compass beyond self-interest."),
        ArchetypeStar("phantom", "Phantom", StellariumDomain.SHADOW_FRINGE, "Moves unseen, leaves no trace, keeps secrets safe.", "Disappears emotionally whenever real vulnerability is asked."),
        ArchetypeStar("con_artist", "Con Artist", StellariumDomain.SHADOW_FRINGE, "Reads people effortlessly and gives them what they crave.", "Forgets who they truly are beneath layered masks."),

        // 8. Grounding & Survival
        ArchetypeStar("skeptic", "Skeptic", StellariumDomain.GROUNDING_SURVIVAL, "Cuts through hype and sees traps before stepping in.", "Cynical; refuses hope even when real grace arrives."),
        ArchetypeStar("survivor", "Survivor", StellariumDomain.GROUNDING_SURVIVAL, "Endures anything and improvises when resources vanish.", "Still fights yesterday's war long after the danger passed."),
        ArchetypeStar("coward", "Coward", StellariumDomain.GROUNDING_SURVIVAL, "Respects real danger and stays alive to fight smarter.", "Freezes or flees when someone they love needs a stand."),
        ArchetypeStar("stoic", "Stoic", StellariumDomain.GROUNDING_SURVIVAL, "Unshakeable composure through grief and chaos.", "Bottles emotion until it erupts or turns to numbness."),
        ArchetypeStar("pragmatist", "Pragmatist", StellariumDomain.GROUNDING_SURVIVAL, "Keeps boots on the ground and solves what's in front of them.", "Dismisses dreams and ideals as childish luxuries."),
        ArchetypeStar("cynic", "Cynic", StellariumDomain.GROUNDING_SURVIVAL, "Never fooled by propaganda or false charm.", "Poisons collective morale before effort can begin.")
    )

    fun findById(id: String): ArchetypeStar? = stars.firstOrNull { it.id == id }

    fun drawContrastingStars(
        count: Int = 3,
        excludingIds: Set<String> = emptySet()
    ): List<StellariumStarSelection> {
        val available = stars.filter { it.id !in excludingIds }.shuffled()
        val picked = mutableListOf<ArchetypeStar>()
        val usedDomains = mutableSetOf<StellariumDomain>()
        for (star in available) {
            if (star.domain !in usedDomains) {
                picked.add(star)
                usedDomains.add(star.domain)
                if (picked.size >= count) break
            }
        }
        if (picked.size < count) {
            for (star in available) {
                if (star !in picked) {
                    picked.add(star)
                    if (picked.size >= count) break
                }
            }
        }
        val polarities = listOf(StarPolarity.POSITIVE, StarPolarity.DUAL, StarPolarity.SHADOW)
        return picked.mapIndexed { idx, star ->
            StellariumStarSelection.fromCatalogStar(star, polarities[idx % polarities.size])
        }
    }

    fun buildConstellationSummary(selections: List<StellariumStarSelection>): String {
        if (selections.isEmpty()) return ""
        return selections.joinToString(" + ") { sel ->
            "${sel.polarity.badge} ${sel.name} (${sel.domain.shortLabel})"
        }
    }
}
