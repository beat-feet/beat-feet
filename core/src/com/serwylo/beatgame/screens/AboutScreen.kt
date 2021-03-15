package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage

class AboutScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading(strings["about.title"], sprites.logo, styles, strings) {
                game.showMenu()
            }
        )

        credits(strings).entries.forEach { entry ->

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
        private fun credits(strings: I18NBundle) = mapOf(

            // Intentionally leave these un-internationalised, because they need to refer back
            // to the original source and license to avoid ambiguity

            strings["about.credits.music"] to listOf(
                "The Haunted Mansion / CC-BY-SA 3.0",
                "Awakening / CC-BY-SA 3.0",
                "Health and Safety / CC-BY-SA 3.0",
                "John Harrison w/ Wichita State University Chamber / CC-BY-SA 3.0"
            ),

            strings["about.credits.graphics"] to listOf(
                "Kenney.nl / CCO 1.0",
                "disabledpaladin / CC-BY-SA 4.0"
            )

        )
    }

}