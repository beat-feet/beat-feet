package com.serwylo.beatgame.screens

import com.badlogic.gdx.files.FileHandle
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.loadWorldFromMp3

class LoadingScreen(
        private val game: BeatGame,
        private val musicFile: FileHandle,
        private val songName: String
) : InfoScreen(
        "Loading...",
        songName
) {

    override fun show() {
        super.show()
        startLoading()
    }

    private fun startLoading() {
        Thread {

            val world = loadWorldFromMp3(musicFile)
            game.startGame(world)

        }.start()
    }

}