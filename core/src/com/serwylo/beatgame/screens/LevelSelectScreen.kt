package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.*
import com.serwylo.beatgame.levels.achievements.loadAllAchievements
import com.serwylo.beatgame.ui.*
import kotlinx.coroutines.*

class LevelSelectScreen(private val game: BeatFeetGame, private val initialWorld: World): ScreenAdapter() {

    private val stage = makeStage()

    private val sprites = game.assets.getSprites()
    private val styles = game.assets.getStyles()
    private val skin = game.assets.getSkin()
    private val strings = game.assets.getStrings()

    private val distanceTexture = sprites.right_sign
    private val scoreTexture = sprites.score

    private val achievements = loadAllAchievements()

    private val header = Container<Actor>()
    private val body = Container<Actor>()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

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
            makeHeading(strings["level-select.title"], sprites.logo, styles, strings) {
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

        header.actor = makeWorldSelector(
            strings,
            styles,
            world,
            onPrevious = if (world === TheOriginalWorld) null else { -> onPreviousWorld(world) },
            onNext = { onNextWorld(world) },
        )

        Gdx.app.log("LevelSelectScreen", "Viewing level list")
        body.actor = Table().also { table ->
            table.pad(UI_SPACE)

            // Later on, do some proper responsive sizing. However my first attempts struggled with
            // density independent pixel calculations (even though the math is simple, it didn't
            // seem to set proper breakpoints, perhaps because of the arbitrary math in calcDensityScaleFactor()
            // from before it occurred we could use DIPs).
            val levelsPerRow = if (Gdx.app.type == Application.ApplicationType.Desktop) 5 else 4
            val width = (stage.width - UI_SPACE * 2) / levelsPerRow
            val height = width * 3 / 4

            var x = 0
            var y = 0

            world.getLevels().forEachIndexed { i, level ->

                if (i % levelsPerRow == 0) {
                    table.row()
                    y ++
                    x = 0
                }

                table.add(makeButton(level)).width(width).height(height)

                x ++

            }
        }
    }

    private var cachedWorlds: List<World>? = null

    private suspend fun allWorlds(): List<World> {
        return cachedWorlds ?: loadAllWorlds().also { newlyLoaded ->
            cachedWorlds = newlyLoaded
        }
    }

    private fun onPreviousWorld(currentWorld: World?) {
        if (currentWorld === TheOriginalWorld) {
            return
        }

        scope.launch {
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
        }
    }

    private fun onNextWorld(currentWorld: World) {
        scope.launch {
            val worlds = performSlowOperation {
                allWorlds()
            }

            val currentIndex = worlds.indexOfFirst { it.getId() == currentWorld.getId() }
            if (currentIndex < worlds.size - 1) {
                setupStage(worlds[currentIndex + 1])
            } else {
                showComingSoon()
            }
        }
    }

    private fun createLoadingMessage(): Actor {
        return Label(strings["loading-screen.loading"], styles.label.medium)
    }

    private suspend fun <T>performSlowOperation(block: suspend () -> T): T = withContext(Dispatchers.IO) {
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
                    body.actor = createLoadingMessage()
                    body.actor.addAction(Actions.fadeIn(0.2f))
                },
            )
        )

        val result = block()

        header.actor.clearActions()
        body.actor.clearActions()

        header.actor.color.a = 1f
        body.actor.color.a = 1f

        result
    }

    private fun showComingSoon() {
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
            ).pad(UI_SPACE).expandX().fill(0.5f, 0f)
            row().pad(UI_SPACE)

            add(
                Label(strings["level-select.more-coming-soon.when-we-find-time"], styles.label.small).also { label ->
                    label.wrap = true
                    label.setAlignment(Align.center)
                }
            ).pad(UI_SPACE).expandX().fill(0.4f, 0f)
            row().pad(UI_SPACE)

            add(makeButton(strings["level-select.more-coming-soon.suggest-a-song"], styles) {
                Gdx.net.openURI("https://github.com/beat-feet/beat-feet/issues/new?title=Song%20suggestion:%20&labels=song+suggestion&body=(PLEASE%20NOTE:%20This%20game%20is%20open%20source,%20and%20all%20songs%20included%20in%20it%20must%20be%20freely%20licensed,%20for%20example,%20CC-BY)")
            }).pad(UI_SPACE)
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
        setupStage(initialWorld)
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

    private fun makeButton(level: Level): WidgetGroup {

        val isLocked = level.getUnlockRequirements().isLocked(achievements)
        val buttonStyle = if (isLocked) "locked" else "default"
        val textColor = if (isLocked) Color.GRAY else Color.WHITE

        val button = Button(skin, buttonStyle).apply {
            isDisabled = isLocked
            setFillParent(true)
            addListener(object: ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    onLevelSelected(level)
                }
            })
        }

        val labelString = if (isLocked && !level.getUnlockRequirements().isAlmostUnlocked(achievements)) "???" else level.getLabel(strings)

        val levelLabel = Label(labelString, styles.label.medium).apply {
            wrap = true
            color = textColor
            setAlignment(Align.topLeft)
        }

        val table = Table().apply {
            setFillParent(true)
            touchable = Touchable.disabled // Let the button in the background do the interactivity.
            pad(Value.percentWidth(0.125f))

            add(levelLabel).expand().fill().colspan(4)
        }

        val highScore = loadHighScore(level)

        if (isLocked) {

            val unlockDescription = Label(level.getUnlockRequirements().describeOutstandingRequirements(strings, achievements), styles.label.small)
            unlockDescription.color = textColor

            table.row()
            table.add(unlockDescription)

        } else if (highScore.exists()) {

            val distanceLabel = Label(highScore.distancePercentString(), styles.label.small)
            val scoreLabel = Label(highScore.points.toString(), styles.label.small)

            val distanceIcon = makeIcon(distanceTexture)
            val scoreIcon = makeIcon(scoreTexture)

            val iconSize = Value.percentWidth(0.75f)
            val iconSpace = Value.percentWidth(0.2f)

            table.row()
            table.add(distanceIcon).spaceRight(iconSpace).size(iconSize)
            table.add(distanceLabel)
            table.add(scoreIcon).spaceLeft(iconSpace).spaceRight(iconSpace).size(iconSize)
            table.add(scoreLabel).expandX().left()

        }

        return WidgetGroup(button, table)

    }

    fun onLevelSelected(level: Level): Boolean {
        if (level === CustomLevel) {
            val file = level.getMp3File()
            if (!file.exists()) {
                game.explainCustomSongs()
            } else {
                game.loadGame(level)
            }
        } else {
            game.loadGame(level)
        }

        return true
    }

}

