package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.levels.achievements.Achievement
import java.io.File

interface Level {
    fun getId(): String
    fun getMp3File(): FileHandle
    fun getLabel(strings: I18NBundle): String
    fun getUnlockRequirements(): UnlockRequirements
}

class BuiltInLevel(
    private val mp3Name: String,
    private val labelId: String,
    private val toUnlock: UnlockRequirements
): Level {

    override  fun getId(): String {
        return mp3Name
    }

    override fun getMp3File(): FileHandle {
        return Gdx.files.internal("songs${File.separator}mp3${File.separator}${mp3Name}")
    }

    override fun getLabel(strings: I18NBundle): String {
        return strings[labelId]
    }

    override fun getUnlockRequirements(): UnlockRequirements {
        return toUnlock
    }

}

/* TODO: Dynamically fetch levels from the web.
class RemoteLevel(): Level {
    override fun getMp3File(): FileHandle {
    }

    override fun getLabel(strings: I18NBundle): String {
    }

    override fun getUnlockRequirements(): UnlockRequirements {
    }

}
*/

object Levels {

    val TheLaundryRoom = BuiltInLevel(
            "the_haunted_mansion_the_laundry_room.mp3",
            "levels.the-laundry-room",
            Unlocked()
    )

    val TheCourtyard = BuiltInLevel(
            "the_haunted_mansion_the_courtyard.mp3",
            "levels.the-courtyard",
            Unlocked()
    )

    val Maintenance = BuiltInLevel(
            "health_and_safety_maintenance.mp3",
            "levels.maintenance",
            TotalAchievements(5)
    )

    val ForcingTheGamecard = BuiltInLevel(
            "health_and_safety_forcing_the_gamecard.mp3",
            "levels.forcing-the-gamecard",
            TotalAchievements(10)
    )

    val SharplyBentWire = BuiltInLevel(
            "health_and_safety_sharply_bent_wire.mp3",
            "levels.sharply-bent-wire",
            TotalAchievements(15)
    )

    val EyeTwitching = BuiltInLevel(
            "health_and_safety_eye_twitching.mp3",
            "levels.eye-twitching",
            TotalAchievements(20)
    )

    val LightFlashes = BuiltInLevel(
            "health_and_safety_light_flashes.mp3",
            "levels.light-flashes",
            TotalAchievements(25)
    )

    val PlayInAWellLitRoom = BuiltInLevel(
            "health_and_safety_play_in_a_well_lit_room.mp3",
            "levels.play-in-a-well-lit-room",
            TotalAchievements(30)
    )

    val ContactWithMoistureAndDirt = BuiltInLevel(
            "health_and_safety_contact_with_moisture_and_dirt.mp3",
            "levels.contact-with-moisture-and-dirt",
            TotalAchievements(35)
    )

    val TheBallroom = BuiltInLevel(
            "the_haunted_mansion_the_ballroom.mp3",
            "levels.the-ballroom",
            TotalAchievements(40)
    )

    val OldClock = BuiltInLevel(
            "awakenings_old_clock.mp3",
            "levels.old-clock",
            TotalAchievements(45)
    )

    val RegulationsForEquipment = BuiltInLevel(
            "health_and_safety_regulations_for_equipment_use.mp3",
            "levels.regulations-for-equipment",
            TotalAchievements(50)
    )

    val Convulsions = BuiltInLevel(
            "health_and_safety_convulsions.mp3",
            "levels.convulsions",
            TotalAchievements(55)
    )

    val ContactWithDustAndLint = BuiltInLevel(
            "health_and_safety_contact_with_dust_and_lint.mp3",
            "levels.contact-with-dust-and-lint",
            TotalAchievements(60)
    )

    val TheExerciseRoom = BuiltInLevel(
            "the_haunted_mansion_the_exercise_room.mp3",
            "levels.the-exercise-room",
            TotalAchievements(65)
    )

    val Vivaldi = BuiltInLevel(
            "vivaldi.mp3",
            "levels.vivaldi",
            TotalAchievements(75)
    )

    val ReorientTheReceivingAntenna = BuiltInLevel(
            "health_and_safety_reorient_the_receiving_antenna.mp3",
            "levels.reorient-the-receiving-antenna",
            TotalAchievements(80)
    )

    val Custom  = BuiltInLevel(
            "custom.mp3",
            "levels.custom",
            Unlocked()
    )

    val all = listOf(
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
            Custom
    )

    fun byId(id: String): Level {
        return all.find { it.getId() == id }
                ?: error("Could not find level with ID: \"$id\"")
    }

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
