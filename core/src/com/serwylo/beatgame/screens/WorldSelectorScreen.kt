package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.TheOriginalWorld
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.loadAllWorlds
import com.serwylo.beatgame.ui.*
import kotlinx.coroutines.*
import ktx.async.KTX
import ktx.async.newSingleThreadAsyncContext
import ktx.async.onRenderingThread

abstract class WorldSelectorScreen(
    private val game: BeatFeetGame,
    private val headingId: String,
    private val headingIcon: TextureRegion,
    private val initialWorld: World
): ScreenAdapter() {

    protected val stage = makeStage()

    protected val sprites = game.assets.getSprites()
    protected val styles = game.assets.getStyles()
    protected val skin = game.assets.getSkin()
    protected val strings = game.assets.getStrings()

    private val header = Container<Actor>()
    private val body = Container<Actor>()

    private val job = Job()
    private val scope = CoroutineScope(newSingleThreadAsyncContext("WorldSelectorScreen") + job)

    private var currentWorld: World = initialWorld

    private var cachedWorlds: List<World>? = null

    private suspend fun allWorlds(): List<World> {
        return cachedWorlds ?: loadAllWorlds().also { newlyLoaded ->
            cachedWorlds = newlyLoaded
        }
    }

    init {

        val container = Table().apply {
            pad(UI_SPACE)
            padTop(UI_SPACE * 2)
        }

        val scrollPane = ScrollPane(container, skin).apply {
            setFillParent(true)
            setScrollingDisabled(true, false)
            setupOverscroll(width / 4, 30f, 200f)
        }

        stage.addActor(scrollPane)

        container.add(
            makeHeading(strings[headingId], headingIcon, styles, strings) {
                game.showMenu()
            }
        ).top()
        container.row()

        container.add(header)
        container.row()

        container.add(body).expand().fillX().top()
        container.row()

        body.fill()

        setupStage(initialWorld)

    }

    private fun setupStage(world: World) {
        this.currentWorld = world

        header.actor = makeWorldSelector(
            strings,
            styles,
            world,
            onPrevious = if (world === TheOriginalWorld) null else { -> onPreviousWorld(world) },
            onNext = { onNextWorld(world) },
        )

        body.actor = makeBody(world)
    }

    protected abstract fun makeBody(world: World): Actor

    private fun onPreviousWorld(currentWorld: World?) {
        if (currentWorld === TheOriginalWorld) {
            return
        }

        scope.launch {
            try {
                val worlds = performSlowOperation {
                    allWorlds()
                }
                if (currentWorld == null) {
                    setupStage(worlds.last())
                } else {
                    val currentIndex = worlds.indexOfFirst { it.getId() == currentWorld.getId() }
                    if (currentIndex > 0) {
                        setupStage(worlds[currentIndex - 1])
                    }
                }
            } catch (exception: Exception) {
                showError(exception) {
                    onPreviousWorld(currentWorld)
                }
            }
        }
    }

    private fun onNextWorld(currentWorld: World) {
        scope.launch {
            try {
                val worlds = performSlowOperation {
                    allWorlds()
                }

                onRenderingThread {
                    val currentIndex = worlds.indexOfFirst { it.getId() == currentWorld.getId() }
                    if (currentIndex < worlds.size - 1) {
                        setupStage(worlds[currentIndex + 1])
                    } else {
                        showComingSoon(worlds)
                    }
                }
            } catch (exception: Exception) {
                onRenderingThread {
                    showError(exception) {
                        onNextWorld(currentWorld)
                    }
                }
            }
        }
    }

    private suspend fun <T>performSlowOperation(block: suspend () -> T): T = withContext(Dispatchers.KTX) {
        header.actor.addAction(
            Actions.sequence(
                Actions.delay(0.5f),
                Actions.fadeOut(0.2f),
            )
        )

        body.actor.addAction(
            Actions.sequence(
                Actions.delay(0.5f),
                Actions.fadeOut(0.2f),
                Actions.run {
                    body.pad(UI_SPACE * 4)
                    body.actor = Label(strings["loading-screen.loading"], styles.label.medium).also { label ->
                        label.setAlignment(Align.center)
                        label.addAction(Actions.fadeIn(0.2f))
                    }
                },
            )
        )

        val result = withContext(scope.coroutineContext) {
            block()
        }

        header.actor.clearActions()
        body.actor.clearActions()
        body.pad(0f)

        header.actor.color.a = 1f
        body.actor.color.a = 1f

        result
    }

    private fun showError(error: Throwable, tryAgain: () -> Unit) {
        header.clear()
        body.actor = makeErrorReport(
            strings,
            styles,
            error,
            strings["error.message.downloading-list-of-levels"],
            tryAgain
        )
    }

    private fun showComingSoon(knownWorlds: List<World>) {
        header.actor = makeWorldSelector(strings, styles, null, onPrevious = {
            onPreviousWorld(null)
        })

        body.actor = Table().apply {
            pad(UI_SPACE)
            padTop(UI_SPACE * 4)
            add(
                Label(strings["level-select.more-coming-soon.ask-for-recommendations"], styles.label.medium).also { label ->
                    label.wrap = true
                    label.setAlignment(Align.center)
                }
            ).pad(UI_SPACE).expandX().fill(0.75f, 0f).colspan(2)
            row().pad(UI_SPACE)

            add(
                Label(strings["level-select.more-coming-soon.when-we-find-time"], styles.label.small).also { label ->
                    label.wrap = true
                    label.setAlignment(Align.center)
                }
            ).pad(UI_SPACE).expandX().fill(0.6f, 0f).colspan(2)
            row().pad(UI_SPACE)

            add(
                makeButton(strings["level-select.more-coming-soon.suggest-a-song"], styles) {
                    Gdx.net.openURI("https://github.com/beat-feet/beat-feet/issues/new?title=Song%20suggestion:%20&labels=song+suggestion&body=(PLEASE%20NOTE:%20This%20game%20is%20open%20source,%20and%20all%20songs%20included%20in%20it%20must%20be%20freely%20licensed,%20for%20example,%20CC-BY)")
                }
            ).pad(UI_SPACE).right()

            add(
                makeButton("Check for new levels", styles) {
                    refreshLevels(knownWorlds)
                }
            ).pad(UI_SPACE).left()
        }

    }

    private fun refreshLevels(knownWorlds: List<World>) {
        scope.launch {
            try {
                val newWorlds = performSlowOperation {
                    loadAllWorlds(forceUncached = true).also { newlyLoaded ->
                        cachedWorlds = newlyLoaded
                    }
                }

                val oldLevelCount = knownWorlds.sumOf { it.getLevels().size }
                val newLevelCount = newWorlds.sumOf { it.getLevels().size }

                onRenderingThread {
                    when {
                        newWorlds.size > knownWorlds.size -> setupStage(newWorlds[knownWorlds.size])
                        oldLevelCount != newLevelCount -> setupStage(newWorlds.last())
                        else -> showComingSoon(newWorlds)
                    }
                }
            } catch (exception: Exception) {
                onRenderingThread {
                    showError(exception) {
                        refreshLevels(knownWorlds)
                    }
                }
            }
        }
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

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        setupStage(currentWorld)
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

    }

    override fun dispose() {
        stage.dispose()
        scope.cancel()
    }

}

