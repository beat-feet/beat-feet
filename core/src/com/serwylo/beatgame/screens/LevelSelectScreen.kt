package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.levels.loadHighScore
import java.io.File

class LevelSelectScreen(private val game: BeatGame): ScreenAdapter() {

    private val atlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))

    private val stage = Stage(ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))
    private val skin = Skin(Gdx.files.internal("skin.json"))

    private var mediumFont = BitmapFont().apply { data.scale(0.2f) }
    private var smallFont = BitmapFont().apply { data.scale(-0.3f) }

    private val mediumLabelStyle = Label.LabelStyle(mediumFont, Color.WHITE)
    private val smallLabelStyle = Label.LabelStyle(smallFont, Color.WHITE)

    private val distanceTexture = atlas.findRegion("right_sign")
    private val scoreTexture = atlas.findRegion("score")

    private val achievements = loadAllAchievements()

    init {

        val levelsPerRow = 4
        val size = stage.width / levelsPerRow

        var x = 0
        var y = 0

        val table = Table()

        val scrollPane = ScrollPane(table, skin)
        scrollPane.setFillParent(true)
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setupOverscroll(size / 4, 30f, 200f)

        stage.addActor(scrollPane)

        Levels.all.forEachIndexed { i, level ->

            if (i > 0 && i % levelsPerRow == 0) {
                table.row()
                y ++
                x = 0
            }

            table.add(makeButton(level)).width(size).height(size)

            x ++

        }

    }

    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
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

        val button = Button(skin, buttonStyle)
        button.isDisabled = isLocked
        button.setFillParent(true)
        button.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onLevelSelected(level)
            }
        })

        val labelString = if (isLocked && !level.unlockRequirements.isAlmostUnlocked(achievements)) "???" else level.label
        val levelLabel = Label(labelString, mediumLabelStyle)
        levelLabel.wrap = true
        levelLabel.color = textColor
        levelLabel.setAlignment(Align.topLeft)

        val table = Table()
        table.setFillParent(true)
        table.touchable = Touchable.disabled // Let the button in the background do the interactivity.
        table.pad(Value.percentWidth(0.125f))

        table.add(levelLabel).expand().fill().colspan(4)

        val highScore = loadHighScore(level)

        if (isLocked) {

            val unlockDescription = Label(level.unlockRequirements.describeOutstandingRequirements(achievements), smallLabelStyle)
            unlockDescription.color = textColor

            table.row()
            table.add(unlockDescription)

        } else if (highScore.exists()) {

            val distanceLabel = Label(highScore.distancePercentString(), smallLabelStyle)
            val scoreLabel = Label(highScore.points.toString(), smallLabelStyle)

            val distanceIcon = Image(distanceTexture)
            val scoreIcon = Image(scoreTexture)

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
            game.loadGame(Gdx.files.internal("songs${File.separator}mp3${File.separator}${level.mp3Name}"), level.label)
        }

        return true
    }

    companion object {

        @JvmStatic
        private val VIEWPORT_WIDTH = 600f

        @JvmStatic
        private val VIEWPORT_HEIGHT = 400f

    }

}

