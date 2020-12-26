package com.serwylo.beatgame.analysis

import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.audio.AudioFeature
import com.serwylo.beatgame.fft.FFTWindow
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

/**
 * Given a particular time series, identify and return a list of [AudioFeature]s, representing
 * interesting parts of the series (e.g. for audio, it may be loud or sudden changes).
 *
 * We need to know the window size + sample rate, in order to convert from the windows used for
 * our FFT to seconds.
 */
fun extractFeaturesFromSeries(series: DoubleArray, windowSize: Int, sampleRate: Int): List<AudioFeature> {

    val analysis = analyseSeries(series)

    val features = mutableListOf<AudioFeature>()
    var currentFeatureStart = -1
    var currentFeatureSum = 0.0
    for (i in analysis.indices) {
        val interesting = analysis[i] > 0
        if (interesting) {
            // Start recording a new feature... Or just continue on recording
            if (currentFeatureStart == -1) {
                currentFeatureStart = i
            }
            currentFeatureSum += series[i]
        } else if (currentFeatureStart >= 0) {
            // We just finished a feature, lets account for it.
            val length = i - currentFeatureStart
            features.add(AudioFeature(
                    strength = currentFeatureSum.toFloat() / length,
                    durationInSeconds = (length * windowSize).toFloat() / sampleRate,
                    startTimeInSeconds = (currentFeatureStart * windowSize).toFloat() / sampleRate
            ))

            currentFeatureStart = -1
            currentFeatureSum = 0.0
        }
    }

    // Normalise each of the possible feature strengths.
    var min = Float.MAX_VALUE
    var max = Float.MIN_VALUE
    features.forEach {
        if (it.strength < min) min = it.strength
        if (it.strength > max) max = it.strength
    }

    return features.map {
        AudioFeature((it.strength - min) / (max - min), it.startTimeInSeconds, it.durationInSeconds)
    }
}

fun extractHeightMapFromSeries(series: DoubleArray, windowSize: Int, sampleRate: Int, maxHeight: Float): Array<Vector2> {
    val normalisation = normaliseSeries(series)
    return Array(series.size) { i ->
        Vector2(
                (i * windowSize).toFloat() / sampleRate,
                ((series[i] - normalisation.min) / normalisation.range).toFloat() * maxHeight
        )
    }
}

/**
 * A poor substitute for differentiating the series to find rates of change.
 * This naively checks:
 *  - If the previous point is less than this point, but the next one is lower (we are a peak) or the same (we just plateaued).
 *  - If we should continue a particular peak for a longer duration. This happens if the next data point is the same and we are already recording a feature.
 */
fun analyseSeries(series: DoubleArray): DoubleArray {

    val data = DoubleArray(series.size)

    for (i in 1 until series.size - 1) {

        if (series[i - 1] < series[i] && series[i] >= series[i + 1]) {

            // We have reached a new local maximum. Start recording something interesting.
            data[i] = 1.0

        } else if (series[i] == series[i + 1] && data[i - 1] > 0) {

            // Continuing with the previous local maximum.
            data[i] = 1.0

        }

    }

    return data
}

fun smoothSeriesMean(series: DoubleArray, smoothingWindow: Int) =
        smoothSeries(series, smoothingWindow) { it.mean }

fun smoothSeriesMedian(series: DoubleArray, smoothingWindow: Int) =
        smoothSeries(series, smoothingWindow) { it.getPercentile(0.5) }

/**
 * Same as [seriesFromFFTWindows], but applies a [smoothingFun] over a certain number of values
 * either side defined by the [smoothingWindow] (where the [smoothingWindow] is the number of values
 * to the left and right including the sample getting smoothed). Therefore, the [smoothingWindow]
 * must be odd (half the values on the left, half on the right, and a final position for the value
 * being smoothed) or else it will throw an exception.
 */
fun smoothSeries(series: DoubleArray, smoothingWindow: Int, smoothingFun: (stats: DescriptiveStatistics) -> Double): DoubleArray {

    if (smoothingWindow % 2 == 0) {
        throw IllegalArgumentException("Smoothing window must be an odd number.")
    }

    val eitherSide:Int = smoothingWindow / 2

    val smoothSeries = DoubleArray(series.size)

    val start = eitherSide
    val end = (series.size - eitherSide - 1)
    for (i in start..end) {
        val descriptiveStatistics = DescriptiveStatistics()
        series.slice(IntRange(i - eitherSide, i + eitherSide))
                .forEach { descriptiveStatistics.addValue(it) }

        smoothSeries[i] = smoothingFun(descriptiveStatistics)
    }

    return smoothSeries

}

/**
 * Maps from a [List] of [FFTWindow] values to a [DoubleArray], where each value in the new array
 * is the result of calling [stat] on the [FFTWindow] at the same position.
 */
fun seriesFromFFTWindows(windows: List<FFTWindow>, stat: (w: FFTWindow) -> Double): DoubleArray {
    val array = DoubleArray(windows.size)

    for (i in windows.indices) {
        array[i] = stat(windows[i])
    }

    return array
}

/**
 * From a one dimensional array of values, to a new (single dimensional) array twice the size, which
 * represents x/y coordinates to render a line graph of the series.
 *
 * The x axis goes from 0 -> series.size.
 * The y axis is normalised from 0 -> 1.
 */
fun renderSeries(series:DoubleArray, graphSize: Float): FloatArray {
    val array = FloatArray(series.size * 2)

    val normalisation = normaliseSeries(series)

    series.forEachIndexed { i, value ->
        if (i != 0) {
            array[i * 2] = ((value - normalisation.min) / normalisation.range * graphSize).toFloat()
            array[i * 2 + 1] = i.toFloat()
        }
    }

    return array
}

fun normaliseSeries(series: DoubleArray): Normalisation {
    var minVal = Double.MAX_VALUE
    var maxVal = Double.MIN_VALUE
    series.forEach {
        if (it < minVal) {
            minVal = it
        }
        if (it > maxVal) {
            maxVal = it
        }
    }

    return Normalisation(minVal, maxVal)
}

data class Normalisation(
        val min: Double,
        val max: Double
) {
    val range = max - min
}