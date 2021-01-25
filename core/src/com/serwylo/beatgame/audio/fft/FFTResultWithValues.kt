package com.serwylo.beatgame.audio.fft

import com.serwylo.beatgame.audio.Mp3Data

data class FFTResultWithValues(
        val mp3Data: Mp3Data,
        val windowSize: Int,
        val windows: List<FFTWindowWithValues>
) {
    fun toResult() = FFTResult(mp3Data, windowSize, windows.map { it.toWindow() })
}