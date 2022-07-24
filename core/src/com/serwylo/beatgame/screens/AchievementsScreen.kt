package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.allAchievements
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage

class AchievementsScreen(private val game: BeatFeetGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        setupStage()
    }

    private fun setupStage() {
        val achievements = loadAllAchievements()

        val styles = game.assets.getStyles()
        val sprites = game.assets.getSprites()
        val strings = game.assets.getStrings()

        val table = Table()
        table.padBottom(UI_SPACE * 2)
        table.row().align(Align.center).pad(UI_SPACE * 2)

        val headingGroup = makeHeading(strings["achievements.title"], sprites.star, styles, strings) {
            game.showMenu()
        }

        table.add(headingGroup).colspan(2)

        Levels.all.forEach { level ->

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

        stage.addActor(
            ScrollPane(table).apply {
                setFillParent(true)
                setScrollingDisabled(true, false)
                setupOverscroll(UI_SPACE, 30f, 200f)
            }
        )

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        stage.clear()
        setupStage()
    }

    private fun makeAchievementsTable(styles: Assets.Styles, strings: I18NBundle, achievements: List<Achievement>, level: Level): Actor {

        val achievementsTable = Table()

        allAchievements.forEachIndexed { i, achievement ->
            val isAchieved = achievements.any { it.level == level && it.type.id == achievement.id }
            val label = Label(strings["achievement.${achievement.id}"], styles.label.small)
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