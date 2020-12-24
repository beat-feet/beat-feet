package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.analysis.*
import com.serwylo.beatgame.entities.Player
import com.serwylo.beatgame.features.Feature
import com.serwylo.beatgame.features.World
import com.serwylo.beatgame.fft.FFTWindow
import com.serwylo.beatgame.fft.calculateMp3FFT
import kotlin.math.ln

class PlatformGameScreen(
        private val game: BeatGame,
        musicFile: FileHandle
) : ScreenAdapter() {

    private val music = Gdx.audio.newMusic(musicFile)

    private val camera = OrthographicCamera(20f, 10f)

    private val features: List<Feature>
    private val heightMap: Array<Vector2>
    private val world: World
    private val player: Player

    init {

        val spectogram = calculateMp3FFT(musicFile.read())
        val featureSeries = seriesFromFFTWindows(spectogram.windows) { it.median() }
        val smoothFeatureSeries = smoothSeriesMedian(featureSeries, 13)
        features = extractFeaturesFromSeries(smoothFeatureSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate)

        val heightMapSeries = seriesFromFFTWindows(spectogram.windows) { it: FFTWindow ->
            val freq = it.dominantFrequency()
            if (freq.toInt() == 0) 0.0 else ln(freq)
        }

        val smoothHeightMapSeries = smoothSeriesMean(heightMapSeries, 15)
        heightMap = extractHeightMapFromSeries(smoothHeightMapSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate, 3f)

        world = World.generate(heightMap, features, SCALE_X)

        player = Player { world.heightAtPosition(it) }
    }

    override fun show() {

        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE) {
                    player.performJump()
                    return true
                } else if (keycode == Input.Keys.ESCAPE) {
                    game.showMenu()
                }

                return false
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                player.performJump()
                return true
            }
        }


        camera.translate(5f, 2f, 0f)
        camera.update()

        music.play()
    }

    override fun hide() {
        music.stop()
        music.dispose()

        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        camera.translate(delta * SCALE_X, 0f)
        camera.update()

        player.update(delta)

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        world.render(camera, Rectangle(player.getPosition() - 5f, -2f, 20f, 10f))
        player.render(camera)
    }

    companion object {

        /**
         * To convert horizontal units from seconds -> metres. That sounds a bit odd, but this is a side
         * scrolling game where features appear at very specific time points, and the screen scrolls
         * at a consistent rate. Therefore it does kind-of-in-an-odd-way make sense to multiple a seconds
         * value to get a horizontal offset in metres.
         *
         * All of the level generation starts with music, which is measured in samples at a particular
         * sample rate.
         *
         * This is then converted into specific time points in seconds, so that regardless of the sample
         * rate of a particular song, all songs produce features of the same duration.
         *
         * The final step is to convert seconds into measurements on screen. This is used for that.
         */
        const val SCALE_X = 5f

    }

}