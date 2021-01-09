package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import kotlin.math.max

class Score(val distancePercent: Float, val score: Int) {

    companion object {
        fun load(musicFileName: String): Score {
            val prefs = prefs()
            val distancePercent = prefs.getFloat(distanceKey(musicFileName), 0f)
            val score = prefs.getInteger(scoreKey(musicFileName), 0)
            return Score(distancePercent, score)
        }

        fun save(musicFileName: String, distancePercent: Float, score: Int) {
            val highest = load(musicFileName)

            val prefs = prefs()
            prefs.putFloat(distanceKey(musicFileName), max(highest.distancePercent, distancePercent))
            prefs.putInteger(scoreKey(musicFileName), max(highest.score, score))
            prefs.flush()
        }

        private fun prefs() = Gdx.app.getPreferences("scores")
        private fun distanceKey(musicFileName: String) = "score-distancePercent-$musicFileName"
        private fun scoreKey(musicFileName: String) = "score-score-$musicFileName"
    }

}