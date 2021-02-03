package com.serwylo.beatgame.levels

import com.serwylo.beatgame.levels.achievements.Achievement

data class Level(
        val mp3Name: String,
        val label: String,
        val unlockRequirements: UnlockRequirements
)

object Levels {

    val Maintenance = Level(
            "health_and_safety_maintenance.mp3",
            "Maintenance",
            Unlocked()
    )

    val TheCourtyard = Level(
            "the_haunted_mansion_the_courtyard.mp3",
            "The Courtyard",
            Unlocked()
    )

    val ForcingTheGamecard = Level(
            "health_and_safety_forcing_the_gamecard.mp3",
            "Forcing the Gamecard",
            TotalAchievements(5)
    )

    val SharplyBentWire = Level(
            "health_and_safety_sharply_bent_wire.mp3",
            "Sharply Bent Wire",
            TotalAchievements(10)
    )

    val TheLaundryRoom = Level(
            "the_haunted_mansion_the_laundry_room.mp3",
            "The Laundry Room",
            TotalAchievements(15)
    )

    val EyeTwitching = Level(
            "health_and_safety_eye_twitching.mp3",
            "Eye Twitching",
            TotalAchievements(20)
    )

    val LightFlashes = Level(
            "health_and_safety_light_flashes.mp3",
            "Light Flashes",
            TotalAchievements(25)
    )

    val PlayInAWellLitRoom = Level(
            "health_and_safety_play_in_a_well_lit_room.mp3",
            "Play in a Well Lit Room",
            TotalAchievements(30)
    )

    val ContactWithMoistureAndDirt = Level(
            "health_and_safety_contact_with_moisture_and_dirt.mp3",
            "Contact with Moisture and Dirt",
            TotalAchievements(35)
    )

    val TheBallroom = Level(
            "the_haunted_mansion_the_ballroom.mp3",
            "The Ballroom",
            TotalAchievements(40)
    )

    val OldClock = Level(
            "awakenings_old_clock.mp3",
            "Old Clock",
            TotalAchievements(45)
    )

    val RegulationsForEquipment = Level(
            "health_and_safety_regulations_for_equipment_use.mp3",
            "Regulations for Equipment",
            TotalAchievements(50)
    )

    val Convulsions = Level(
            "health_and_safety_convulsions.mp3",
            "Convulsions",
            TotalAchievements(55)
    )

    val ContactWithDustAndLint = Level(
            "health_and_safety_contact_with_dust_and_lint.mp3",
            "Contact with Dust and Lint",
            TotalAchievements(60)
    )

    val TheExerciseRoom = Level(
            "the_haunted_mansion_the_exercise_room.mp3",
            "The Exercise Room",
            TotalAchievements(65)
    )

    val Vivaldi = Level(
            "vivaldi.mp3",
            "Vivaldi",
            TotalAchievements(100)
    )

    val ReorientTheReceivingAntenna = Level(
            "health_and_safety_reorient_the_receiving_antenna.mp3",
            "Reorient the Receiving Antenna",
            TotalAchievements(150)
    )

    val Custom  = Level(
            "custom.mp3",
            "{Custom}",
            Unlocked()
    )

    val all = listOf(
            Maintenance,
            TheCourtyard,
            ForcingTheGamecard,
            SharplyBentWire,
            TheLaundryRoom,
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

    fun bySong(mp3Name: String): Level {
        return all.find { it.mp3Name == mp3Name }
                ?: error("Could not find level corresponding to mp3 $mp3Name")
    }

}

abstract class UnlockRequirements {
    abstract fun isLocked(achievements: List<Achievement>): Boolean
    abstract fun isAlmostUnlocked(achievements: List<Achievement>): Boolean
    abstract fun describeOutstandingRequirements(achievements: List<Achievement>): String
}

class Unlocked: UnlockRequirements() {
    override fun isLocked(achievements: List<Achievement>) = false
    override fun isAlmostUnlocked(achievements: List<Achievement>) = false
    override fun describeOutstandingRequirements(achievements: List<Achievement>) = ""
}

class TotalAchievements(val numRequired: Int, val numUntilAlmostUnlocked: Int = 10): UnlockRequirements() {
    override fun isLocked(achievements: List<Achievement>): Boolean {
        return achievements.size < numRequired
    }

    override fun isAlmostUnlocked(achievements: List<Achievement>): Boolean {
        return numRequired - achievements.size <= numUntilAlmostUnlocked
    }

    override fun describeOutstandingRequirements(achievements: List<Achievement>): String {
        val numLeft = numRequired - achievements.size
        return if (numLeft == 1) {
            "1 more achievement"
        } else {
            "$numLeft more achievements"
        }
    }
}
