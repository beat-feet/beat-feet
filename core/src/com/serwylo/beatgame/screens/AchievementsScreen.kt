package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.allAchievements
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeIcon
import com.serwylo.beatgame.ui.makeStage

class AchievementsScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val achievements = loadAllAchievements()

        val styles = game.assets.getStyles()
        val sprites = game.assets.getSprites()

        val table = Table()
        table.padBottom(UI_SPACE * 2)
        table.row().align(Align.center).pad(UI_SPACE * 2)

        val headingGroup = HorizontalGroup()

        headingGroup.addActor(makeIcon(sprites.star))
        headingGroup.addActor(Label("Achievements", styles.label.large))

        table.add(headingGroup).colspan(2)

        Levels.all.forEach { level ->

            val isLocked = level.unlockRequirements.isLocked(achievements)
            val textColor = if (isLocked) Color.GRAY else Color.WHITE
            val labelString = if (isLocked && !level.unlockRequirements.isAlmostUnlocked(achievements)) "???" else level.label

            val levelLabel = Label(labelString, styles.label.medium)
            levelLabel.color = textColor

            table.row()
                    .spaceBottom(UI_SPACE * 1.5f)

            table.add(levelLabel)
                    .align(Align.right)
                    .spaceRight(UI_SPACE * 2)

            val achievementsWidget: Actor = if (!isLocked) {
                makeAchievementsTable(styles, achievements, level)
            } else {
                val toUnlockLabel = Label(level.unlockRequirements.describeOutstandingRequirements(achievements) + " to unlock", styles.label.small)
                toUnlockLabel.color = Color.GRAY
                toUnlockLabel
            }

            table.add(achievementsWidget)
                    .align(Align.left)

        }

        val scrollPane = ScrollPane(table)
        scrollPane.setFillParent(true)
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setupOverscroll(UI_SPACE, 30f, 200f)
        stage.addActor(scrollPane)

    }

    private fun makeAchievementsTable(styles: Assets.Styles, achievements: List<Achievement>, level: Level): Actor {

        val achievementsTable = Table()

        allAchievements.forEachIndexed { i, achievement ->
            val isAchieved = achievements.any { it.level == level && it.type.id == achievement.id }
            val label = Label(achievement.label, styles.label.small)
            label.color = if (isAchieved) Color.WHITE else Color.GRAY

            if (i > 0 && i % ACHIEVEMENTS_PER_ROW == 0) {
                achievementsTable.row()
            }

            achievementsTable.add(label).align(Align.left).spaceRight(UI_SPACE * 2).spaceBottom(UI_SPACE / 2)
        }

        return achievementsTable

    }

    override fun show() {
        super.show()

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = InputMultiplexer(stage, object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                }

                return false
            }

        })

    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

    }

    companion object {

        private const val ACHIEVEMENTS_PER_ROW = 4

    }

}