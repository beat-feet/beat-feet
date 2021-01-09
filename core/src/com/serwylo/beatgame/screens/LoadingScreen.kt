package com.serwylo.beatgame.screens

import com.badlogic.gdx.files.FileHandle
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Score
import com.serwylo.beatgame.audio.loadWorldFromMp3

class LoadingScreen(
        private val game: BeatGame,
        private val musicFile: FileHandle,
        private val songName: String
) : InfoScreen(
        songName,
        "Best: ${(Score.load(musicFile.name()).distancePercent * 100).toInt()}%\n\nLoading..."
) {

    override fun show() {
        super.show()
        startLoading()
    }

    private fun startLoading() {
        Thread {

            val startTime = System.currentTimeMillis()
            val world = loadWorldFromMp3(musicFile)
            val loadTime = System.currentTimeMillis() - startTime
            if (loadTime < MIN_LOAD_TIME) {
                Thread.sleep(MIN_LOAD_TIME - loadTime)
            }
            game.startGame(world)

        }.start()
    }

    companion object {

        private const val MIN_LOAD_TIME = 1000

    }

}