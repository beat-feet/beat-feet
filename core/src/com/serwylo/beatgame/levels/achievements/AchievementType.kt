package com.serwylo.beatgame.levels.achievements

import com.serwylo.beatgame.levels.HighScore
import com.serwylo.beatgame.levels.Score

abstract class AchievementType(
        val id: String,
        val label: String
) {
    abstract fun isAchieved(score: Score, highScore: HighScore): Boolean
}

abstract class Combo(private val num: Int): AchievementType("combo-$num", "${num}x combo") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return score.getMaxMultiplier() >= num
    }
}

class ComboX5: Combo(5)
class ComboX10: Combo(10)
class ComboX25: Combo(25)
class ComboX50: Combo(50)
class ComboX100: Combo(100)

abstract class Attempts(private val num: Int): AchievementType("attempts-$num", "$num attempts") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return highScore.attempts >= num
    }
}

class AttemptsX5: Attempts(5)
class AttemptsX10: Attempts(10)
class AttemptsX25: Attempts(25)
class AttemptsX50: Attempts(50)
class AttemptsX100: Attempts(100)

abstract class Distance(private val num: Int): AchievementType("distance-$num", "$num% complete") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return (score.distancePercent * 100).toInt() >= num
    }
}

class DistanceX10: Distance(10)
class DistanceX25: Distance(25)
class DistanceX50: Distance(50)
class DistanceX75: Distance(75)

class FinishedLevel: AchievementType("finished-level", "Completed!") {
    override fun isAchieved(score: Score, highScore: HighScore): Boolean {
        return score.distancePercent >= 1f
    }
}

val allAchievements = listOf(
        FinishedLevel(),
        DistanceX10(),
        DistanceX25(),
        DistanceX50(),
        DistanceX75(),
        ComboX5(),
        ComboX10(),
        ComboX25(),
        ComboX50(),
        ComboX100(),
        AttemptsX5(),
        AttemptsX10(),
        AttemptsX25(),
        AttemptsX50(),
        AttemptsX100()
)
