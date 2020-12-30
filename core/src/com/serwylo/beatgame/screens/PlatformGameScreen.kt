package com.serwylo.beatgame.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.game.entities.makeCamera
import com.serwylo.beatgame.game.entities.makeGround
import com.serwylo.beatgame.game.entities.makeObstacle
import com.serwylo.beatgame.game.entities.makePlayer
import com.serwylo.beatgame.game.systems.*

class PlatformGameScreen(
        private val game: BeatGame,
        private val world: World,
        private val shapeRenderer: ShapeRenderer
) : ScreenAdapter() {

    private val engine = Engine()
    private var initialised = false

    override fun show() {

        engine.addEntity(makeCamera(engine))
        engine.addEntity(makeGround(engine))
        engine.addEntity(makePlayer(engine))

        world.features.forEach {
            engine.addEntity(makeObstacle(engine, it, SCALE_X))
        }

        engine.addSystem(GravitySystem())
        engine.addSystem(ScrollingSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(RenderingSystem(shapeRenderer))
        engine.addSystem(PlayerControlSystem())
        engine.addSystem(CollisionSystem())
        engine.addSystem(CollisionResolutionSystem())

        initialised = true

        // world.music.play()
    }

    override fun hide() {
        world.dispose()
    }

    override fun render(delta: Float) {
        if (initialised) {
            engine.update(delta)
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
        const val SCALE_X = 15f

    }

}