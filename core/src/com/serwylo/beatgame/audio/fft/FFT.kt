package com.serwylo.beatgame.audio.fft

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.serwylo.beatgame.audio.Mp3Data
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.MP3Decoder
import javazoom.jl.decoder.OutputBuffer
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.ln
import kotlin.math.min

fun calculateMp3FFT(mp3InputStream: InputStream): FFTResult {

    val mp3Data = readPcm(mp3InputStream)

    val windowSize = 1024

    val numWindows = mp3Data.pcmSamples.size / windowSize
    val windows = ArrayList<FFTWindow>(numWindows)
    for (windowIndex in 0..numWindows) {
        val frequencyValues = calculateFFTWindow(mp3Data, windowIndex, windowSize)
        windows.add(FFTWindow.create(windowIndex, frequencyValues))
    }

    return FFTResult(mp3Data, windowSize, windows)

}

fun calculateMp3FFTWithValues(mp3InputStream: InputStream): FFTResultWithValues {

    val mp3Data = readPcm(mp3InputStream)

    val windowSize = 1024

    val numWindows = mp3Data.pcmSamples.size / windowSize
    val windows = ArrayList<FFTWindowWithValues>(numWindows)
    for (windowIndex in 0..numWindows) {
        val frequencyValues = calculateFFTWindow(mp3Data, windowIndex, windowSize)
        windows.add(FFTWindowWithValues.create(windowIndex, frequencyValues))
    }

    return FFTResultWithValues(mp3Data, windowSize, windows)

}

fun smoothFFT(spectogram:FFTResultWithValues, smoothingWindow: Int): FFTResultWithValues {

    if (smoothingWindow % 2 == 0) {
        throw IllegalArgumentException("Smoothing window must be an odd number.")
    }

    val eitherSide:Int = smoothingWindow / 2

    val smoothSeries = ArrayList<FFTWindowWithValues>(spectogram.windows.size)

    for (i in spectogram.windows.indices) {
        val start = (i - eitherSide).coerceAtLeast(0)
        val end = (i + eitherSide).coerceAtMost(spectogram.windows.size - 1)

        val window = spectogram.windows.slice(IntRange(start, end)).reduce { acc, window ->
            val newValues: List<FrequencyValue> = acc.values.mapIndexed { i, value ->
                FrequencyValue(value.frequency, value.absValue + window.values[i].absValue)
            }

            val avgValues = newValues.map { FrequencyValue(it.frequency, it.absValue / (end - start)) }

            FFTWindowWithValues.create(i, newValues)
        }

        smoothSeries.add(window)
    }

    return FFTResultWithValues(spectogram.mp3Data, spectogram.windowSize, smoothSeries)
}

fun renderSpectogram(fftResult: FFTResultWithValues): Pixmap {

    val pixmap = Pixmap(
            fftResult.windows[0].values.size,
            fftResult.windows.size,
            Pixmap.Format.RGB888
    )

    var minAbsValue = Double.MAX_VALUE
    var maxAbsValue = Double.MIN_VALUE

    var minLogValue = Double.MAX_VALUE
    var maxLogValue = Double.MAX_VALUE

    fftResult.windows.forEach { window ->
        window.values.forEach { value ->

            if (value.absValue < minAbsValue) {
                minAbsValue = value.absValue
                minLogValue = value.logAbsValue
            }

            if (value.absValue > maxAbsValue) {
                maxAbsValue = value.absValue
                maxLogValue = value.logAbsValue
            }

        }
    }

    val absRange = maxAbsValue - minAbsValue
    val logRange = maxLogValue - minLogValue

    fftResult.windows.forEachIndexed { windowIndex, window ->

        val y = fftResult.windows.size - windowIndex

        window.values.forEachIndexed { valueIndex, value ->

            val x = translateX(valueIndex.toFloat())
            val absIntensity = ((value.absValue - minAbsValue) / absRange).toFloat()
            val logIntensity = ((value.logAbsValue - minLogValue) / logRange).toFloat()

            val absColour = Color(absIntensity, absIntensity * 6, absIntensity,1.0f)
            val logColour = Color(logIntensity / 2, logIntensity / 3, logIntensity / 4, 1.0f)

            pixmap.drawPixel(x.toInt(), y, absColour.add(logColour).toIntBits())

        }

    }

    return pixmap

}

private fun translateX(x: Float) = ln(x) * 20

private val buffers = HashMap<Int, DoubleArray>()

/**
 * GC friendly way of making [DoubleArray]s available without having to constantly recreate
 * for every window of the MP3 we are processing.
 */
private fun getBuffer(size: Int): DoubleArray {
    val existingBuffer = buffers[size]
    if (existingBuffer != null) {
        return existingBuffer
    }

    val newBuffer = DoubleArray(size)
    buffers[size] = newBuffer
    return newBuffer
}

private fun calculateFFTWindow(mp3Data: Mp3Data, windowIndex: Int, windowSize: Int): List<FrequencyValue> {

    val startSample = windowIndex * windowSize
    val endSample = min(mp3Data.pcmSamples.size, startSample + windowSize)

    val samples = DoubleArray(windowSize)
    val samplesToCast = mp3Data.pcmSamples.slice(IntRange(startSample, endSample - 1))

    for (i in samplesToCast.indices) {
        samples[i] = samplesToCast[i].toDouble()
    }

    // For the case where we ran up against the end of the music file, and we didn't fill
    // the buffer. We still require the data to be a power of two, so continue filling 0's
    // as per the commons-math documentation suggests.
    for (i in samplesToCast.size until windowSize) {
        samples[i] = 0.0
    }

    // Interpreting the x axis of FFT results.
    // https://stackoverflow.com/a/4371627
    val fft = FastFourierTransformer(DftNormalization.STANDARD)
    val fftResult = fft.transform(samples, TransformType.FORWARD)

    // The second half of the results are the mirror image of the first half
    val size = samples.size / 2 + 1
    val values = ArrayList<FrequencyValue>(size)
    for (i in 0 until size) {
        values.add(FrequencyValue(
                frequency = i.toDouble() * mp3Data.sampleRate / windowSize,
                absValue = fftResult[i].abs()
        ))
    }

    return values

}

/**
 * Originally from libgdx Mp3.Sound class (Licensed as Apache 2.0)
 */
private fun readPcm(mp3InputStream: InputStream): Mp3Data {

    val output = ByteArrayOutputStream(4096)

    val bitstream = Bitstream(mp3InputStream)
    val decoder = MP3Decoder()

    try {
        var outputBuffer: OutputBuffer? = null
        var sampleRate = -1
        var channels = -1
        while (true) {
            val header = bitstream.readFrame() ?: break
            if (outputBuffer == null) {
                channels = if (header.mode() == Header.SINGLE_CHANNEL) 1 else 2
                outputBuffer = OutputBuffer(channels, true)
                decoder.setOutputBuffer(outputBuffer)
                sampleRate = header.sampleRate
            }
            try {
                decoder.decodeFrame(header, bitstream)
            } catch (ignored: Exception) {
                // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
            }
            bitstream.closeFrame()
            output.write(outputBuffer.buffer, 0, outputBuffer.reset())
        }
        bitstream.close()
        return Mp3Data(output.toByteArray(), channels, sampleRate)
    } catch (e: java.lang.Exception) {
        return Mp3Data(ByteArray(0), 0, 0)
    }

}
