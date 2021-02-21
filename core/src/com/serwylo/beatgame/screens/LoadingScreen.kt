package com.serwylo.beatgame.screens

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.loadWorldFromMp3
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.loadHighScore

class LoadingScreen(
        private val game: BeatGame,
        private val musicFile: FileHandle,
        songName: String
) : InfoScreen(
        game,
        songName,
        "Loading...",
        game.assets.getSprites().logo
) {

    private val level: Level = Levels.bySong(musicFile.name())

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

    override fun otherActor(): WidgetGroup {

        val topScore = loadHighScore(level)

        val labelStyle = Label.LabelStyle()
        labelStyle.font = mediumFont

        val verticalGroup = VerticalGroup()
        verticalGroup.space(SPACING)

        val horizontalGroup = HorizontalGroup()
        horizontalGroup.space(SPACING)

        val bestLabel = Label("Best", labelStyle)
        val distanceLabel = Label("${(topScore.distancePercent * 100).toInt()}%", labelStyle)
        val scoreLabel = Label("${topScore.points}", labelStyle)

        val distanceImage = Image(game.assets.getSprites().right_sign)
        val scoreImage = Image(game.assets.getSprites().score)

        horizontalGroup.addActor(bestLabel)
        horizontalGroup.addActor(distanceImage)
        horizontalGroup.addActor(distanceLabel)
        horizontalGroup.addActor(scoreImage)
        horizontalGroup.addActor(scoreLabel)

        verticalGroup.addActor(horizontalGroup)

        return verticalGroup

    }

    companion object {

        private const val MIN_LOAD_TIME = 1000

        private const val SPACING = 10f

    }

}