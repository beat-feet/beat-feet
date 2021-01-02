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
        rmse: Double
): FFTWindow(windowIndex, energy, mean, stdDev, min, max, q1, median, q3, kurtosis, skewness, dominantFrequency, rmse) {

    companion object {
        fun create(windowIndex: Int, values: List<FrequencyValue>): FFTWindowWithValues {

            val stats = DescriptiveStatistics(values.size)

            values.forEach {
                stats.addValue(it.absValue)
            }

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
                    dominantFrequency = values.maxBy { it.absValue }!!.frequency,

                    // https://maelfabien.github.io/machinelearning/Speech9/#3-root-mean-square-energy
                    rmse = sqrt(values.map { it.absValue * it.absValue }.sum() / values.size)
            )

        }
    }

}
