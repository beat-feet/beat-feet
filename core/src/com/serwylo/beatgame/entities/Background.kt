package com.serwylo.beatgame.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.ParallaxCamera

class Background(private val maxSpeed: Float) : Entity {

    /**
     * Postpone initialisation until the first render, because we need to know the camera
     * viewport bounds in order to place them somewhere sensible.
     */
    private val clouds = mutableListOf<Cloud>()

    override fun update(delta: Float) {
        clouds.forEach { it.update(delta) }
    }

    override fun render(camera: ParallaxCamera, isPaused: Boolean) {

        val cloudParallaxX = 0.2f

        val r = Globals.shapeRenderer
        r.projectionMatrix = camera.calculateParallaxMatrix(cloudParallaxX, 1f)

        val bottomLeft = camera.unproject(Vector3(0f, Gdx.graphics.height.toFloat(), 0f))
        bottomLeft.x = bottomLeft.x * cloudParallaxX
        val cameraViewport = Rectangle(camera.position.x * cloudParallaxX - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight)
        val nextViewport = Rectangle(cameraViewport.x + cameraViewport.width, cameraViewport.y, cameraViewport.width, cameraViewport.height)

        if (clouds.size == 0) {
            clouds.addAll((0..20).mapIndexed { it, i ->
                val cloud = Cloud(maxSpeed)
                cloud.init(if (i < 10) cameraViewport else nextViewport)
                cloud
            })
        }

        r.color = Color.WHITE
        r.begin(ShapeRenderer.ShapeType.Filled)

        clouds.forEach {
            it.checkBoundsAndMaybeReset(cameraViewport, nextViewport)
            it.render(r)
        }

        r.end()

        r.begin(ShapeRenderer.ShapeType.Line)
        r.color = Color.CYAN
        r.rect(cameraViewport.x, cameraViewport.y, cameraViewport.width, cameraViewport.height)
        r.end()
    }

    class Cloud(private val playerSpeed: Float) {

        val position = Vector2()
        val size = Vector2()
        val velocity = Vector2()

        fun init(viewport: Rectangle) {

            position.set(
                    viewport.x + (Math.random() * viewport.width).toFloat(),
                    viewport.y + viewport.height / 2 + (Math.random() * (viewport.height / 2)).toFloat()
            )

            size.set(
                    (Math.random() * 2 + 1).toFloat(),
                    (Math.random() * 0.5 + 0.5).toFloat()
            )

            val minSpeed = playerSpeed / 40
            val maxSpeed = minSpeed * 2
            velocity.x = -(Math.random() * (maxSpeed - minSpeed) + minSpeed).toFloat()

        }

        fun update(delta: Float) {
            position.x = position.x + velocity.x * delta
        }

        /**
         * Check whether clouds have moved beyond the left of the [currentViewport]. If so, then
         * respawn them into the [nextViewport].
         */
        fun checkBoundsAndMaybeReset(currentViewport: Rectangle, nextViewport: Rectangle) {
            if (position.x + position.y < currentViewport.x) {
                init(nextViewport)
            }
        }

        fun render(shapeRenderer: ShapeRenderer) {
            shapeRenderer.ellipse(position.x, position.y, size.x, size.y, 0f, 10)
        }

    }

}