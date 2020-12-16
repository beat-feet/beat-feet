package com.serwylo.beatgame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType

class BeatGame : ApplicationAdapter() {
    private var frequencies: List<Double> = listOf()
    private lateinit var music: Music
    private lateinit var doubles: DoubleArray

    private var currentSampleCounter: Float = 0f
    private var totalSampleCounter: Float = 0f
    private val sampleRate = 44100
    private val sampleTimeInSeconds = 0.1f

    override fun create() {
        val musicFile = Gdx.files.internal("Man_Bites_Dog.mp3")
        music = Gdx.audio.newMusic(musicFile)
        doubles = musicFile
                .readBytes()
                .map { it.toDouble() }
                .toDoubleArray()

        music.play()
    }

    override fun render() {

        currentSampleCounter += Gdx.graphics.deltaTime
        if (currentSampleCounter > sampleTimeInSeconds) {

            val startTime = System.currentTimeMillis()
            val startSample = (totalSampleCounter * sampleRate).toInt()
            val endSample = ((totalSampleCounter + currentSampleCounter) * sampleRate).toInt()
            val windowSize = endSample - startSample

            var minPowerOfTwo = 2
            while (minPowerOfTwo < windowSize) {
                minPowerOfTwo *= 2
            }

            val samples = ArrayList<Double>(minPowerOfTwo)
            samples.addAll(doubles.slice(IntRange(startSample, endSample)))
            while (samples.size < minPowerOfTwo) {
                samples.add(0.0)
            }

            val fft = FastFourierTransformer(DftNormalization.STANDARD)
            frequencies = fft.transform(samples.toDoubleArray(), TransformType.FORWARD)
                    .slice(IntRange(0, samples.size / 2))
                    .map { it.abs() * 1 / samples.size }

            val duration = System.currentTimeMillis() - startTime
            println("Calculating FFT, took $duration}ms. Music is at: ${music.position}")

            currentSampleCounter = 0f

        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val startTime = System.currentTimeMillis()

        val line = ShapeRenderer(2)
        line.color = Color.YELLOW
        frequencies.forEachIndexed { i, value ->
            line.begin(ShapeRenderer.ShapeType.Line)
            line.line(100f + i * 0.08f, 100f, 100f + i * 0.08f, 100f + value.toFloat() * 100)
            line.end()
        }

        val duration = System.currentTimeMillis() - startTime
        println("Printing, took $duration}ms")

    }

    override fun dispose() {
        music.dispose()
    }
}