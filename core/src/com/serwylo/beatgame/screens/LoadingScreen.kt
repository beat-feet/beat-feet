package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.features.LevelData
import com.serwylo.beatgame.audio.loadCachedLevelData
import com.serwylo.beatgame.audio.loadLevelDataFromMp3
import com.serwylo.beatgame.levels.*
import com.serwylo.beatgame.ui.*
import kotlinx.coroutines.*
import ktx.async.newSingleThreadAsyncContext
import ktx.async.onRenderingThread

class LoadingScreen(
    private val game: BeatFeetGame,
    private val level: Level,
): ScreenAdapter() {

    private val stage = makeStage()
    private val loadingLabel: Label

    private val sprites = game.assets.getSprites()
    private val styles = game.assets.getStyles()
    private val strings = game.assets.getStrings()

    private val job = Job()
    private val scope = CoroutineScope(newSingleThreadAsyncContext("LoadingScreen") + job)

    init {



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

    private fun showError(exception: Exception) {
        Gdx.input.inputProcessor = stage
        stage.clear()
        stage.addActor(
            ScrollPane(
                Table().apply {
                    add(
                        makeHeading(level.getLabel(strings), sprites.logo, styles, strings) {
                            game.showLevelSelectMenu(level.getWorld())
                        }
                    )
                    row()
                    add(
                        makeErrorReport(
                            game.assets.getStrings(),
                            game.assets.getStyles(),
                            exception,
                            game.assets.getStrings()["error.message.downloading-level-data"]
                        ) {
                            game.loadGame(level)
                        }
                    )
                }
            ).apply {
                setFillParent(true)
                setScrollingDisabled(true, false)
                setupOverscroll(width / 4, 30f, 200f)
            }
        )
    }

    private fun startLoading() {
        val strings = game.assets.getStrings()
        scope.launch {

            val startTime = System.currentTimeMillis()

            val levelData: LevelData = when (level) {

                is RemoteLevel -> {
                    val levelDataFile = try {

                        if (!level.getMp3File().exists()) {
                            onRenderingThread {
                                loadingLabel.setText(strings["loading-screen.downloading-song"])
                            }

                            level.ensureMp3Downloaded()
                        }

                        val levelDataFile = level.getLevelDataFile()
                        if (!levelDataFile.exists()) {
                            onRenderingThread {
                                loadingLabel.setText(strings["loading-screen.downloading-level"])
                            }

                            level.ensureLevelDataDownloaded()
                        }

                        levelDataFile

                    } catch (exception: Exception) {
                        onRenderingThread {
                            showError(exception)
                        }

                        return@launch
                    }

                    loadCachedLevelData(levelDataFile)
                }

                is LegacyCustomLevel, is CustomLevel -> {
                    val file = level.getLevelDataFile()
                    if (!file.exists()) {
                        onRenderingThread {
                            loadingLabel.setText(strings["loading-screen.analysing-mp3"] + "\n" + level.getMp3File().file().absolutePath + "\n" + strings["loading-screen.custom-song-warning"])
                        }

                        loadLevelDataFromMp3(level)
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
            val minTime = if (level === LegacyCustomLevel) MIN_LOAD_TIME * 2 else MIN_LOAD_TIME
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