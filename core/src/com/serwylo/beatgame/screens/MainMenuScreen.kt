package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.BeatGame

class MainMenuScreen(private val game: BeatGame): ScreenAdapter() {

    private val camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)

    private val spriteBatch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()

    private val menuItems = listOf(
            "the_haunted_mansion_the_courtyard.mp3",
            "the_haunted_mansion_the_exercise_room.mp3",
            "the_haunted_mansion_the_laundry_room.mp3",
            "the_haunted_mansion_the_ballroom.mp3",
            "vivaldi.mp3"
    )

    private val songs = sortedMapOf(
            Pair("the_haunted_mansion_the_courtyard.mp3", "The Courtyard"),
            Pair("the_haunted_mansion_the_exercise_room.mp3", "The Exercise Room"),
            Pair("the_haunted_mansion_the_laundry_room.mp3", "The Laundry Room"),
            Pair("the_haunted_mansion_the_ballroom.mp3", "The Ballroom"),
            Pair("vivaldi.mp3", "Vivaldi")
    )

    private var selectedIndex = 0

    private var font = BitmapFont().apply {
        data.scale(-0.6f)
    }

    private fun up() {
        selectedIndex --
        if (selectedIndex == -1) {
            selectedIndex = songs.size - 1
        }
    }

    private fun down() {
        selectedIndex = (selectedIndex + 1) % songs.size
    }

    override fun show() {

        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.UP) {

                    up()
                    return true

                } else if (keycode == Input.Keys.DOWN) {

                    down()
                    return true

                } else if (keycode == Input.Keys.ENTER) {

                    game.startGame(menuItems[selectedIndex])
                    return true

                }

                return false
            }

        }

    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val itemHeight = 10f

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(-VIEWPORT_WIDTH, (menuItems.size - selectedIndex) * itemHeight, VIEWPORT_WIDTH * 2, itemHeight)
        shapeRenderer.end()

        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()

        menuItems.forEachIndexed { i, _ ->
            font.draw(spriteBatch, songs[menuItems[i]], 0f, (menuItems.size - i) * itemHeight + itemHeight)
        }

        spriteBatch.end()

    }

    companion object {
        private const val VIEWPORT_WIDTH = 400f
        private const val VIEWPORT_HEIGHT = 200f
    }
}