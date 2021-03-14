package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage

class AboutScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading("Credits", sprites.logo, styles) {
                game.showMenu()
            }
        )

        CREDITS.entries.forEach { entry ->

            val heading = entry.key
            val values = entry.value

            container.addActor(
                Label(heading, styles.label.large).apply {
                    setAlignment(Align.center)
                }
            )

            values.forEach { value ->

                container.addActor(
                    Label(value, styles.label.medium).apply {
                        setAlignment(Align.center)
                    }
                )

            }
        }

        stage.addActor(container)

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
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

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    companion object {
        private val CREDITS = mapOf(

            "Music" to listOf(
                "The Haunted Mansion / CC-BY-SA 3.0",
                "Awakening / CC-BY-SA 3.0",
                "Health and Safety / CC-BY-SA 3.0",
                "John Harrison w/ Wichita State University Chamber / CC-BY-SA 3.0"
            ),

            "Graphics" to listOf(
                "Kenney.nl / CCO 1.0",
                "disabledpaladin / CC-BY-SA 4.0"
            )

        )
    }

}