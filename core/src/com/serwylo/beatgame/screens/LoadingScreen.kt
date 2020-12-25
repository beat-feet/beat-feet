package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.analysis.*
import com.serwylo.beatgame.features.World
import com.serwylo.beatgame.fft.FFTWindow
import com.serwylo.beatgame.fft.calculateMp3FFT
import kotlin.math.ln

class LoadingScreen(private val game: BeatGame, private val musicFile: FileHandle, private val songName: String) : ScreenAdapter() {

    private val camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
    private val spriteBatch = SpriteBatch()

    private var bigFont = BitmapFont().apply { data.scale(0.5f) }
    private var smallFont = BitmapFont().apply { data.scale(-0.5f) }

    override fun show() {
        startLoading()
    }

    private fun startLoading() {
        Thread {

            val spectogram = calculateMp3FFT(musicFile.read())
            val featureSeries = seriesFromFFTWindows(spectogram.windows) { it.median() }
            val smoothFeatureSeries = smoothSeriesMedian(featureSeries, 13)
            val features = extractFeaturesFromSeries(smoothFeatureSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate)

            val heightMapSeries = seriesFromFFTWindows(spectogram.windows) { it: FFTWindow ->
                val freq = it.dominantFrequency()
                if (freq.toInt() == 0) 0.0 else ln(freq)
            }

            val smoothHeightMapSeries = smoothSeriesMean(heightMapSeries, 15)
            val heightMap = extractHeightMapFromSeries(smoothHeightMapSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate, 3f)

            val music = Gdx.audio.newMusic(musicFile)
            val world = World(music, heightMap, features, PlatformGameScreen.SCALE_X)

            game.startGame(world)

        }.start()
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()

        bigFont.draw(spriteBatch, songName, 0f, 25f)
        smallFont.draw(spriteBatch, "Loading...", 0f, 0f)

        spriteBatch.end()

    }

    companion object {
        private const val VIEWPORT_WIDTH = 400f
        private const val VIEWPORT_HEIGHT = 200f
    }
}