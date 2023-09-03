package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.audio.getCustomLevelDataFile
import com.serwylo.beatgame.levels.achievements.Achievement
import java.io.File

sealed interface Level {
    fun getId(): String
    fun getMp3File(): FileHandle
    fun getLevelDataFile(): FileHandle
    fun getLabel(strings: I18NBundle): String
    fun getUnlockRequirements(): UnlockRequirements
    fun getWorld(): World
    fun getAttribution(): Attribution?
}

interface World {
    fun getId(): String
    fun getLabel(strings: I18NBundle): String
    fun getLevels(): List<Level>
    fun getAttribution(): List<Attribution>
}

data class Attribution(
    val name: String,
    val license: String,
    val sourceUrl: String,
    val author: String?,
)

class BuiltInLevel(
    private val world: World,
    private val mp3Name: String,
    private val labelId: String,
    private val toUnlock: UnlockRequirements,
    private val attribution: Attribution? = null,
): Level {

    override  fun getId() = mp3Name
    override fun getMp3File() = Gdx.files.internal("songs${File.separator}mp3${File.separator}${mp3Name}")

    override fun getLevelDataFile(): FileHandle {
        val name = File(mp3Name).nameWithoutExtension
        return Gdx.files.internal("songs${File.separator}data${File.separator}${name}.json")
    }

    override fun getLabel(strings: I18NBundle) = strings[labelId]
    override fun getUnlockRequirements() = toUnlock
    override fun getWorld() = world
    override fun getAttribution() = attribution
}

/**
 * From the original implementation where only a single custom mp3 could be used, and it
 * appeared in the original world as the last level. Furthermore, it looked in a hard-coded
 * location which was tricky to get add files to.
 *
 * The new implementation ([CustomLevel]) allows multiple custom levels, helps people find
 * song files on their device, and tries to do a better job of loading them in a sensible
 * timeframe.
 */
object LegacyCustomLevel: Level {

    override fun getId() = "custom.mp3"
    override fun getMp3File(): FileHandle = Gdx.files.external("BeatFeet${File.separator}custom.mp3")
    override fun getLevelDataFile(): FileHandle {
        val name = "custom-${getMp3File().lastModified()}"
        return Gdx.files.local(".cache${File.separator}world${File.separator}$name.json")
    }
    override fun getLabel(strings: I18NBundle): String = strings["levels.custom"]
    override fun getUnlockRequirements() = Unlocked()
    override fun getWorld() = TheOriginalWorld
    override fun getAttribution() = null

}

class RemoteWorld(val summary: WorldsDTO.WorldSummaryDTO, private val data: WorldDTO): World {

    private val levels = data.getLevels().map { RemoteLevel(this, it) }

    override fun getId() = summary.id
    override fun getLabel(strings: I18NBundle) = summary.name
    override fun getLevels() = levels

    override fun getAttribution(): List<Attribution> {
        val myAttribution = data.attribution?.let { dto ->
            Attribution(
                name = summary.name,
                license = dto.licenseName,
                author = dto.author,
                sourceUrl = dto.sourceUrl,
            )
        }

        return (levels.map { it.getAttribution() } + myAttribution).filterNotNull().distinct()
    }
}

/**
 * The ID of levels is used to store assets associated with them, such as downloaded files (for
 * levels loaded remotely) or generated files (for custom songs generated locally). As such,
 * be conservative in what characters we accept for safety.
 */
fun sanitiseFilename(id: String) = SAFE_FILENAME_REGEX.findAll(id).toList().joinToString("") { it.value }

class RemoteLevel(private val world: RemoteWorld, private val data: WorldDTO.LevelDTO): Level {

    override fun getId() = data.id
    override fun getLabel(strings: I18NBundle) = data.label
    override fun getWorld() = world
    override fun getMp3File() = getCachedMp3File(this)
    override fun getLevelDataFile() = getCachedLevelDataFile(this)

    suspend fun ensureMp3Downloaded() = downloadAndCacheFile(data.mp3Url, getCachedMp3File(this))
    suspend fun ensureLevelDataDownloaded() = downloadAndCacheFile(data.dataUrl, getLevelDataFile())

    override fun getUnlockRequirements(): UnlockRequirements {
        val isWorldUnlocked = world.summary.unlockRequirements.type == "unlocked"
        val isLevelUnlocked = data.unlockRequirements.type == "unlocked"

        val requiredForWorld = if (isWorldUnlocked) 0 else world.summary.unlockRequirements.numRequired ?: 0
        val requiredForLevel = if (isLevelUnlocked) 0 else data.unlockRequirements.numRequired ?: 0

        return TotalAchievements(requiredForWorld + requiredForLevel)
    }

    override fun getAttribution() = data.attribution?.let { dto ->
        Attribution(
            name = data.label,
            license = dto.licenseName,
            author = dto.author,
            sourceUrl = dto.sourceUrl,
        )
    }

}


class CustomWorld(levelsData: List<CustomWorldDTO.CustomLevelDTO>): World {

    private val levels = levelsData.map {
        CustomLevel(this, it.id, it.label, Gdx.files.absolute(it.mp3Path))
    }

    override fun getId() = "custom"

    override fun getLabel(strings: I18NBundle) = "Your world"

    override fun getLevels() = levels

    override fun getAttribution() = emptyList<Attribution>()

}

class CustomLevel(private val world: CustomWorld, private val id: String, private val label: String, private val mp3File: FileHandle): Level {
    override fun getId() = id

    override fun getMp3File(): FileHandle = mp3File

    override fun getLevelDataFile() = getCustomLevelDataFile(this)

    override fun getLabel(strings: I18NBundle) = label

    override fun getUnlockRequirements() = Unlocked()

    override fun getWorld() = world

    override fun getAttribution() = null

}

object TheOriginalWorld: World {

    override fun getId() = "built-in:the-original-world"
    override fun getLabel(strings: I18NBundle) = strings["worlds.the-original"]
    override fun getAttribution() = listOf(
        Attribution(
            "The Haunted House",
            "CC-BY-SA 3.0",
            "https://ds10forum.bandcamp.com/album/ds10forum-com-the-haunted-mansion",
            "DS10Forum.com",
        ),
        Attribution(
            "Health and Safety",
            "CC-BY-SA 3.0",
            "https://ds10forum.bandcamp.com/album/health-and-safety",
            "DS10Forum.com",
        ),
        Attribution(
            "Awakenings",
            "CC-BY-SA 3.0",
            "https://ds10forum.bandcamp.com/album/awakenings",
            "DS10Forum.com",
        ),
        Attribution(
            "Vivaldi",
            "CC-BY-SA 3.0",
            "https://archive.org/details/The_Four_Seasons_Vivaldi-10361",
            "John Harrison w/ Wichita State University Chamber",
        ),
    )

    override fun getLevels(): List<Level> {
        return listOf(
            TheLaundryRoom,
            TheCourtyard,
            Maintenance,
            ForcingTheGamecard,
            SharplyBentWire,
            EyeTwitching,
            LightFlashes,
            PlayInAWellLitRoom,
            ContactWithMoistureAndDirt,
            TheBallroom,
            OldClock,
            RegulationsForEquipment,
            Convulsions,
            ContactWithDustAndLint,
            TheExerciseRoom,
            Vivaldi,
            ReorientTheReceivingAntenna,
        )
    }

    val TheLaundryRoom = BuiltInLevel(
        this,
        "the_haunted_mansion_the_laundry_room.mp3",
        "levels.the-laundry-room",
        Unlocked()
    )

    val TheCourtyard = BuiltInLevel(
        this,
        "the_haunted_mansion_the_courtyard.mp3",
        "levels.the-courtyard",
        Unlocked()
    )

    val Maintenance = BuiltInLevel(
        this,
        "health_and_safety_maintenance.mp3",
        "levels.maintenance",
        TotalAchievements(5)
    )

    val ForcingTheGamecard = BuiltInLevel(
        this,
        "health_and_safety_forcing_the_gamecard.mp3",
        "levels.forcing-the-gamecard",
        TotalAchievements(10)
    )

    val SharplyBentWire = BuiltInLevel(
        this,
        "health_and_safety_sharply_bent_wire.mp3",
        "levels.sharply-bent-wire",
        TotalAchievements(15)
    )

    val EyeTwitching = BuiltInLevel(
        this,
        "health_and_safety_eye_twitching.mp3",
        "levels.eye-twitching",
        TotalAchievements(20)
    )

    val LightFlashes = BuiltInLevel(
        this,
        "health_and_safety_light_flashes.mp3",
        "levels.light-flashes",
        TotalAchievements(25)
    )

    val PlayInAWellLitRoom = BuiltInLevel(
        this,
        "health_and_safety_play_in_a_well_lit_room.mp3",
        "levels.play-in-a-well-lit-room",
        TotalAchievements(30)
    )

    val ContactWithMoistureAndDirt = BuiltInLevel(
        this,
        "health_and_safety_contact_with_moisture_and_dirt.mp3",
        "levels.contact-with-moisture-and-dirt",
        TotalAchievements(35)
    )

    val TheBallroom = BuiltInLevel(
        this,
        "the_haunted_mansion_the_ballroom.mp3",
        "levels.the-ballroom",
        TotalAchievements(40)
    )

    val OldClock = BuiltInLevel(
        this,
        "awakenings_old_clock.mp3",
        "levels.old-clock",
        TotalAchievements(45)
    )

    val RegulationsForEquipment = BuiltInLevel(
        this,
        "health_and_safety_regulations_for_equipment_use.mp3",
        "levels.regulations-for-equipment",
        TotalAchievements(50)
    )

    val Convulsions = BuiltInLevel(
        this,
        "health_and_safety_convulsions.mp3",
        "levels.convulsions",
        TotalAchievements(55)
    )

    val ContactWithDustAndLint = BuiltInLevel(
        this,
        "health_and_safety_contact_with_dust_and_lint.mp3",
        "levels.contact-with-dust-and-lint",
        TotalAchievements(60)
    )

    val TheExerciseRoom = BuiltInLevel(
        this,
        "the_haunted_mansion_the_exercise_room.mp3",
        "levels.the-exercise-room",
        TotalAchievements(65)
    )

    val Vivaldi = BuiltInLevel(
        this,
        "vivaldi.mp3",
        "levels.vivaldi",
        TotalAchievements(75)
    )

    val ReorientTheReceivingAntenna = BuiltInLevel(
        this,
        "health_and_safety_reorient_the_receiving_antenna.mp3",
        "levels.reorient-the-receiving-antenna",
        TotalAchievements(80)
    )

}

abstract class UnlockRequirements {
    abstract fun isLocked(achievements: List<Achievement>): Boolean
    abstract fun isAlmostUnlocked(achievements: List<Achievement>): Boolean
    abstract fun describeOutstandingRequirements(strings: I18NBundle, achievements: List<Achievement>): String
}

class Unlocked: UnlockRequirements() {
    override fun isLocked(achievements: List<Achievement>) = false
    override fun isAlmostUnlocked(achievements: List<Achievement>) = false
    override fun describeOutstandingRequirements(strings: I18NBundle, achievements: List<Achievement>) = ""
}

class TotalAchievements(val numRequired: Int, val numUntilAlmostUnlocked: Int = 10): UnlockRequirements() {
    override fun isLocked(achievements: List<Achievement>): Boolean {
        return achievements.size < numRequired
    }

    override fun isAlmostUnlocked(achievements: List<Achievement>): Boolean {
        return numRequired - achievements.size <= numUntilAlmostUnlocked
    }

    override fun describeOutstandingRequirements(strings: I18NBundle, achievements: List<Achievement>): String {
        val numLeft = numRequired - achievements.size
        return strings.format("achievements.num-left", numLeft)
    }
}
