package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.CustomWorld
import com.serwylo.beatgame.levels.TheOriginalWorld
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.createCustomWorld
import com.serwylo.beatgame.levels.loadAllWorlds
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeButton
import com.serwylo.beatgame.ui.makeErrorReport
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage
import com.serwylo.beatgame.ui.makeWorldSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        return cachedWorlds ?: loadAllWorlds(forceIncludeCustom = initialWorld is CustomWorld).also { newlyLoaded ->
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

    protected fun setupStage(world: World) {
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
                Label("Turn your own songs into worlds", styles.label.large).apply {
                    setAlignment(Align.center)
                }
            ).pad(UI_SPACE).expandX().fill(0.75f, 0f).colspan(2)

            row()

            val existingCustomWorld = knownWorlds.find { it is CustomWorld }
            add(
                makeButton(if (existingCustomWorld != null) "Add to your own world" else "Add a new world", styles) {
                    if (existingCustomWorld == null) {
                        val newCustomWorld = createCustomWorld()

                        // If we have previously loaded the worlds (why wouldn't we at this point?
                        // I can't imagine a scenario where we wouldn't), make sure that we now also
                        // have a reference to this new one.
                        cachedWorlds?.also {
                            cachedWorlds = it + newCustomWorld
                        }

                        setupStage(newCustomWorld)
                    } else {
                        setupStage(existingCustomWorld)
                    }
                }
            ).pad(UI_SPACE).center().colspan(2).spaceBottom(UI_SPACE * 2)

            row()

            add(
                Label("See what worlds the community is building", styles.label.large).apply {
                    setAlignment(Align.center)
                }
            ).pad(UI_SPACE).expandX().fill(0.75f, 0f).colspan(2)

            row()

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

    /**
     * A bit of a cludge, but scene2d doesn't seem to support the notion of listening for
     * long presses on individual buttons. Thus, this work around where we add a top level
     * [com.badlogic.gdx.InputProcessor] which listens for long presses. When received, we
     * ask the child class if they want to process it. To do this, they need to manually
     * ask each of the relevant actors if they are interested in it, e.g. using
     * [Actor.screenToLocalCoordinates]. We don't have enough information to do that ourselves,
     * because we don't know what actors exist on the child classes stage.
     */
    protected open fun onLongPress(screenX: Float, screenY: Float): Boolean {
        return false;
    }

    override fun show() {

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = InputMultiplexer(
            GestureDetector(object : GestureAdapter() {
                override fun longPress(x: Float, y: Float): Boolean {
                    return onLongPress(x, y)
                }
            }),
            stage,
            object : InputAdapter() {

                override fun keyDown(keycode: Int): Boolean {
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                        game.showMenu()
                        return true
                    }

                    return false
                }

            }
        )

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

    companion object {
        private const val TAG = "WorldSelectorScreen"
    }

}

