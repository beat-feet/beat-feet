package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage

class ExplainCustomSongsScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()

        stage.addActor(
            VerticalGroup().apply {

                setFillParent(true)
                align(Align.center)
                space(UI_SPACE)

                addActor(
                    makeHeading("Custom Songs", sprites.logo, styles)
                )

                val description = "Want a level that matches your favourite song?\n\nCopy an MP3 file to:\n\n${customMp3().file().absolutePath}\n\nWe'll generate a level just for you!"
                addActor(
                    Label(description, styles.label.medium).apply {
                        setAlignment(Align.center)
                    }
                )
            }
        )
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = InputMultiplexer(stage, object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                }

                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                game.showMenu()
                return true
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

}