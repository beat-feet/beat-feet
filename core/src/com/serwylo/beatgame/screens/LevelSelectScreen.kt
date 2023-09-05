package com.serwylo.beatgame.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
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
import com.serwylo.beatgame.levels.CustomWorld
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.achievements.Achievement
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.levels.addCustomLevel
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeIcon
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration
import java.io.FilenameFilter


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
                    table.row().left()
                    y ++
                    x = 0
                }

                table.add(makeLevelButton(level, achievements)).width(width).height(height)

                x ++

            }

            if (world is CustomWorld) {
                table.add(makeSquareButton(
                    false,
                    { buttonTable ->
                        buttonTable.add(Label("+", styles.label.huge))
                    }, {
                        val conf = NativeFileChooserConfiguration()
                        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));

                        // Filter out all files which do not have the .ogg extension and are not of an audio MIME type - belt and braces
                        conf.mimeFilter = "audio/*"
                        conf.nameFilter = FilenameFilter { dir, name -> name.endsWith("mp3") }
                        conf.title = "Choose MP3 file";


                        game.platformListener.fileChooser().chooseFile(conf, object : NativeFileChooserCallback {
                            override fun onFileChosen(file: FileHandle) {
                                val world = addCustomLevel(file)
                                setupStage(world)
                            }

                            override fun onCancellation() {
                            }

                            override fun onError(exception: Exception) {
                            }
                        })
                    }
                )).width(width).height(height)
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

