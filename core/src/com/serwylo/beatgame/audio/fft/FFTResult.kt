package com.serwylo.beatgame.audio.fft

import com.serwylo.beatgame.audio.Mp3Data

data class FFTResult(
        val mp3Data: Mp3Data,
        val windowSize: Int,
        val windows: List<FFTWindow>
)
