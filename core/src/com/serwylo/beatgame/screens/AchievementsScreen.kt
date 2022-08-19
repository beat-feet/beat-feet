package com.serwylo.beatgame.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.TheOriginalWorld
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.allAchievements
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.ui.UI_SPACE

class AchievementsScreen(private val game: BeatFeetGame): WorldSelectorScreen(
    game,
    "achievements.title",
    game.assets.getSprites().star,
    TheOriginalWorld,
) {

    override fun makeBody(world: World) = Table().also { table ->

        val achievements = loadAllAchievements()

        table.pad(UI_SPACE)

        world.getLevels().forEach { level ->

            val isLocked = level.getUnlockRequirements().isLocked(achievements)
            val textColor = if (isLocked) Color.GRAY else Color.WHITE
            val labelString = if (isLocked && !level.getUnlockRequirements().isAlmostUnlocked(achievements)) "???" else level.getLabel(strings)

            val levelLabel = Label(labelString, styles.label.medium).apply {
                setAlignment(Align.right)
                wrap = true
                color = textColor
            }

            table.row().apply {
                spaceBottom(UI_SPACE * 3f)
            }

            table.add(levelLabel).apply {
                align(Align.top)
                width(UI_SPACE * 30)
                padRight(UI_SPACE * 2)
                spaceRight(UI_SPACE)
            }

            val achievementsWidget: Actor = if (!isLocked) {
                makeAchievementsTable(styles, strings, achievements, level)
            } else {
                val toUnlockLabel = Label(strings.format("achievements.unlock-requirements", level.getUnlockRequirements().describeOutstandingRequirements(strings, achievements)), styles.label.small)
                toUnlockLabel.color = Color.GRAY
                toUnlockLabel
            }

            table.add(achievementsWidget).apply {
                align(Align.left or Align.top)
            }
        }
    }

    private fun makeAchievementsTable(styles: Assets.Styles, strings: I18NBundle, achievements: List<Achievement>, level: Level): Actor {

        val achievementsTable = Table()

        allAchievements.forEachIndexed { i, achievement ->
            val isAchieved = achievements.any { it.levelId == level.getId() && it.type.id == achievement.id }
            val label = Label(strings["achievement.${achievement.id}"], styles.label.small)
            label.color = if (isAchieved) Color.WHITE else Color.GRAY

            if (i > 0 && i % ACHIEVEMENTS_PER_ROW == 0) {
                achievementsTable.row()
            }

            achievementsTable.add(label).align(Align.left).spaceRight(UI_SPACE * 2).spaceBottom(UI_SPACE / 2)
        }

        return achievementsTable

    }

    companion object {

        private const val ACHIEVEMENTS_PER_ROW = 4

    }

}