package com.serwylo.beatgame.audio.features

/**
 * Something of interest found in the music. Has a strength and duration, as well as a point in time.
 */
data class Feature(val strength: Float, val startTimeInSeconds: Float, val durationInSeconds: Float) {

    override fun toString(): String {
        return "Feature [strength: $strength, ${startTimeInSeconds}s -> ${startTimeInSeconds + durationInSeconds}s]"
    }

}