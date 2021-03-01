package com.serwylo.beatgame.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.ParallaxCamera

class Background(private val sprites: Assets.Sprites, private val maxSpeed: Float) : Entity {

    /**
     * Postpone initialisation until the first render, because we need to know the camera
     * viewport bounds in order to place them somewhere sensible.
     */
    private val clouds = mutableListOf<Cloud>()

    override fun update(delta: Float) {
        clouds.forEach { it.update(delta) }
    }

    override fun render(batch: SpriteBatch, camera: ParallaxCamera, isPaused: Boolean) {

        val cloudParallaxX = 0.2f

        batch.projectionMatrix = camera.calculateParallaxMatrix(cloudParallaxX, 1f)

        val bottomLeft = camera.unproject(Vector3(0f, Gdx.graphics.height.toFloat(), 0f))
        bottomLeft.x = bottomLeft.x * cloudParallaxX
        val cameraViewport = Rectangle(camera.position.x * cloudParallaxX - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight)
        val nextViewport = Rectangle(cameraViewport.x + cameraViewport.width, cameraViewport.y, cameraViewport.width, cameraViewport.height)

        if (clouds.size == 0) {
            clouds.addAll((0..20).mapIndexed { it, i ->
                val cloud = Cloud(sprites, maxSpeed)
                cloud.init(if (i < 10) cameraViewport else nextViewport)
                cloud
            })
        }

        clouds.forEach {
            it.checkBoundsAndMaybeReset(cameraViewport, nextViewport)
            it.render(batch)
        }
    }

    class Cloud(private val sprites: Assets.Sprites, private val playerSpeed: Float) {

        var sprite = Sprite()
        val velocity = Vector2()

        fun init(viewport: Rectangle) {

            val minSpeed = playerSpeed / 40
            val maxSpeed = minSpeed * 2
            velocity.x = -(Math.random() * (maxSpeed - minSpeed) + minSpeed).toFloat()

            val region = arrayOf(
                sprites.cloud_a,
                sprites.cloud_b,
                sprites.cloud_c,
                sprites.cloud_d,
                sprites.cloud_e,
                sprites.cloud_f,
                sprites.cloud_g,
                sprites.cloud_h,
                sprites.cloud_i,
                sprites.cloud_j
            ).random()

            sprite.setRegion(region)
            sprite.setSize(region.originalWidth * 0.01f, region.originalHeight * 0.01f)

            sprite.setPosition(
                viewport.x + (Math.random() * viewport.width).toFloat(),
                viewport.y + viewport.height / 3 * 2 + (Math.random() * (viewport.height / 3)).toFloat()
            )

        }

        fun update(delta: Float) {
            sprite.x = sprite.x + velocity.x * delta
        }

        /**
         * Check whether clouds have moved beyond the left of the [currentViewport]. If so, then
         * respawn them into the [nextViewport].
         */
        fun checkBoundsAndMaybeReset(currentViewport: Rectangle, nextViewport: Rectangle) {
            if (sprite.x + sprite.width < currentViewport.x) {
                init(nextViewport)
            }
        }

        fun render(batch: SpriteBatch) {
            sprite.draw(batch)
        }

    }

}