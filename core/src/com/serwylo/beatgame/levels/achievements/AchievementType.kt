package com.serwylo.beatgame.levels.achievements

import com.serwylo.beatgame.levels.HighScore
import com.serwylo.beatgame.levels.Score

/**
 * @param id Used to save and reference this achievement, but also used for i18n strings to describe them.
 */
abstract class AchievementType(val id: String) {
    abstract fun isAchieved(score: Score, highScore: HighScore): Boolean
}

abstract class Combo(private val num: Int): AchievementType("combo-$num") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return score.getMaxMultiplier() >= num
    }
}

class ComboX5: Combo(5)
class ComboX10: Combo(10)
class ComboX25: Combo(25)
class ComboX50: Combo(50)

abstract class Attempts(private val num: Int): AchievementType("attempts-$num") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return highScore.attempts >= num
    }
}

class AttemptsX5: Attempts(5)
class AttemptsX25: Attempts(25)

abstract class Distance(private val num: Int): AchievementType("distance-$num") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return (score.distancePercent * 100).toInt() >= num
    }
}

class DistanceX10: Distance(10)
class DistanceX25: Distance(25)
class DistanceX50: Distance(50)
class DistanceX75: Distance(75)

class FinishedLevel: AchievementType("finished-level") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return score.distancePercent >= 1f
    }
}

val allAchievements = listOf(
        DistanceX10(),
        DistanceX25(),
        DistanceX50(),
        DistanceX75(),
        ComboX5(),
        ComboX10(),
        ComboX25(),
        ComboX50(),
        AttemptsX5(),
        AttemptsX25(),
        FinishedLevel()
)
