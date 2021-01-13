package com.serwylo.beatgame.audio.playground

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.audio.features.Feature
import com.serwylo.beatgame.audio.fft.FFTResult
import com.serwylo.beatgame.audio.fft.FFTResultWithValues
import com.serwylo.beatgame.audio.fft.FFTWindow
import java.lang.Math.max
import kotlin.math.ln

/**
 * A place to experiment with DSP and audio analysis, to visually inspect what sort of feedback
 * can be gleamed.
 */
class AudioAnalysisPlaygroundGame : ApplicationAdapter() {
    private lateinit var featureSwatch: List<Color>
    private lateinit var features: List<Feature>
    private lateinit var font: BitmapFont
    private lateinit var camera: Camera
    private lateinit var sprite: Sprite
    private lateinit var batch: SpriteBatch
    private lateinit var texture: Texture
    private lateinit var spectogram: FFTResultWithValues
    private lateinit var spectogramImage: Pixmap
    private lateinit var music: Music
    private val statsWidth = 25f
    private val statsOffset = 130f
    private val series = mutableMapOf<String, DoubleArray>()
    private val seriesVertices = mutableMapOf<String, FloatArray>()

    override fun create() {
        // val musicFile = Gdx.files.internal("sine_1000Hz_plus_500Hz.mp3")
        // val musicFile = Gdx.files.internal("vivaldi.mp3")
        val musicFile = Gdx.files.internal("the_haunted_mansion_the_courtyard.mp3")
        music = Gdx.audio.newMusic(musicFile)
        spectogram = com.serwylo.beatgame.audio.fft.calculateMp3FFTWithValues(musicFile.read())
        spectogramImage = com.serwylo.beatgame.audio.fft.renderSpectogram(spectogram)
        texture = Texture(spectogramImage.width, spectogramImage.height, Pixmap.Format.RGB888)
        sprite = Sprite(texture)
        featureSwatch = listOf(
                Color.RED,
                Color.CYAN,
                Color.BLUE,
                Color.YELLOW,
                Color.PURPLE,
                Color.PINK,
                Color.ORANGE,
                Color.GREEN,
                Color.MAGENTA,
                Color.SKY
        )

        font = BitmapFont().apply {
            data.scale(-0.6f)
        }
        batch = SpriteBatch(1)
        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.translate((Gdx.graphics.width / 2).toFloat(), (Gdx.graphics.height / 4).toFloat(), 0f)

        // Loud (energy) is good, indicates things are happening.
        series["energy"] = seriesFromFFTWindows(spectogram.windows) { it.energy }
        series["energyU3"] = smoothSeriesMean(series["energy"]!!, 3)
        series["energy3"] = smoothSeriesMedian(series["energy"]!!, 3)
        series["energy13"] = smoothSeriesMedian(series["energy"]!!, 13)

        // When it is loud, is it high or low pitched? Loud drums seem to go down, whereas lout
        // other instruments go higher (as does voice)
        val domFreq = { it: FFTWindow ->
            val freq = it.dominantFrequency
            if (freq.toInt() == 0) 0.0 else ln(freq)
        }

        series["domFreq"] = seriesFromFFTWindows(spectogram.windows, domFreq)
        series["domFreqU3"] = smoothSeriesMean(series["domFreq"]!!, 3)
        series["domFreq3"] = smoothSeriesMedian(series["domFreq"]!!, 3)
        series["domFreq13"] = smoothSeriesMedian(series["domFreq"]!!, 13)

        // When it is loud, is it loud across a bunch of different frequencies? If there is a small
        // standard deviation then it is really just a loud noise at one frequency most likely.
        series["stdDev"] = seriesFromFFTWindows(spectogram.windows) { it.stdDev }
        series["rmse"] = seriesFromFFTWindows(spectogram.windows) { it.rmse }
        series["min"] = seriesFromFFTWindows(spectogram.windows) { it.min }
        series["median"] = seriesFromFFTWindows(spectogram.windows) { it.median }
        series["max"] = seriesFromFFTWindows(spectogram.windows) { it.max }
        series["kurtosis"] = seriesFromFFTWindows(spectogram.windows) { it.kurtosis }
        series["skewness"] = seriesFromFFTWindows(spectogram.windows) { it.skewness }

        /* No intuition for these yet after observing for some time.
        */

        val toExtractFeatures = setOf("energy13", "domFreq13")
        series.keys
                .filter { toExtractFeatures.contains(it) }
                .toSet()
                .forEach { series["$it*"] = analyseSeries(series[it]!!) }

        features = extractFeaturesFromSeries(series["energy13"]!!, spectogram.windowSize, spectogram.mp3Data.sampleRate)

        series.onEach {
            seriesVertices[it.key] = renderSeries(it.value, statsWidth)
        }

        music.play()
        music.volume = 1f
    }

    override fun render() {
        texture.draw(spectogramImage, 0, 0)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.translate(0f, Gdx.graphics.deltaTime * spectogram.mp3Data.sampleRate / spectogram.windowSize, 0f)
        camera.update()

        batch.projectionMatrix = camera.combined
        batch.begin()
        sprite.draw(batch)
        batch.end()

        val time = ShapeRenderer(2)
        time.color = Color.GREEN
        time.projectionMatrix = camera.combined
        time.begin(ShapeRenderer.ShapeType.Line)
        val musicYPosition = music.position * spectogram.mp3Data.sampleRate / spectogram.windowSize
        time.line(0f, musicYPosition, Gdx.graphics.width.toFloat(), musicYPosition)
        time.end()

        batch.projectionMatrix.translate(statsOffset, camera.position.y - Gdx.graphics.height / 5, 0f)
        series.onEach { entry ->
            batch.begin()
            font.draw(batch, entry.key, 0f, 0f)
            batch.end()
            batch.projectionMatrix.translate(statsWidth + statsWidth / 5, 0f, 0f)
        }


        val beforeCameraY = camera.position.y.toInt() - 150
        val statsStartY = max(0, if (beforeCameraY % 2 == 0) beforeCameraY else beforeCameraY + 1)
        val statsCount = 1000
        val stats = ShapeRenderer(spectogram.windows.size + 10)
        stats.projectionMatrix = camera.combined
        stats.begin(ShapeRenderer.ShapeType.Line)
        stats.translate(statsOffset, 0f, 0f)
        seriesVertices.onEach { entry ->
            stats.color = Color.GRAY
            stats.line(0f, 0f, 0f, spectogramImage.height.toFloat())
            stats.color = Color.WHITE
            stats.polyline(entry.value, statsStartY, statsStartY + statsCount)
            stats.translate((statsWidth + statsWidth / 5).toFloat(), 0f, 0f)
        }
        stats.end()

        /*val blobs = ShapeRenderer(1000)
        blobs.begin(ShapeRenderer.ShapeType.Filled)

        features
                .filter { it.startTimeInSeconds < music.position && it.startTimeInSeconds + it.durationInSeconds * 10 > music.position }
                .forEach {
                    blobs.color = featureSwatch[it.hashCode() % featureSwatch.size]
                    blobs.circle(
                            (it.hashCode() % Gdx.graphics.width).toFloat(),
                            (it.hashCode() % Gdx.graphics.height).toFloat(),
                            (music.position - it.startTimeInSeconds) * 50 + (50 * it.strength))
                }

        blobs.end()*/

    }

    override fun dispose() {
        music.dispose()
    }
}
