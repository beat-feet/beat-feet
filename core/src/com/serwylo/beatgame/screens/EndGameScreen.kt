package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.HighScore
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.levels.Score

class EndGameScreen(
        private val game: BeatGame,
        private val world: World,
        private val score: Score
): InfoScreen("The End") {

    private val atlas: TextureAtlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))

    override fun show() {
        super.show()

        HighScore.save(world.musicFileName, score)

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

    override fun otherActor(): WidgetGroup {

        val distanceLabelStyle = Label.LabelStyle()
        distanceLabelStyle.font = mediumFont

        val scoreLabelStyle = Label.LabelStyle()
        scoreLabelStyle.font = mediumFont

        val verticalGroup = VerticalGroup()
        verticalGroup.space(SPACING)

        var record = false
        val highScore = HighScore.load(world.musicFileName)
        if ((score.distancePercent * 100).toInt() > (highScore.distancePercent * 100).toInt()) {
            distanceLabelStyle.fontColor = Color.GREEN
            record = true
        }

        if (score.getPoints() > highScore.points) {
            scoreLabelStyle.fontColor = Color.GREEN
            record = true
        }

        if (record) {
            val recordLabelStyle = Label.LabelStyle()
            recordLabelStyle.font = mediumFont

            val recordLabel = Label("New Record!", recordLabelStyle)
            verticalGroup.addActor(recordLabel)
        }

        val horizontalGroup = HorizontalGroup()
        horizontalGroup.space(SPACING)

        val distanceLabel = Label("${(score.distancePercent * 100).toInt()}%", distanceLabelStyle)
        val scoreLabel = Label("${score.getPoints()}", scoreLabelStyle)

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

        private const val SPACING = 10f

    }

}