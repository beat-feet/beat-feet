package com.serwylo.beatgame.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.ui.*

class MainMenuScreen(private val game: BeatGame): ScreenAdapter() {

    private val stage = makeStage()

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading(
                "Beat Game",
                sprites.logo,
                styles
            )
        )

        val buttonTable = Table()
        buttonTable.pad(UI_SPACE)

        val playButton = makeLargeButton("Play", styles) { game.showLevelSelectMenu() }
        val achievementsButton = makeButton("Achievements", styles) { game.showAchievements() }
        val aboutButton = makeButton("About", styles) { game.showAboutScreen() }

        buttonTable.apply {
            row()
            add(playButton).fillX().padBottom(UI_SPACE * 2)
            row()
            add(achievementsButton).fillX()
            row()
            add(aboutButton).fillX()
        }

        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            val quitButton = makeButton("Quit", styles) { Gdx.app.exit() }
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