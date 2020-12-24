package com.serwylo.beatgame.audio

import java.nio.ByteBuffer

data class Mp3Data(
        private val bytes: ByteArray,
        val channels: Int,
        val sampleRate: Int
) {

    val pcmSamples = ShortArray(bytes.size / 2 / channels)

    init {

        for (i in pcmSamples.indices) {
            val channel1 = ByteBuffer.wrap(bytes, i * 2 * channels, 2).short
            val channel2 = if (channels == 2) ByteBuffer.wrap(bytes, i * 2 * channels + 2, 2).short else channel1
            pcmSamples[i] = ((channel1 + channel2) / 2).toShort()
        }

    }

}
