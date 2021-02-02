package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.levels.HighScore
import com.serwylo.beatgame.levels.Score
import com.serwylo.beatgame.levels.achievements.AchievementType
import com.serwylo.beatgame.levels.achievements.achievementsForLevel
import com.serwylo.beatgame.levels.achievements.allAchievements
import com.serwylo.beatgame.levels.achievements.saveAchievements
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.levels.saveHighScore

class EndGameScreen(
        private val game: BeatGame,
        world: World,
        private val score: Score
): InfoScreen("The End") {

    private val atlas: TextureAtlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))
    private val existingAchievements = achievementsForLevel(world.level())
    private val achievements: List<AchievementType>
    private val existingHighScore: HighScore = loadHighScore(world.level())
    private val highScore: HighScore = saveHighScore(world.level(), score)

    init {

        achievements = allAchievements.filter {
            it.isAchieved(score, highScore) && existingAchievements.all { existing -> existing.id != it.id }
        }

        saveAchievements(achievements, world.level())

    }

    override fun show() {
        super.show()

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
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
        if ((score.distancePercent * 100).toInt() > (existingHighScore.distancePercent * 100).toInt()) {
            distanceLabelStyle.fontColor = Color.GREEN
            record = true
        }

        if (score.getPoints() > existingHighScore.points) {
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

        val achievementsTable = Table()
        val achievementLabelStyle = Label.LabelStyle(mediumFont, Color.WHITE)
        val achievementIcon = atlas.findRegion("star")

        achievements.forEachIndexed { i, it ->
            val label = Label(it.label, achievementLabelStyle)
            label.color =  Color.WHITE

            val icon = Image(achievementIcon)

            val group = HorizontalGroup()
            group.addActor(icon)
            group.addActor(label)

            if (i > 0 && i % 3 == 0) {
                achievementsTable.row()
            }

            achievementsTable.add(group).space(SPACING / 2)
        }

        verticalGroup.addActor(achievementsTable)

        return verticalGroup

    }

    companion object {

        private const val SPACING = 10f

    }

}