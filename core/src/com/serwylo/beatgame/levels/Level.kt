package com.serwylo.beatgame.levels

import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.levels.achievements.Achievement

data class Level(
        val mp3Name: String,
        val labelId: String,
        val unlockRequirements: UnlockRequirements
)

data class LevelGroup(
    val number: Int,
    val labelId: String,
    val levels: List<Level>,
)

object Levels {

    object SpaceJacked {

        val Tutorial = Level(
            "spacejacked_01_tutorial.mp3",
            "levels.spacejacked.tutorial",
            Unlocked()
        )

        val Splash = Level(
            "spacejacked_02_splash.mp3",
            "levels.spacejacked.splash",
            Unlocked()
        )

        val Ship = Level(
            "spacejacked_03_ship.mp3",
            "levels.spacejacked.ship",
            Unlocked()
        )

        val ShipUnderAttack = Level(
            "spacejacked_04_ship_under_attack.mp3",
            "levels.spacejacked.ship-under-attack",
            Unlocked()
        )

        val GoGoGo = Level(
            "spacejacked_05_go_go_go.mp3",
            "levels.spacejacked.go-go-go",
            Unlocked()
        )

        val GoGoFasterFaster = Level(
            "spacejacked_06_go_go_faster_faster.mp3",
            "levels.spacejacked.go-go-faster-faster",
            Unlocked()
        )

        val LetsRest = Level(
            "spacejacked_07_lets_rest.mp3",
            "levels.spacejacked.lets-rest",
            Unlocked()
        )

        val Rescued = Level(
            "spacejacked_08_rescued.mp3",
            "levels.spacejacked.rescued",
            Unlocked()
        )

        val Metallius = Level(
            "spacejacked_09_metallius.mp3",
            "levels.spacejacked.metallius",
            Unlocked()
        )

        val GoGoMetallius = Level(
            "spacejacked_10_go_go_metallius.mp3",
            "levels.spacejacked.go_go_metallius",
            Unlocked()
        )

        val WhereAmI = Level(
            "spacejacked_11_where_am_i.mp3",
            "levels.spacejacked.where-am-i",
            Unlocked()
        )

        val WhereverAliens = Level(
            "spacejacked_12_wherever_aliens.mp3",
            "levels.spacejacked.wherever-aliens",
            Unlocked()
        )

        val MoreAliens = Level(
            "spacejacked_13_more_aliens.mp3",
            "levels.spacejacked.more-aliens",
            Unlocked()
        )

        val SuperSuper = Level(
            "spacejacked_15_super_super.mp3",
            "levels.spacejacked.super-super",
            Unlocked()
        )

    }

    val TheLaundryRoom = Level(
            "the_haunted_mansion_the_laundry_room.mp3",
            "levels.the-laundry-room",
            Unlocked()
    )

    val TheCourtyard = Level(
            "the_haunted_mansion_the_courtyard.mp3",
            "levels.the-courtyard",
            Unlocked()
    )

    val Maintenance = Level(
            "health_and_safety_maintenance.mp3",
            "levels.maintenance",
            TotalAchievements(5)
    )

    val ForcingTheGamecard = Level(
            "health_and_safety_forcing_the_gamecard.mp3",
            "levels.forcing-the-gamecard",
            TotalAchievements(10)
    )

    val SharplyBentWire = Level(
            "health_and_safety_sharply_bent_wire.mp3",
            "levels.sharply-bent-wire",
            TotalAchievements(15)
    )

    val EyeTwitching = Level(
            "health_and_safety_eye_twitching.mp3",
            "levels.eye-twitching",
            TotalAchievements(20)
    )

    val LightFlashes = Level(
            "health_and_safety_light_flashes.mp3",
            "levels.light-flashes",
            TotalAchievements(25)
    )

    val PlayInAWellLitRoom = Level(
            "health_and_safety_play_in_a_well_lit_room.mp3",
            "levels.play-in-a-well-lit-room",
            TotalAchievements(30)
    )

    val ContactWithMoistureAndDirt = Level(
            "health_and_safety_contact_with_moisture_and_dirt.mp3",
            "levels.contact-with-moisture-and-dirt",
            TotalAchievements(35)
    )

    val TheBallroom = Level(
            "the_haunted_mansion_the_ballroom.mp3",
            "levels.the-ballroom",
            TotalAchievements(40)
    )

    val OldClock = Level(
            "awakenings_old_clock.mp3",
            "levels.old-clock",
            TotalAchievements(45)
    )

    val RegulationsForEquipment = Level(
            "health_and_safety_regulations_for_equipment_use.mp3",
            "levels.regulations-for-equipment",
            TotalAchievements(50)
    )

    val Convulsions = Level(
            "health_and_safety_convulsions.mp3",
            "levels.convulsions",
            TotalAchievements(55)
    )

    val ContactWithDustAndLint = Level(
            "health_and_safety_contact_with_dust_and_lint.mp3",
            "levels.contact-with-dust-and-lint",
            TotalAchievements(60)
    )

    val TheExerciseRoom = Level(
            "the_haunted_mansion_the_exercise_room.mp3",
            "levels.the-exercise-room",
            TotalAchievements(65)
    )

    val Vivaldi = Level(
            "vivaldi.mp3",
            "levels.vivaldi",
            TotalAchievements(75)
    )

    val ReorientTheReceivingAntenna = Level(
            "health_and_safety_reorient_the_receiving_antenna.mp3",
            "levels.reorient-the-receiving-antenna",
            TotalAchievements(80)
    )

    val Custom  = Level(
            "custom.mp3",
            "levels.custom",
            Unlocked()
    )

    val groups = listOf(
        LevelGroup(1, "level-group.1", listOf(
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
        )),
        LevelGroup(2, "level-group.2", listOf(
            SpaceJacked.Tutorial,
            SpaceJacked.Splash,
            SpaceJacked.Ship,
            SpaceJacked.ShipUnderAttack,
            SpaceJacked.GoGoGo,
            SpaceJacked.GoGoFasterFaster,
            SpaceJacked.LetsRest,
            SpaceJacked.Rescued,
            SpaceJacked.Metallius,
            SpaceJacked.GoGoMetallius,
            SpaceJacked.WhereAmI,
            SpaceJacked.WhereverAliens,
            SpaceJacked.MoreAliens,
            SpaceJacked.SuperSuper,
        )),
    )

    val all = groups.map { it.levels }.flatten()

    fun bySong(mp3Name: String): Level {
        return all.find { it.mp3Name == mp3Name }
                ?: error("Could not find level corresponding to mp3 $mp3Name")
    }

    fun groupForLevel(level: Level) = groups.find { it.levels.contains(level) } ?: error("Could not find world for level: ${level.labelId}")

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
