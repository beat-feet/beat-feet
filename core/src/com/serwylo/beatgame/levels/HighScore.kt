package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import kotlin.math.max

data class HighScore(val distancePercent: Float, val points: Int, val timestamp: Long, val attempts: Int) {

    fun distancePercentString() = "${(distancePercent * 100).toInt()}%"
    fun exists() = distancePercent > 0 || points > 0

}

fun clearAllHighScores() {
    prefs().clear()
}

fun loadHighScore(level: Level): HighScore {
    val json = prefs().getString(level.getId(), "")
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

    prefs().putString(level.getId(), json).flush()

    return toSave
}

/**
 * @see loadHasPerformedDoubleJump
 */
fun saveHasPerformedDoubleJump(value: Boolean) {
    prefs().putBoolean("didDoubleJump", value).flush()
}

/**
 * Each game, keep track of whether or not a player managed to double jump at all during the game.
 * If not, assume that the player is new and hasn't yet figured out how to double jump.
 * This information will be used to change the start game message hinting at the fact you can
 * double tap to jump higher.
 *
 * Defaults to true because the first time we ask this question, we don't want to show this
 * additional help message.
 */
fun loadHasPerformedDoubleJump() =
    prefs().getBoolean("didDoubleJump", true)

private fun prefs() = Gdx.app.getPreferences("com.serwylo.beat-game.scores")