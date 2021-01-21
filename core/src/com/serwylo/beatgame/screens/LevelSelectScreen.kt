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
import com.badlogic.gdx.utils.viewport.FitViewport
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Score
import com.serwylo.beatgame.audio.customMp3
import java.io.File
import kotlin.math.min

class LevelSelectScreen(private val game: BeatGame): ScreenAdapter() {

    private val atlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))

    private val stage = Stage(FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))
    private val skin = Skin(Gdx.files.internal("skin.json"))

    private var mediumFont = BitmapFont().apply { data.scale(0.2f) }
    private var smallFont = BitmapFont().apply { data.scale(-0.3f) }

    private val mediumLabelStyle = Label.LabelStyle(mediumFont, Color.WHITE)
    private val smallLabelStyle = Label.LabelStyle(smallFont, Color.WHITE)

    private val distanceTexture = atlas.findRegion("right_sign")
    private val scoreTexture = atlas.findRegion("score")

    init {

        val levelsPerRow = 4
        val rows = levels.size / levelsPerRow + 1
        val maxWidth = stage.width / levelsPerRow
        val maxHeight = stage.height / rows

        val width = min(maxWidth, maxHeight)
        val height = width

        var x = 0
        var y = 0

        val table = Table()
        //table.debug = true
        table.setFillParent(true)
        stage.addActor(table)

        levels.forEachIndexed { i, level ->
            if (i > 0 && i % levelsPerRow == 0) {
                table.row()
                y ++
                x = 0
            }


            table.add(makeButton(level)).width(width).height(height)
            /*button.x = padding + x * width + x * padding
            button.y = stage.height -
                    padding - /* pad from the edge of the screen */
                    (y * height) - /* Move this many rows down... */
                    (y * padding) - /* ... including padding for each row */
                    height /* and the origin of this actor is in the bottom left, so drop down 1 more cell */

            stage.addActor(button)*/

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

        val button = Button(skin)
        button.setFillParent(true)
        button.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onLevelSelected(level)
            }
        })

        val levelLabel = Label(level.label, mediumLabelStyle)
        levelLabel.wrap = true
        levelLabel.setAlignment(Align.topLeft)

        val table = Table()
        table.debug = false
        table.setFillParent(true)
        table.touchable = Touchable.disabled // Let the button in the background do the interactivity.
        table.pad(Value.percentWidth(0.125f))

        table.add(levelLabel).expand().fill().colspan(4)

        val highScore = Score.load(level.mp3Name)

        if (highScore.exists()) {

            val distanceLabel = Label(highScore.distancePercentString(), smallLabelStyle)
            val scoreLabel = Label(highScore.score.toString(), smallLabelStyle)

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

        private val levels = listOf(
                Level("the_haunted_mansion_the_courtyard.mp3", "The Courtyard"),
                Level("the_haunted_mansion_the_exercise_room.mp3", "The Exercise Room"),
                Level("the_haunted_mansion_the_laundry_room.mp3", "The Laundry Room"),
                Level("the_haunted_mansion_the_ballroom.mp3", "The Ballroom"),
                Level("vivaldi.mp3", "Vivaldi"),
                Level("custom.mp3", "{Custom}")
        )

        data class Level(
                val mp3Name: String,
                val label: String
        )

    }

}

