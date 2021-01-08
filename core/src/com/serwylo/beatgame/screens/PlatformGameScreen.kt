package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.HUD
import com.serwylo.beatgame.audio.features.Feature
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.entities.*
import com.serwylo.beatgame.graphics.calcDensityScaleFactor
import com.serwylo.beatgame.graphics.makeCamera


class PlatformGameScreen(
        private val game: BeatGame,
        private val world: World
) : ScreenAdapter() {

    private val camera = makeCamera(20, 10, calcDensityScaleFactor())
    private lateinit var hud: HUD
    private lateinit var obstacles: List<Obstacle>

    private lateinit var ground: Ground
    private lateinit var player: Player
    private lateinit var deadPlayer: DeadPlayer

    private var isInitialised = false

    private var atlas: TextureAtlas? = null

    private var state = State.PENDING
    private var startTime = 0f
    private var deathTimeTime = 0f

    private var prePauseState: State = state

    enum class State {
        PENDING,
        PAUSED,
        WARMING_UP,
        PLAYING,
        DYING,
    }

    override fun show() {

        isInitialised = false

        atlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))

        obstacles = generateObstacles(atlas!!, world.features)

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE) {
                    if (state == State.PAUSED) {
                        resume()
                    } else {
                        player.performJump()
                    }
                    return true
                } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                } else if (keycode == Input.Keys.P) {
                    if (state == State.PAUSED) {
                        resume()
                    } else {
                        pause()
                    }
                    return true
                }

                return false
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                player.performJump()
                return true
            }
        }

        hud = HUD(atlas!!)

        camera.translate(camera.viewportWidth / 4, camera.viewportHeight / 5, 0f)
        camera.update()

        player = Player(Vector2(SCALE_X, 0f), atlas!!)
        deadPlayer = DeadPlayer(atlas!!)

        ground = ObstacleBuilder.makeGround(atlas!!)

        Globals.animationTimer = 0f

        isInitialised = true
    }

    override fun hide() {

        world.dispose()

        atlas?.dispose()
        atlas = null

        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun render(delta: Float) {
        if (!isInitialised) {
            return
        }

        Globals.animationTimer += delta

        processInput()
        updateEntities(delta)
        renderEntities(delta)
        hud.render(player)
    }

    private fun processInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (state == State.PENDING) {
                state = State.WARMING_UP
                startTime = Globals.animationTimer
            } else if (state == State.PLAYING || state == State.WARMING_UP) {
                player.performJump()
            }
        }
    }

    private fun updateEntities(delta: Float) {

        checkCollisions()

        if (state == State.PENDING || state == State.PAUSED) {

            // Do nothing, we just need to animate the running player (which happens in the render loop).
            return

        }

        if (state == State.PLAYING || state == State.WARMING_UP) {

            player.update(delta)
            scrollCamera(delta)

            if (state == State.WARMING_UP && Globals.animationTimer - startTime > WARM_UP_TIME) {
                startGame()
            }

            if (player.getHealth() <= 0) {

                state = State.DYING
                deadPlayer.setup(player.position)
                deathTimeTime = Globals.animationTimer

            }

        } else if (state == State.DYING) {

            if (Globals.animationTimer - deathTimeTime < DEATH_TIME) {

                camera.translate(0f, delta * DeadPlayer.FLOAT_SPEED / 8)
                camera.zoom += DEATH_ZOOM_RATE * delta
                camera.update()

            } else {

                endGame()

            }

        }

    }

    private fun scrollCamera(delta: Float) {
        camera.translate(delta * SCALE_X, 0f)
        camera.update()
    }

    private fun renderEntities(delta: Float) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        ground.render(camera, state == State.PAUSED)
        obstacles.forEach { it.render(camera, state == State.PAUSED) }

        if (state == State.DYING) {
            deadPlayer.render(camera, state == State.PAUSED)
        } else {
            player.render(camera, state == State.PAUSED)
        }
    }

    private fun startGame() {
        state = State.PLAYING
        world.music.play()
    }

    private fun endGame() {
        world.music.stop()
        game.endGame(player.getScore())
    }

    override fun pause() {
        super.pause()

        prePauseState = state
        world.music.pause()
        state = State.PAUSED
    }

    override fun resume() {
        super.resume()

        world.music.play()
        state = prePauseState
    }

    private fun checkCollisions() {
        obstacles.forEach {
            if (player.isColliding(it.rect)) {
                player.hit(it)
            }
        }
    }

    companion object {

        /**
         * To convert horizontal units from seconds -> metres. That sounds a bit odd, but this is a side
         * scrolling game where features appear at very specific time points, and the screen scrolls
         * at a consistent rate. Therefore it does kind-of-in-an-odd-way make sense to multiple a seconds
         * value to get a horizontal offset in metres.
         *
         * All of the level generation starts with music, which is measured in samples at a particular
         * sample rate.
         *
         * This is then converted into specific time points in seconds, so that regardless of the sample
         * rate of a particular song, all songs produce features of the same duration.
         *
         * The final step is to convert seconds into measurements on screen. This is used for that.
         */
        const val SCALE_X = 5f

        /**
         * Less than this distance between obstacles, and we will merge them together (i.e. increase
         * the size of the one on the left until it reaches the one on the right).
         */
        private const val OBSTACLE_GAP_THRESHOLD = 0.175f

        /**
         * The features extracted from audio line up with exactly when a particular feature of the
         * music is detected. The game is more fun when this lines up with when you'd expect the
         * player to have to jump in order to avoid the feature (more rhythmic that way), so we
         * offset each feature by this many seconds.
         */
        private const val FEATURE_START_TIME_OFFSET = -0.1f

        /**
         * Once the game starts, the player runs infinitely until the player jumps for the first
         * time. After that, wait this long before starting the song.
         */
        private const val WARM_UP_TIME = 3f


        /**
         * How long after dying before we move onto the game end screen.
         * The animation of death is handled differently, managed by the [Player] class.
         */
        private const val DEATH_TIME = 5f

        private const val DEATH_ZOOM_RATE = -0.015f

        private fun generateObstacles(atlas: TextureAtlas, features: List<Feature>): List<Obstacle> {
            val rects = features.map {
                Rectangle(
                        (it.startTimeInSeconds + FEATURE_START_TIME_OFFSET + WARM_UP_TIME) * SCALE_X,
                        0f,
                        it.durationInSeconds * SCALE_X,
                        (it.strength * Obstacle.STRENGTH_TO_HEIGHT).coerceAtLeast(Obstacle.MIN_HEIGHT)
                )
            }

            val toRemove = mutableSetOf<Rectangle>()
            var i = 0
            while (i < rects.size - 1) {

                val current = rects[i]

                // Continue merging subsequent items in succession if they are of the same height
                // and close enough x distance.
                var nextIndex = i + 1
                while (nextIndex < rects.size) {

                    val next = rects[nextIndex]

                    val distanceToNext = next.x - current.x - current.width
                    if (distanceToNext < OBSTACLE_GAP_THRESHOLD && current.height == next.height) {
                        current.width += distanceToNext + next.width

                        // Don't start processing this obstacle in the next high level loop, because
                        // it has been merged into the current one.
                        i ++

                        // Also completely remove it later on.
                        toRemove.add(next)
                    }

                    nextIndex ++

                }

                i ++
            }

            return rects.filter { !toRemove.contains(it) }
                    .map { ObstacleBuilder.makeObstacle(it, atlas) }

        }

    }

}