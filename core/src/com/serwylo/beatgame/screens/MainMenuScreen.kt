package com.serwylo.beatgame.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.ui.*

class MainMenuScreen(private val game: BeatFeetGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        val container = VerticalGroup().apply {
            setFillParent(true)
            align(Align.center)
            space(UI_SPACE)
        }

        container.addActor(
            makeHeading(strings["app.name"], sprites.logo, styles, strings)
        )

        val buttonTable = Table()

        val playButton = makeLargeButton(strings["main-menu.btn.play"], styles) { game.showLevelSelectMenu() }
        val achievementsButton = makeButton(strings["main-menu.btn.achievements"], styles) { game.showAchievements() }
        val aboutButton = makeButton(strings["main-menu.btn.about"], styles) { game.showAboutScreen() }

        buttonTable.apply {
            pad(UI_SPACE)

            row()
            add(playButton).apply {
                fillX()
                padBottom(UI_SPACE * 2)
            }

            row()
            add(achievementsButton).apply {
                fillX()
            }

            row()
            add(aboutButton).apply {
                fillX()
            }
        }

        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            val quitButton = makeButton(strings["main-menu.btn.quit"], styles) { Gdx.app.exit() }
            buttonTable.row()
            buttonTable.add(quitButton).fillX()
        }

        container.addActor(buttonTable)

        stage.addActor(container)

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
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

}