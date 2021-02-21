package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.allAchievements
import com.serwylo.beatgame.levels.achievements.loadAllAchievements

class AchievementsScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = Stage(ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))

    private var bigFont = BitmapFont().apply { data.scale(0.5f) }
    private var mediumFont = BitmapFont().apply { data.scale(-0.2f) }
    private var smallFont = BitmapFont().apply { data.scale(-0.5f) }

    init {
        val achievements = loadAllAchievements()

        val bigLabelStyle = Label.LabelStyle(bigFont, Color.WHITE)
        val mediumLabelStyle = Label.LabelStyle(mediumFont, Color.WHITE)
        val smallLabelStyle = Label.LabelStyle(smallFont, Color.WHITE)

        val table = Table()
        table.row().align(Align.center).pad(20f)

        val headingGroup = HorizontalGroup()

        val headingIcon = Image(game.assets.getSprites().star)
        val headingLabel = Label("Achievements", bigLabelStyle)

        headingGroup.addActor(headingIcon)
        headingGroup.addActor(headingLabel)

        table.add(headingGroup).colspan(2)

        Levels.all.forEach { level ->

            val isLocked = level.unlockRequirements.isLocked(achievements)
            val textColor = if (isLocked) Color.GRAY else Color.WHITE
            val labelString = if (isLocked && !level.unlockRequirements.isAlmostUnlocked(achievements)) "???" else level.label

            val levelLabel = Label(labelString, mediumLabelStyle)
            levelLabel.color = textColor

            table.row()
                    .spaceBottom(15f)

            table.add(levelLabel)
                    .align(Align.right)
                    .spaceRight(10f)

            val achievementsWidget: Actor = if (!isLocked) {
                makeAchievementsTable(achievements, level)
            } else {
                val toUnlockLabel = Label(level.unlockRequirements.describeOutstandingRequirements(achievements) + " to unlock", smallLabelStyle)
                toUnlockLabel.color = Color.GRAY
                toUnlockLabel
            }

            table.add(achievementsWidget)
                    .align(Align.left)

        }

        val scrollPane = ScrollPane(table)
        scrollPane.setFillParent(true)
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setupOverscroll(10f, 30f, 200f)
        stage.addActor(scrollPane)

    }

    private fun makeAchievementsTable(achievements: List<Achievement>, level: Level): Actor {

        val achievementsTable = Table()
        val achievementLabelStyle = Label.LabelStyle(smallFont, Color.WHITE)

        allAchievements.forEachIndexed { i, achievement ->
            val isAchieved = achievements.any { it.level == level && it.type.id == achievement.id }
            val label = Label(achievement.label, achievementLabelStyle)
            label.color = if (isAchieved) Color.WHITE else Color.GRAY

            if (i > 0 && i % ACHIEVEMENTS_PER_ROW == 0) {
                achievementsTable.row()
            }

            achievementsTable.add(label).space(5f)
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

        @JvmStatic
        private val VIEWPORT_WIDTH = 400f

        @JvmStatic
        private val VIEWPORT_HEIGHT = 200f

        private const val ACHIEVEMENTS_PER_ROW = 4

    }

}