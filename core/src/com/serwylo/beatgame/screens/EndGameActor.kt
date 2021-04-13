package com.serwylo.beatgame.screens

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.HighScore
import com.serwylo.beatgame.levels.Score
import com.serwylo.beatgame.levels.achievements.AchievementType
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeButton
import com.serwylo.beatgame.ui.makeIcon
import com.serwylo.beatgame.ui.makeLargeButton

class EndGameActor(
    private val game: BeatFeetGame,
    existingHighScore: HighScore,
    score: Score,
    newAchievements: List<AchievementType>,
    onReplay: () -> Unit,
    onChangeLevel: () -> Unit,
    onMainMenu: () -> Unit
): VerticalGroup() {

    private val styles = game.assets.getStyles()
    private val strings = game.assets.getStrings()

    init {

        align(Align.center)
        columnAlign(Align.center)
        space(UI_SPACE)

        val playAgainButton = makeLargeButton(strings["btn.play-again"], styles) { onReplay() }
        val changeLevelButton = makeButton(strings["btn.change-level"], styles) { onChangeLevel() }
        val mainMenuButton = makeButton(strings["btn.main-menu"], styles) { onMainMenu() }

        val secondaryButtons = HorizontalGroup()
        secondaryButtons.addActor(changeLevelButton)
        secondaryButtons.addActor(mainMenuButton)

        addActor(playAgainButton)
        addActor(secondaryButtons)

        val distanceRecord = (score.distancePercent * 100).toInt() > (existingHighScore.distancePercent * 100).toInt()
        val scoreRecord = score.getPoints() > existingHighScore.points

        if (distanceRecord || scoreRecord) {
            val horizontalGroup = HorizontalGroup()
            horizontalGroup.space(SPACING)
            horizontalGroup.padTop(SPACING)

            horizontalGroup.addActor(Label(strings["new-record"], styles.label.medium))

            if (distanceRecord) {
                horizontalGroup.addActor(makeIcon(game.assets.getSprites().right_sign))
                horizontalGroup.addActor(Label("${(score.distancePercent * 100).toInt()}%", styles.label.medium))
            }

            if (scoreRecord) {
                horizontalGroup.addActor(makeIcon(game.assets.getSprites().score))
                horizontalGroup.addActor(Label("${score.getPoints()}", styles.label.medium))
            }

            addActor(horizontalGroup)
        }

        addActor(makeAchievementsTable(newAchievements))

    }

    private fun makeAchievementsTable(achievements: List<AchievementType>): Table {

        val achievementsTable = Table()
        val icon = game.assets.getSprites().star

        achievements.forEachIndexed { i, it ->
            val label = Label(strings["achievement.${it.id}"], styles.label.medium)

            val group = HorizontalGroup()
            group.addActor(makeIcon(icon))
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
