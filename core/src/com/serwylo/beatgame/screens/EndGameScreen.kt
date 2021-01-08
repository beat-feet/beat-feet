package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Globals

class EndGameScreen(private val game: BeatGame, private val score: Int, private val distancePercent: Float): MenuScreen() {

    override fun show() {

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                }

                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                game.showMenu()
                return true
            }

        }

    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = Globals.spriteBatch
        batch.projectionMatrix = camera.combined
        batch.begin()

        bigFont.draw(batch, "The End", 0f, 25f)
        smallFont.draw(batch, "${(distancePercent * 100).toInt()}% / ${score}", 0f, 0f)

        batch.end()

    }

}