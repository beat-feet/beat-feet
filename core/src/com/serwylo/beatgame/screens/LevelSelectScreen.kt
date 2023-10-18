package com.serwylo.beatgame.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.CustomLevel
import com.serwylo.beatgame.levels.CustomWorld
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.levels.onAddNewLevel
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeIcon


class LevelSelectScreen(private val game: BeatFeetGame, initialWorld: World): WorldSelectorScreen(
    game,
    "level-select.title",
    game.assets.getSprites().logo,
    initialWorld,
) {

    private lateinit var levelButtons: Map<Level, WidgetGroup>

    override fun onLongPress(screenX: Float, screenY: Float): Boolean {
        val level = levelButtons.entries.find { (_, button) ->
            val local = button.screenToLocalCoordinates(Vector2(screenX, screenY))
            button.hit(local.x, local.y, true) != null
        }?.key

        if (level is CustomLevel) {
            game.screen = DeleteLevelScreen(game, level)
            return true
        }

        return false
    }

    override fun makeBody(world: World) =
        Table().also { table ->

            val achievements = loadAllAchievements()

            table.pad(UI_SPACE)

            // Later on, do some proper responsive sizing. However my first attempts struggled with
            // density independent pixel calculations (even though the math is simple, it didn't
            // seem to set proper breakpoints, perhaps because of the arbitrary math in calcDensityScaleFactor()
            // from before it occurred we could use DIPs).
            val levelsPerRow = if (Gdx.app.type == Application.ApplicationType.Desktop) 5 else 4
            val width = (stage.width - UI_SPACE * 2) / levelsPerRow
            val height = width * 3 / 4

            var x = 0
            var y = 0

            val levelButtonsBuilder = mutableMapOf<Level, WidgetGroup>()

            world.getLevels().forEachIndexed { i, level ->

                if (i % levelsPerRow == 0) {
                    table.row().left()
                    y ++
                    x = 0
                }

                val levelButton = makeLevelButton(level, achievements)
                levelButtonsBuilder[level] = levelButton
                table.add(levelButton).width(width).height(height)

                x ++

            }

            levelButtons = levelButtonsBuilder

            if (world is CustomWorld) {
                table.add(makeSquareButton(
                    false,
                    { buttonTable ->
                        buttonTable.add(Label("+", styles.label.huge))
                    }, {
                        onAddNewLevel(game) { setupStage(it) }
                    }
                )).width(width).height(height)

                if (world.getLevels().isNotEmpty()) {
                    table.row()
                    table.add(Label("Long press on levels to delete.", styles.label.small))
                        .center()
                        .space(UI_SPACE)
                        .colspan(levelsPerRow)
                }
            }
        }

    private fun makeSquareButton(isDisabled: Boolean, configureTable: (table: Table) -> Unit, onClick: () -> Unit): WidgetGroup {

        val buttonStyle = if (isDisabled) "locked" else "default"

        val button = Button(skin, buttonStyle).apply {
            this.isDisabled = isDisabled
            setFillParent(true)
            addListener(object: ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    onClick()
                }
            })
        }

        val table = Table().apply {
            setFillParent(true)
            touchable = Touchable.disabled // Let the button in the background do the interactivity.
            pad(Value.percentWidth(0.125f))
        }

        configureTable(table)

        return WidgetGroup(button, table)

    }

    private fun makeLevelButton(level: Level, allAchievements: List<Achievement>): WidgetGroup {

        val isLocked = level.getUnlockRequirements().isLocked(allAchievements)
        val textColor = if (isLocked) Color.GRAY else Color.WHITE

        val labelString = if (isLocked && !level.getUnlockRequirements().isAlmostUnlocked(allAchievements)) "???" else level.getLabel(strings)

        val highScore = loadHighScore(level)

        return makeSquareButton(isLocked, { table ->
            val levelLabel = Label(labelString, styles.label.medium).apply {
                wrap = true
                color = textColor
                setAlignment(Align.topLeft)
            }

            table.add(levelLabel).expand().fill().colspan(4)

            if (isLocked) {

                val unlockDescription = Label(level.getUnlockRequirements().describeOutstandingRequirements(strings, allAchievements), styles.label.small)
                unlockDescription.color = Color.GRAY

                table.row()
                table.add(unlockDescription)

            } else if (highScore.exists()) {

                val distanceLabel = Label(highScore.distancePercentString(), styles.label.small)
                val scoreLabel = Label(highScore.points.toString(), styles.label.small)

                val distanceIcon = makeIcon(sprites.right_sign)
                val scoreIcon = makeIcon(sprites.score)

                val iconSize = Value.percentWidth(0.75f)
                val iconSpace = Value.percentWidth(0.2f)

                table.row()
                table.add(distanceIcon).spaceRight(iconSpace).size(iconSize)
                table.add(distanceLabel)
                table.add(scoreIcon).spaceLeft(iconSpace).spaceRight(iconSpace).size(iconSize)
                table.add(scoreLabel).expandX().left()

            }
        }, { onLevelSelected(level) })
    }

    fun onLevelSelected(level: Level): Boolean {
        game.loadGame(level)

        return true
    }

}

