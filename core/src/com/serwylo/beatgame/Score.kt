package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import kotlin.math.max

class Score(val distancePercent: Float, val score: Int, val timestamp: Long) {

    fun distancePercentString() = "${(distancePercent * 100).toInt()}%"
    fun exists() = distancePercent > 0 || score > 0

    companion object {
        fun load(musicFileName: String): Score {
            val prefs = prefs()
            val distancePercent = prefs.getFloat(distanceKey(musicFileName), 0f)
            val score = prefs.getInteger(scoreKey(musicFileName), 0)
            val timestamp = prefs.getLong(timestampKey(musicFileName), 0)
            return Score(distancePercent, score, timestamp)
        }

        fun save(musicFileName: String, distancePercent: Float, score: Int, force: Boolean = false) {
            val highest = load(musicFileName)

            val distancePercentToSave = if (force) distancePercent else max(highest.distancePercent, distancePercent)
            val scoreToSave = if (force) score else max(highest.score, score)

            val prefs = prefs()
            prefs.putFloat(distanceKey(musicFileName), distancePercentToSave)
            prefs.putInteger(scoreKey(musicFileName), scoreToSave)
            prefs.putLong(timestampKey(musicFileName), System.currentTimeMillis())
            prefs.flush()
        }

        private fun prefs() = Gdx.app.getPreferences("scores")
        private fun distanceKey(musicFileName: String) = "$musicFileName-distancePercent"
        private fun scoreKey(musicFileName: String) = "$musicFileName-score"
        private fun timestampKey(musicFileName: String) = "$musicFileName-timestamp"
    }

}