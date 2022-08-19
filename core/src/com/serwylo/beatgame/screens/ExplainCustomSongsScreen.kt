package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.levels.TheOriginalWorld
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeButton
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage

class ExplainCustomSongsScreen(private val game: BeatFeetGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        val table = Table().apply {

            pad(UI_SPACE * 2)

            val title = makeHeading(strings["custom-songs.title"], sprites.logo, styles, strings) {
                game.showLevelSelectMenu(TheOriginalWorld)
            }

            row()
            add(title).center().pad(UI_SPACE)

            val mp3File = customMp3().file()
            val description = Label(strings["custom-songs.description"], styles.label.medium).apply {
                setAlignment(Align.center)
                wrap = true
            }

            row()
            add(description).prefWidth(Value.percentWidth(1f)).pad(UI_SPACE * 2)

            val path = Label(mp3File.absolutePath, styles.label.small).apply {
                setAlignment(Align.center)
                wrap = true
            }

            row()
            add(path).center().pad(UI_SPACE * 2)

            val copied = Label("Copied to clipboard!", styles.label.small).apply {
                setAlignment(Align.center)
                color = color.cpy().apply { a = 0f }
            }

            val copyPath = makeButton(strings["custom-songs.copy-folder"], styles) {
                game.platformListener.copyToClipboard(mp3File.parentFile?.absolutePath ?: mp3File.absolutePath)
                copied.clearActions()
                copied.addAction(
                    sequence(
                        alpha(0f),
                        fadeIn(0.1f),
                        delay(1f),
                        fadeOut(0.8f)
                    )
                )
            }

            row()
            add(copyPath).center()

            row()
            add(copied).center()

        }

        stage.addActor(
            ScrollPane(table).apply {
                setFillParent(true)
                setScrollingDisabled(true, false)
                setupOverscroll(UI_SPACE, 30f, 200f)
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

}