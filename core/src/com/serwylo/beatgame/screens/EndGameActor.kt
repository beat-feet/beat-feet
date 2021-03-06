package com.serwylo.beatgame.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.levels.HighScore
import com.serwylo.beatgame.levels.Score
import com.serwylo.beatgame.levels.achievements.AchievementType

class EndGameActor(
        private val game: BeatGame,
        existingHighScore: HighScore,
        score: Score,
        newAchievements: List<AchievementType>,
        onReplay: () -> Unit,
        onChangeLevel: () -> Unit,
        onMainMenu: () -> Unit
): VerticalGroup() {

    private var mediumFont = BitmapFont().apply { data.scale(-0.2f) }

    init {

        align(Align.center)
        columnAlign(Align.center)

        val replayButton = TextButton("Replay", game.assets.getSkin())
        replayButton.isDisabled = false
        replayButton.pad(SPACING * 1.75f)
        replayButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onReplay()
            }
        })

        val changeLevelButton = TextButton("Change level", game.assets.getSkin())
        changeLevelButton.isDisabled = false
        changeLevelButton.label.setFontScale(0.5f)
        changeLevelButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onChangeLevel()
            }
        })

        val mainMenuButton = TextButton("Main menu", game.assets.getSkin())
        mainMenuButton.isDisabled = false
        mainMenuButton.label.setFontScale(0.5f)
        mainMenuButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onMainMenu()
            }
        })

        val secondaryButtons = HorizontalGroup()
        secondaryButtons.addActor(changeLevelButton)
        secondaryButtons.addActor(mainMenuButton)

        addActor(replayButton)
        addActor(secondaryButtons)

        val distanceRecord = (score.distancePercent * 100).toInt() > (existingHighScore.distancePercent * 100).toInt()
        val scoreRecord = score.getPoints() > existingHighScore.points

        if (distanceRecord || scoreRecord) {
            val recordLabelStyle = Label.LabelStyle()
            recordLabelStyle.font = mediumFont

            val horizontalGroup = HorizontalGroup()
            horizontalGroup.space(SPACING)
            horizontalGroup.padTop(SPACING)

            horizontalGroup.addActor(Label("New Record!", recordLabelStyle))

            if (distanceRecord) {
                horizontalGroup.addActor(Image(game.assets.getSprites().right_sign))
                horizontalGroup.addActor(Label("${(score.distancePercent * 100).toInt()}%", recordLabelStyle))
            }

            if (scoreRecord) {
                horizontalGroup.addActor(Image(game.assets.getSprites().score))
                horizontalGroup.addActor(Label("${score.getPoints()}", recordLabelStyle))
            }

            addActor(horizontalGroup)
        }

        addActor(makeAchievementsTable(newAchievements))

    }

    private fun makeAchievementsTable(achievements: List<AchievementType>): Table {

        val achievementsTable = Table()
        val achievementLabelStyle = Label.LabelStyle(mediumFont, Color.WHITE)
        val icon = game.assets.getSprites().star

        achievements.forEachIndexed { i, it ->
            val label = Label(it.label, achievementLabelStyle)

            val group = HorizontalGroup()
            group.addActor(Image(icon))
            group.addActor(label)

            if (i > 0 && i % 3 == 0) {
                achievementsTable.row()
            }

            achievementsTable.add(group).space(5f)
        }

        return achievementsTable

    }

    companion object {

        private const val SPACING = 10f

    }

}
