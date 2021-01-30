package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.levels.Score
import kotlin.math.max

class HighScore(val distancePercent: Float, val points: Int, val timestamp: Long) {

    fun distancePercentString() = "${(distancePercent * 100).toInt()}%"
    fun exists() = distancePercent > 0 || points > 0

    companion object {
        fun load(musicFileName: String): HighScore {
            val prefs = prefs()
            val distancePercent = prefs.getFloat(distanceKey(musicFileName), 0f)
            val score = prefs.getInteger(pointsKey(musicFileName), 0)
            val timestamp = prefs.getLong(timestampKey(musicFileName), 0)
            return HighScore(distancePercent, score, timestamp)
        }

        fun save(musicFileName: String, score: Score, force: Boolean = false) {
            val highest = load(musicFileName)

            val distancePercentToSave = if (force) score.distancePercent else max(highest.distancePercent, score.distancePercent)
            val pointsToSave = if (force) score.getPoints() else max(highest.points, score.getPoints())

            val prefs = prefs()
            prefs.putFloat(distanceKey(musicFileName), distancePercentToSave)
            prefs.putInteger(pointsKey(musicFileName), pointsToSave)
            prefs.putLong(timestampKey(musicFileName), System.currentTimeMillis())
            prefs.flush()
        }

        private fun prefs() = Gdx.app.getPreferences("scores")
        private fun distanceKey(musicFileName: String) = "$musicFileName-distancePercent"
        private fun pointsKey(musicFileName: String) = "$musicFileName-score"
        private fun timestampKey(musicFileName: String) = "$musicFileName-timestamp"
    }

}