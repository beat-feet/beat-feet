package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Score
import com.serwylo.beatgame.audio.loadWorldFromMp3

class LoadingScreen(
        private val game: BeatGame,
        private val musicFile: FileHandle,
        private val songName: String
) : InfoScreen(
        songName,
        "Loading..."
) {

    private val atlas: TextureAtlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))

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

        val topScore = Score.load(musicFile.name())

        val labelStyle = Label.LabelStyle()
        labelStyle.font = mediumFont

        val verticalGroup = VerticalGroup()
        verticalGroup.space(SPACING)

        val bestLabel = Label("Best", labelStyle)
        verticalGroup.addActor(bestLabel)

        val horizontalGroup = HorizontalGroup()
        horizontalGroup.space(SPACING)

        val distanceLabel = Label("${(topScore.distancePercent * 100).toInt()}%", labelStyle)
        val scoreLabel = Label("${topScore.score}", labelStyle)

        val distanceImage = Image(atlas.findRegion("right_sign"))
        val scoreImage = Image(atlas.findRegion("score"))

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