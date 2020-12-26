package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.entities.Ground
import com.serwylo.beatgame.entities.Player
import com.serwylo.beatgame.entities.Level
import com.serwylo.beatgame.entities.Obstacle

class PlatformGameScreen(
        private val game: BeatGame,
        private val level: Level
) : ScreenAdapter(), ContactListener {

    private val camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT).apply {
        translate(VIEWPORT_X, -VIEWPORT_Y, 0f)
        update()
    }

    private val player: Player
    private val world = World(Vector2(0f, -10f), true)

    init {

        Box2D.init()

        player = Player(world)
        Ground(level.heightMap.size * SCALE_X).init(world)
        level.features.forEach { Obstacle(it).init(world, SCALE_X) }

        world.setContactListener(this)

    }

    override fun show() {

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE) {
                    player.performJump()
                    return true
                } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                }

                return false
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                player.performJump()
                return true
            }
        }

        level.music.play()
    }

    override fun hide() {
        level.dispose()
        world.dispose()

        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun render(delta: Float) {
        camera.translate(delta * SCALE_X, 0f)
        camera.update()

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // level.render(camera, Rectangle(player.getPosition() - VIEWPORT_X, -VIEWPORT_Y, VIEWPORT_WIDTH, VIEWPORT_HEIGHT))
        player.render(camera)

        Globals.box2DRenderer.render(world, camera.combined)

        stepPhysics(delta)
    }

    private var accumulator = 0f

    /**
     * https://github.com/libgdx/libgdx/wiki/Box2d#stepping-the-simulation
     */
    private fun stepPhysics(deltaTime: Float) {
        val frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        while (accumulator >= 1f / 60f) {
            world.step(1f / 60f, 6, 2)
            accumulator -= 1f / 60f
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

        const val VIEWPORT_X = -5f
        const val VIEWPORT_Y = -2f
        const val VIEWPORT_WIDTH = 20f
        const val VIEWPORT_HEIGHT = 10f

    }

    override fun beginContact(contact: Contact?) {
        if (contact == null) {
            return
        }

        val a = contact.fixtureA.body.userData
        val b = contact.fixtureB.body.userData

        if (a is Player) {
            a.beginContact(contact.fixtureB)
        } else if (b is Player) {
            b.beginContact(contact.fixtureA)
        }
    }

    override fun endContact(contact: Contact?) {}
    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}
    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}

}