package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.LevelGroup
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.ui.*
import java.io.File

class LevelSelectScreen(private val game: BeatFeetGame, private val levelGroup: LevelGroup): ScreenAdapter() {

    private val stage = makeStage()

    private val sprites = game.assets.getSprites()
    private val styles = game.assets.getStyles()
    private val skin = game.assets.getSkin()
    private val strings = game.assets.getStrings()

    private val distanceTexture = sprites.right_sign
    private val scoreTexture = sprites.score

    private val achievements = loadAllAchievements()

    init {
        setupStage()
    }

    private fun setupStage() {
        // Later on, do some proper responsive sizing. However my first attempts struggled with
        // density independent pixel calculations (even though the math is simple, it didn't
        // seem to set proper breakpoints, perhaps because of the arbitrary math in calcDensityScaleFactor()
        // from before it occurred we could use DIPs).
        val levelsPerRow = if (Gdx.app.type == Application.ApplicationType.Desktop) 5 else 4
        val width = (stage.width - UI_SPACE * 2) / levelsPerRow
        val height = width * 3 / 4

        var x = 0
        var y = 0

        val container = VerticalGroup().apply {
            space(UI_SPACE)
            padTop(UI_SPACE * 2)
        }

        val scrollPane = ScrollPane(container, skin).apply {
            setFillParent(true)
            setScrollingDisabled(true, false)
            setupOverscroll(width / 4, 30f, 200f)
        }

        stage.addActor(scrollPane)

        container.addActor(
            makeHeading(strings["level-select.title"], sprites.logo, styles, strings) {
                game.showMenu()
            }
        )

        container.addActor(
            makeLevelGroupSelector(
                styles,
                levelGroup,
            ) { newGroup -> game.showLevelSelectMenu(newGroup) }
        )

        val table = Table().apply {
            pad(UI_SPACE)
        }

        container.addActor(table)

        levelGroup.levels.forEachIndexed { i, level ->

            if (i % levelsPerRow == 0) {
                table.row()
                y ++
                x = 0
            }

            table.add(makeButton(level)).width(width).height(height)

            x ++

        }

    }

    override fun show() {

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

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        stage.clear()
        setupStage()
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

    }

    override fun dispose() {
        stage.dispose()
    }

    private fun makeButton(level: Level): WidgetGroup {

        val isLocked = level.unlockRequirements.isLocked(achievements)
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

        val labelString = if (isLocked && !level.unlockRequirements.isAlmostUnlocked(achievements)) "???" else strings[level.labelId]

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

            val unlockDescription = Label(level.unlockRequirements.describeOutstandingRequirements(strings, achievements), styles.label.small)
            unlockDescription.color = textColor

            table.row()
            table.add(unlockDescription)

        } else if (highScore.exists()) {

            val distanceLabel = Label(highScore.distancePercentString(), styles.label.small)
            val scoreLabel = Label(highScore.points.toString(), styles.label.small)

            val distanceIcon = makeIcon(distanceTexture)
            val scoreIcon = makeIcon(scoreTexture)

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
        if (level.mp3Name == "custom.mp3") {
            val file = customMp3()
            if (!file.exists()) {
                game.explainCustomSongs()
            } else {
                game.loadGame(file, "{Custom}")
            }
        } else {
            game.loadGame(Gdx.files.internal("songs${File.separator}mp3${File.separator}${level.mp3Name}"), strings[level.labelId])
        }

        return true
    }

}

