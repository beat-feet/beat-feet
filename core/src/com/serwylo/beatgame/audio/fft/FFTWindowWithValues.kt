package com.serwylo.beatgame.audio.fft

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.sqrt

class FFTWindowWithValues(
        windowIndex: Int,
        val values: List<FrequencyValue>,
        energy: Double,
        mean: Double,
        stdDev: Double,
        min: Double,
        max: Double,
        q1: Double,
        median: Double,
        q3: Double,
        kurtosis: Double,
        skewness: Double,
        dominantFrequency: Double,
        rmse: Double,
        meanFirstQuarter: Double,
        meanSecondQuarter: Double,
        meanThirdQuarter: Double,
        meanFourthQuarter: Double
): FFTWindow(windowIndex, energy, mean, stdDev, min, max, q1, median, q3, kurtosis, skewness, dominantFrequency, rmse, meanFirstQuarter, meanSecondQuarter, meanThirdQuarter) {

    fun toWindow() = FFTWindow(windowIndex, energy, mean, stdDev, min, max, q1, median, q3, kurtosis, skewness, dominantFrequency, rmse, meanFirst, meanSecond, meanThird)

    companion object {
        fun create(windowIndex: Int, values: List<FrequencyValue>): FFTWindowWithValues {

            val stats = DescriptiveStatistics(values.size)

            values.forEach {
                stats.addValue(it.absValue)
            }

            val quarterSize = values.size / 4

            return FFTWindowWithValues(
                    windowIndex,
                    values,

                    // https://maelfabien.github.io/machinelearning/Speech9/#2-energy
                    energy = values.map { it.absValue * it.absValue }.sum(),

                    // https://maelfabien.github.io/machinelearning/Speech9/#1-statistical-features
                    mean = stats.mean,
                    stdDev = stats.standardDeviation,
                    max = stats.max,
                    min = stats.min,
                    q1 = stats.getPercentile(0.25),
                    median = stats.getPercentile(0.5),
                    q3 = stats.getPercentile(0.75),
                    kurtosis = stats.kurtosis,
                    skewness = stats.skewness,
                    dominantFrequency = values.maxByOrNull { it.absValue }!!.frequency,

                    // https://maelfabien.github.io/machinelearning/Speech9/#3-root-mean-square-energy
                    rmse = sqrt(values.map { it.absValue * it.absValue }.sum() / values.size),

                    meanFirstQuarter = values.slice(IntRange(0, quarterSize)).sumByDouble { it.absValue } / quarterSize,
                    meanSecondQuarter = values.slice(IntRange(quarterSize, quarterSize * 2)).sumByDouble { it.absValue } / quarterSize,
                    meanThirdQuarter = values.slice(IntRange(quarterSize * 2, quarterSize * 3)).sumByDouble { it.absValue } / quarterSize,
                    meanFourthQuarter = values.slice(IntRange(quarterSize * 3, values.size - 1)).sumByDouble { it.absValue } / quarterSize
            )

        }
    }

}
