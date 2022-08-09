package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.*
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.ui.*

class LevelSelectScreen(private val game: BeatFeetGame, initialWorld: World): WorldSelectorScreen(
    game,
    "level-select.title",
    game.assets.getSprites().logo,
    initialWorld,
) {

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

            world.getLevels().forEachIndexed { i, level ->

                if (i % levelsPerRow == 0) {
                    table.row()
                    y ++
                    x = 0
                }

                table.add(makeButton(level, achievements)).width(width).height(height)

                x ++

            }
        }

    private fun makeButton(level: Level, allAchievements: List<Achievement>): WidgetGroup {

        val isLocked = level.getUnlockRequirements().isLocked(allAchievements)
        val buttonStyle = if (isLocked) "locked" else "default"
        val textColor = if (isLocked) Color.GRAY else Color.WHITE

        val button = Button(skin, buttonStyle).apply {
            isDisabled = isLocked
            setFillParent(true)
            addListener(object: ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    onLevelSelected(level)
                }
            })
        }

        val labelString = if (isLocked && !level.getUnlockRequirements().isAlmostUnlocked(allAchievements)) "???" else level.getLabel(strings)

        val levelLabel = Label(labelString, styles.label.medium).apply {
            wrap = true
            color = textColor
            setAlignment(Align.topLeft)
        }

        val table = Table().apply {
            setFillParent(true)
            touchable = Touchable.disabled // Let the button in the background do the interactivity.
            pad(Value.percentWidth(0.125f))

            add(levelLabel).expand().fill().colspan(4)
        }

        val highScore = loadHighScore(level)

        if (isLocked) {

            val unlockDescription = Label(level.getUnlockRequirements().describeOutstandingRequirements(strings, allAchievements), styles.label.small)
            unlockDescription.color = textColor

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

        return WidgetGroup(button, table)

    }

    fun onLevelSelected(level: Level): Boolean {
        if (level === CustomLevel) {
            val file = level.getMp3File()
            if (!file.exists()) {
                game.explainCustomSongs()
            } else {
                game.loadGame(level)
            }
        } else {
            game.loadGame(level)
        }

        return true
    }

}

