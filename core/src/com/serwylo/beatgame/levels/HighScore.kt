package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import kotlin.math.max

data class HighScore(val distancePercent: Float, val points: Int, val timestamp: Long, val attempts: Int) {

    fun distancePercentString() = "${(distancePercent * 100).toInt()}%"
    fun exists() = distancePercent > 0 || points > 0

}

fun loadHighScore(level: Level): HighScore {
    val json = prefs().getString(level.mp3Name, "")
    return if (json == "") {
        HighScore(0f, 0, 0, 0)
    } else {
        Gson().fromJson(json, HighScore::class.java)
    }
}

fun saveHighScore(level: Level, score: Score, force: Boolean = false): HighScore {
    val highest = loadHighScore(level)

    val toSave = if (force) {
        HighScore(score.distancePercent, score.getPoints(), System.currentTimeMillis(), 0)
    } else {
        HighScore(
                max(highest.distancePercent, score.distancePercent),
                max(highest.points, score.getPoints()),
                System.currentTimeMillis(),
                highest.attempts + 1
        )
    }

    val json = Gson().toJson(toSave)

    prefs().putString(level.mp3Name, json).flush()

    return toSave
}

private fun prefs() = Gdx.app.getPreferences("com.serwylo.beat-game.scores")