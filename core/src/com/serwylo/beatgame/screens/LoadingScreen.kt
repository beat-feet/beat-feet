package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.audio.loadWorldFromMp3

class LoadingScreen(private val game: BeatGame, private val musicFile: FileHandle, private val songName: String) : MenuScreen() {

    override fun show() {
        startLoading()
    }

    private fun startLoading() {
        Thread {

            val world = loadWorldFromMp3(musicFile)
            game.startGame(world)

        }.start()
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = Globals.spriteBatch
        batch.projectionMatrix = camera.combined
        batch.begin()

        bigFont.draw(batch, songName, 0f, 25f)
        smallFont.draw(batch, "Loading...", 0f, 0f)

        batch.end()

    }

}