package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import java.io.File

class LevelSelectScreen(private val game: BeatFeetGame): ScreenAdapter() {

    companion object {

        val map = """
        A
        |      C-----+
        +-B----+     |  +-E
                     |  | |
          J-+--O---N +--D |
            |  |   |    | F
            |  M---L----K |
            |             |
            I--------H----G
        """.trimIndent().split("\n").asReversed().map { it.toCharArray() }

        val tileSize = 100f

    }

    private val sprites = game.assets.getSprites()
    private val strings = game.assets.getStrings()
    private val styles = game.assets.getStyles()

    private val achievements = loadAllAchievements()

    val up = 0b0001
    val right = 0b0010
    val down = 0b0100
    val left = 0b1000

    val roadSpritesWithMarker = mapOf(
        up or right or down or left to sprites.road_vertical_horizontal_marker,
        down or left or right to sprites.road_down_horizontal_marker,
        down or left to sprites.road_down_left_marker,
        down to sprites.road_down_marker,
        down or right to sprites.road_down_right_marker,
        left or right to sprites.road_horizontal_marker,
        left to sprites.road_left_marker,
        right to sprites.road_right_marker,
        up or left or right to sprites.road_up_horizontal_marker,
        up or left to sprites.road_up_left_marker,
        up to sprites.road_up_marker,
        up or right to sprites.road_up_right_marker,
        up or down or left to sprites.road_vertical_left_marker,
        up or down to sprites.road_vertical_marker,
        up or down or right to sprites.road_vertical_right_marker,
    )

    val roadSprites = mapOf(
        up or right or down or left to sprites.road_vertical_horizontal,
        down or left or right to sprites.road_down_horizontal,
        down or left to sprites.road_down_left,
        down to sprites.road_down,
        down or right to sprites.road_down_right,
        left or right to sprites.road_horizontal,
        left to sprites.road_left,
        right to sprites.road_right,
        up or left or right to sprites.road_up_horizontal,
        up or left to sprites.road_up_left,
        up to sprites.road_up,
        up or right to sprites.road_up_right,
        up or down or left to sprites.road_vertical_left,
        up or down to sprites.road_vertical,
        up or down or right to sprites.road_vertical_right,
    )

    val markerIndices = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    val markers = listOf(
        sprites.marker_a,
        sprites.marker_b,
        sprites.marker_c,
        sprites.marker_d,
        sprites.marker_e,
        sprites.marker_f,
        sprites.marker_g,
        sprites.marker_h,
        sprites.marker_i,
        sprites.marker_j,
        sprites.marker_k,
        sprites.marker_l,
        sprites.marker_m,
        sprites.marker_n,
        sprites.marker_o,
        sprites.marker_p,
        sprites.marker_q,
        sprites.marker_r,
        sprites.marker_s,
        sprites.marker_t,
    )

    private val stage = Stage()

    init {
        val cellSize = tileSize
        for (y in map.indices) {
            val row = map[y]
            for (x in row.indices) {
                getRoadSprite(x, y)?.also { sprite ->
                    stage.addActor(Image(sprite).also { image ->
                        image.x = x * cellSize
                        image.y = y * cellSize
                        image.width = cellSize
                        image.height = cellSize
                    })
                }

                getMarker(x, y)?.also { levelLetter ->

                    val index = markerIndices.indexOf(levelLetter)
                    val level = Levels.all[index]
                    val isLocked = level.unlockRequirements.isLocked(achievements)
                    val sprite = if (isLocked) sprites.marker_blank_small else sprites.marker_blank_large

                    Image(sprite).also { image ->
                        image.x = x * cellSize
                        image.y = y * cellSize
                        image.width = cellSize
                        image.height = cellSize
                        image.touchable = Touchable.enabled
                        image.addListener(object: ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                onLevelSelected(level)
                            }
                        })

                        stage.addActor(image)
                    }

                    Label(strings[level.labelId], styles.label.medium).also { label ->

                        label.x = x * cellSize + cellSize
                        label.y = y * cellSize + cellSize

                        stage.addActor(label)
                    }


                }
            }
        }
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

    private fun isRoad(x: Int, y: Int): Boolean {
        val row = map.getOrNull(y) ?: return false
        val char = row.getOrNull(x) ?: return false

        return char.toString().matches("""[+\-|A-Z]""".toRegex())
    }

    private fun getMarker(x: Int, y: Int): Char? {
        val row = map.getOrNull(y) ?: return null
        val char = row.getOrNull(x) ?: return null

        return if (char.toString().matches("""[A-Z]""".toRegex())) {
            char
        } else {
            null
        }
    }

    private fun getMarkerSprite(x: Int, y: Int): TextureRegion? {
        val marker = getMarker(x, y) ?: return null
        val index = markerIndices.indexOf(marker)
        return if (index >= 0) {
            markers[index]
        } else {
            null
        }
    }

    private fun getRoadSprite(x: Int, y: Int): TextureRegion? {

        if (!isRoad(x, y)) {
            return null
        }

        val isAbove = if (isRoad(x, y + 1)) up else 0
        val isBelow = if (isRoad(x, y - 1)) down else 0
        val isLeft = if (isRoad(x - 1, y)) left else 0
        val isRight = if (isRoad(x + 1, y)) right else 0

        return roadSprites[isAbove or isBelow or isLeft or isRight]

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

}
