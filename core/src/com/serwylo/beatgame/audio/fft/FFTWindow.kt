package com.serwylo.beatgame.audio.fft

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.sqrt

open class FFTWindow(
        val windowIndex: Int,
        val energy: Double,
        val mean: Double,
        val stdDev: Double,
        val min: Double,
        val max: Double,
        val q1: Double,
        val median: Double,
        val q3: Double,
        val kurtosis: Double,
        val skewness: Double,
        val dominantFrequency: Double,
        val rmse: Double,
        val meanFirst: Double,
        val meanSecond: Double,
        val meanThird: Double
) {

    companion object {
        fun create(windowIndex: Int, values: List<FrequencyValue>): FFTWindow {

            val stats = DescriptiveStatistics(values.size)

            values.forEach {
                stats.addValue(it.absValue)
            }

            val thirdSize = values.size / 3

            return FFTWindow(
                    windowIndex,

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

                    meanFirst = values.slice(IntRange(0, thirdSize)).sumByDouble { it.absValue } / thirdSize,
                    meanSecond = values.slice(IntRange(thirdSize, thirdSize * 2)).sumByDouble { it.absValue } / thirdSize,
                    meanThird = values.slice(IntRange(thirdSize * 2, thirdSize * 3)).sumByDouble { it.absValue } / thirdSize
            )

        }
    }

}
