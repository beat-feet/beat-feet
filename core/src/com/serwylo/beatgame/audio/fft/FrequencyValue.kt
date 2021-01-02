package com.serwylo.beatgame.audio.fft

import kotlin.math.ln

data class FrequencyValue(
        val frequency: Double,
        val absValue: Double
) {

    val logAbsValue: Double
        get() {
            val log = ln(absValue)
            return if (log == Double.NEGATIVE_INFINITY) 0.0 else log
        }
}
