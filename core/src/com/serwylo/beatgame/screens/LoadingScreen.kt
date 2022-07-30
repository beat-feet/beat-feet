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
import com.serwylo.beatgame.audio.features.LevelData
import com.serwylo.beatgame.audio.loadCachedLevelData
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

        container.addActor(
            Label(strings["loading-screen.loading"], styles.label.medium)
        )

        loadingLabel = Label("", styles.label.small).also { label ->
            container.addActor(label)
            label.setAlignment(Align.center)
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
        val strings = game.assets.getStrings()
        scope.launch {

            val startTime = System.currentTimeMillis()

            val levelData: LevelData = when (level) {

                is RemoteLevel -> {
                    loadingLabel.setText(strings["loading-screen.downloading-song"])
                    level.ensureMp3Downloaded()

                    loadingLabel.setText(strings["loading-screen.downloading-level"])
                    level.ensureLevelDataDownloaded()

                    loadCachedLevelData(level.getLevelDataFile())
                }

                is CustomLevel -> {
                    val file = level.getLevelDataFile()
                    if (!file.exists()) {
                        loadingLabel.setText(strings.format("loading-screen.analysing-mp3", level.getMp3File().file().absolutePath) + "\n" + strings["loading-screen.custom-song-warning"])
                        loadLevelDataFromMp3(level.getMp3File())
                    } else {
                        loadCachedLevelData(file)
                    }
                }

                is BuiltInLevel -> loadCachedLevelData(level.getLevelDataFile())
            }

            val loadTime = System.currentTimeMillis() - startTime

            // Stay around for just a little longer with custom songs, because we show the file path
            // that you need to change in order to change the song. Once you've used custom songs
            // the first time, this is the only place where you can see this information, so if it
            // disappears too quickly, the user will never be able to find the path again.
            val minTime = if (level === CustomLevel) MIN_LOAD_TIME * 2 else MIN_LOAD_TIME
            if (loadTime < minTime) {
                delay(minTime - loadTime)
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