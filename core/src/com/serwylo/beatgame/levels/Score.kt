package com.serwylo.beatgame.levels

class Score {

    /**
     * Although we only really want to work in integer points, because the score
     * is updated depending on fractions of a second, it seems we should probably store it as
     * a float and truncate when required.
     */
    private var points: Float = 0f

    var distancePercent: Float = 0f

    private var multiplier: Float = 1f

    fun getPoints(): Int = points.toInt()

    fun getMultiplier(): Int = multiplier.toInt()

    fun increase(delta: Float) {
        points += SCORE_PER_SECOND * delta * multiplier
    }

    fun resetMultiplier() {
        multiplier = 1f
    }

    fun increaseMultiplier() {
        multiplier += 0.5f
    }

    fun progress(distancePercent: Float) {
        this.distancePercent = distancePercent
    }

    companion object {

        const val SCORE_PER_SECOND = 100

    }
}