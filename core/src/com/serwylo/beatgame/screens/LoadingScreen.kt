package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.loadWorldFromMp3
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeIcon
import com.serwylo.beatgame.ui.makeStage

class LoadingScreen(
    private val game: BeatGame,
    private val musicFile: FileHandle,
    songName: String
    ): ScreenAdapter() {

    private val stage = makeStage()

    private val level = Levels.bySong(musicFile.name())

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading(songName, sprites.logo, styles)
        )

        val topScore = loadHighScore(level)

        val bestLabel = Label("Best", styles.label.medium)
        val distanceLabel = Label("${(topScore.distancePercent * 100).toInt()}%", styles.label.medium)
        val scoreLabel = Label("${topScore.points}", styles.label.medium)

        val distanceImage = makeIcon(sprites.right_sign)
        val scoreImage = makeIcon(sprites.score)

        container.addActor(
            HorizontalGroup().apply {
                space(UI_SPACE)
                addActor(bestLabel)
                addActor(distanceImage)
                addActor(distanceLabel)
                addActor(scoreImage)
                addActor(scoreLabel)
            }
        )

        container.addActor(
            Label("Loading", styles.label.medium)
        )

        // All other loading is quite quick, because it is just processing pre-generated JSON data.
        // Loading a custom level however will be slow the *first* time it runs. Every time afterwards
        // it will be as fast as others because it will use the cached JSON data however.
        // After 5 seconds, fade in a polite warning message asking patience.
        if (songName == "{Custom}") {
            val slowWarning = Label("Please be patient, it may take some time when analysing your song for the first time...", styles.label.small)
            container.addActor(slowWarning)

            slowWarning.addAction(
                sequence(
                    alpha(0f),
                    delay(5f),
                    fadeIn(2f)
                )
            )
        }

        stage.addActor(container)

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun show() {
        super.show()
        startLoading()
    }

    private fun startLoading() {
        Thread {

            val startTime = System.currentTimeMillis()
            val world = loadWorldFromMp3(musicFile)
            val loadTime = System.currentTimeMillis() - startTime
            if (loadTime < MIN_LOAD_TIME) {
                Thread.sleep(MIN_LOAD_TIME - loadTime)
            }
            game.startGame(world)

        }.start()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        stage.act(delta)
        stage.draw()

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    companion object {
        private const val MIN_LOAD_TIME = 1000
    }

}