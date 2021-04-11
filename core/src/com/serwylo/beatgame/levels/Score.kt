package com.serwylo.beatgame.levels

class Score(
    /**
     * Although we only really want to work in integer points, because the score
     * is updated depending on fractions of a second, it seems we should probably store it as
     * a float and truncate when required.
     */
    private var points: Float = 0f,
    var distancePercent: Float = 0f
) {

    private var multiplier: Float = 1f
    private var maxMultiplier: Int = 1

    fun getPoints(): Int = points.toInt()
    fun getMultiplier(): Int = multiplier.toInt()
    fun getMaxMultiplier(): Int = maxMultiplier

    fun increase(delta: Float) {
        points += SCORE_PER_SECOND * delta * multiplier
    }

    fun resetMultiplier() {
        multiplier = 1f
    }

    fun increaseMultiplier() {
        multiplier += 0.5f

        // Record this so we can grant achievements at the end of the level.
        if (getMultiplier() > maxMultiplier) {
            maxMultiplier = getMultiplier()
        }
    }

    fun progress(distancePercent: Float) {
        this.distancePercent = distancePercent
    }

    companion object {

        const val SCORE_PER_SECOND = 100

    }

}