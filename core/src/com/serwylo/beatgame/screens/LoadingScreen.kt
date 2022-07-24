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
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.audio.loadWorldFromMp3
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.loadHighScore
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeIcon
import com.serwylo.beatgame.ui.makeStage

class LoadingScreen(
    private val game: BeatFeetGame,
    private val musicFile: FileHandle,
    songName: String
    ): ScreenAdapter() {

    private val stage = makeStage()

    private val level = Levels.byId(musicFile.name())

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading(songName, sprites.logo, styles, strings)
        )

        val topScore = loadHighScore(level)

        val bestLabel = Label(strings["loading-screen.best"], styles.label.medium)
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
            Label(strings["loading-screen.loading"], styles.label.medium)
        )

        if (level === Levels.Custom) {
            container.addActor(
                Label(customMp3().file().absolutePath, styles.label.small)
            )
        }

        // All other loading is quite quick, because it is just processing pre-generated JSON data.
        // Loading a custom level however will be slow the *first* time it runs. Every time afterwards
        // it will be as fast as others because it will use the cached JSON data however.
        // After 5 seconds, fade in a polite warning message asking patience.
        if (songName == "{Custom}") {
            val slowWarning = Label(strings["loading-screen.custom-song-warning"], styles.label.small)
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

            // Stay around for just a little longer with custom songs, because we show the file path
            // that you need to change in order to change the song. Once you've used custom songs
            // the first time, this is the only place where you can see this information, so if it
            // disappears too quickly, the user will never be able to find the path again.
            val minTime = if (level === Levels.Custom) MIN_LOAD_TIME * 2 else MIN_LOAD_TIME
            if (loadTime < minTime) {
                Thread.sleep(minTime - loadTime)
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