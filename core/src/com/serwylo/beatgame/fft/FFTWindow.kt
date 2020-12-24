package com.serwylo.beatgame.fft

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.sqrt

data class FFTWindow(
        val windowIndex: Int,
        val values: List<FrequencyValue>
) {

    private val stats: DescriptiveStatistics = DescriptiveStatistics(values.size)

    init {
        values.forEach {
            stats.addValue(it.absValue)
        }
    }

    /**
     * https://maelfabien.github.io/machinelearning/Speech9/#2-energy
     */
    fun energy() = values.map { it.absValue * it.absValue }.sum()

    // https://maelfabien.github.io/machinelearning/Speech9/#1-statistical-features
    fun mean() = stats.mean
    fun stdDev() = stats.standardDeviation
    fun max() = stats.max
    fun min() = stats.min
    fun q1() = stats.getPercentile(0.25)
    fun median() = stats.getPercentile(0.5)
    fun q3() = stats.getPercentile(0.75)
    fun kurtosis() = stats.kurtosis
    fun skewness() = stats.skewness

    fun dominantFrequency() = values.maxBy { it.absValue }!!.frequency

    /**
     * https://maelfabien.github.io/machinelearning/Speech9/#3-root-mean-square-energy
     */
    fun rmse() = sqrt(values.map { it.absValue * it.absValue }.sum() / values.size)

}
