package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.customMp3
import com.serwylo.beatgame.audio.loadLevelDataFromMp3
import com.serwylo.beatgame.levels.*
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeIcon
import com.serwylo.beatgame.ui.makeStage
import kotlinx.coroutines.*
import javax.xml.bind.JAXBElement

class LoadingScreen(
    private val game: BeatFeetGame,
    private val level: Level,
): ScreenAdapter() {

    private val stage = makeStage()
    private val loadingLabel: Label

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        val container = VerticalGroup()
        container.setFillParent(true)
        container.align(Align.center)
        container.space(UI_SPACE)

        container.addActor(
            makeHeading(level.getLabel(strings), sprites.logo, styles, strings)
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

        loadingLabel = Label(strings["loading-screen.loading"], styles.label.medium).also { label ->
            container.addActor(label)
        }

        if (level === CustomLevel) {
            container.addActor(
                Label(customMp3().file().absolutePath, styles.label.small)
            )

            // All other loading is quite quick, because it is just processing pre-generated JSON data.
            // Loading a custom level however will be slow the *first* time it runs. Every time afterwards
            // it will be as fast as others because it will use the cached JSON data however.
            // After 5 seconds, fade in a polite warning message asking patience.
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

    private suspend fun <T>performSlowOperation(label: String, block: suspend () -> T): T = withContext(Dispatchers.IO) {
        loadingLabel.addAction(
            sequence(
                delay(1f),
                fadeOut(0.3f),
                Actions.run { loadingLabel.setText(label) },
                fadeIn(0.3f),
            )
        )

        val result = block()

        loadingLabel.clearActions()

        result
    }

    private fun startLoading() {
        val strings = game.assets.getStrings()
        scope.launch {

            val startTime = System.currentTimeMillis()

            if (level is RemoteLevel) {
                performSlowOperation(strings["loading-screen.downloading-song"]) {
                    level.ensureMp3Downloaded()
                }

                performSlowOperation(strings["loading-screen.downloading-level"]) {
                    level.ensureLevelDataDownloaded()
                }
            }

            val levelData = performSlowOperation(strings["loading-screen.generating-buildings"]) {
                loadLevelDataFromMp3(level.getMp3File())
            }

            val loadTime = System.currentTimeMillis() - startTime

            // Stay around for just a little longer with custom songs, because we show the file path
            // that you need to change in order to change the song. Once you've used custom songs
            // the first time, this is the only place where you can see this information, so if it
            // disappears too quickly, the user will never be able to find the path again.
            val minTime = if (level === CustomLevel) MIN_LOAD_TIME * 2 else MIN_LOAD_TIME
            if (loadTime < minTime) {
                kotlinx.coroutines.delay(minTime - loadTime)
            }
            game.startGame(level, levelData)

        }
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

    override fun dispose() {
        scope.cancel()
        stage.dispose()
    }

    companion object {
        private const val MIN_LOAD_TIME = 1000
    }

}