package com.serwylo.beatgame.levels.achievements

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels

fun saveAchievements(level: Level, achievements: List<AchievementType>) {

    Gdx.app.log(LOGGER, "Saving achievements: ${achievements.map { it.id }.joinToString(", ")}")

    val toPersist = loadPersistedAchievements().append(achievements, level)
    val json = Gson().toJson(toPersist)

    prefs()
            .putString("achievements", json)
            .flush()

}

fun clearAllAchievements() {
    prefs().putString("achievements", "").flush()
}

fun loadAchievementsForLevel(level: Level): List<AchievementType> {
    val saved = loadPersistedAchievements()

    return saved.forLevel(level).map {
        allAchievements.find { it2 -> it2.id == it.achievementId }!!
    }
}

fun loadAllAchievements(): List<Achievement> {
    return loadPersistedAchievements().achievements.map { persisted ->
        Achievement(
                allAchievements.find { it.id == persisted.achievementId }!!,
                Levels.bySong(persisted.levelId)
        )
    }
}

private fun loadPersistedAchievements(): PersistedAchievements {
    val json = prefs().getString("achievements", "")

    if (json == "") {
        return PersistedAchievements(listOf())
    }

    return Gson().fromJson(json, PersistedAchievements::class.java)
}

private fun prefs() = Gdx.app.getPreferences("com.serwylo.beat-game.achievements")

const val LOGGER = "Achievements"

private data class PersistedAchievement(
        val achievementId: String,
        val levelId: String
)

private data class PersistedAchievements(
        val achievements: List<PersistedAchievement>
) {

    fun append(newAchievements: List<AchievementType>, level: Level): PersistedAchievements {
        val existing = forLevel(level)

        val toAdd = newAchievements
                .filterNot { newAchievement -> existing.any { existingAchievement -> existingAchievement.achievementId == newAchievement.id } }
                .map { PersistedAchievement(it.id, level.mp3Name) }

        return PersistedAchievements(achievements.plus(toAdd))
    }

    fun forLevel(level: Level): List<PersistedAchievement> {
        return achievements.filter { it.levelId == level.mp3Name }
    }

    val version = currentVersion

    companion object {

        /**
         * Change this every time we modify the signature of this class, to ensure we don't accidentally
         * try to process a legacy .json file of a different format after upgrading the game.
         *
         * When the version upgrades to a new version, we can write unit tests to ensure they
         * migrate correctly (don't want people losing their trophies!).
         */
        const val currentVersion = 1

    }

}
