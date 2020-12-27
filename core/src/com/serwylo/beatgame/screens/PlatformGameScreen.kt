package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.analysis.*
import com.serwylo.beatgame.entities.Player
import com.serwylo.beatgame.audio.features.World

class PlatformGameScreen(
        private val game: BeatGame,
        private val world: World
) : ScreenAdapter() {

    private val camera = OrthographicCamera(20f, 10f)
    private val player: Player

    init {

        player = Player { world.heightAtPosition(it) }
    }

    override fun show() {

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE) {
                    player.performJump()
                    return true
                } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
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

        world.music.play()
    }

    override fun hide() {
        world.dispose()

        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
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