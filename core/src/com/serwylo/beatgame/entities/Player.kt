package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals
import kotlin.math.abs

class Player(
        val velocity: Vector2 = Vector2(),
        atlas: TextureAtlas
) : Entity {

    private val position = Vector2()

    /**
     * For animation purposes, record the last time we hit an obstacle. Use this to show some
     * visual feedback as to how frequently we get hit.
     */
    private var hitAnimation = -1f

    private var colour: Color = Color.BLUE

    private var jumpCount = 0

    private var walkAnimation: Animation<TextureAtlas.AtlasRegion>
    private var textureJump: TextureRegion
    private var textureHit: TextureRegion

    private var health = 100

    fun getHealth(): Int { return health }

    enum class State {
        RUNNING,
        JUMPING
    }

    private var state: State = State.RUNNING

    private val hitObstacles = mutableSetOf<Obstacle>()

    init {
        textureJump = atlas.findRegion("character_a_jump")
        textureHit = atlas.findRegion("character_a_hit")

        val texturesWalk = atlas.findRegions("character_a_walk")
        walkAnimation = Animation(0.2f, texturesWalk)
    }

    fun performJump() {

        if (jumpCount < 2 && abs(velocity.y) <= DOUBLE_JUMP_THRESHOLD) {

            velocity.y = JUMP_VELOCITY
            state = State.JUMPING
            jumpCount ++

        }

    }

    private fun sprite(): TextureRegion {

        if (hitAnimation > 0) {
            return textureHit
        }

        if (state == State.RUNNING) {
            return walkAnimation.getKeyFrame(Globals.animationTimer, true)
        }

        return textureJump

    }

    override fun render(camera: Camera) {

        val batch = Globals.spriteBatch
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(sprite(), position.x, position.y, WIDTH, HEIGHT)
        batch.end()

        val player = Globals.shapeRenderer
        player.projectionMatrix = camera.combined
        player.color = colour
        player.begin(ShapeRenderer.ShapeType.Line)
        player.rect(position.x, position.y, WIDTH, HEIGHT)
        player.end()

    }

    override fun update(delta: Float) {
        hitAnimation -= delta
        if (hitAnimation < 0f) {
            colour.set(0f, 0f, 1f, 1f)
        } else {
            val hitRange = hitAnimation / HIT_ANIMATION_DURATION
            colour.set(hitRange, 0f, 1f - hitRange, 1f)
        }

        velocity.y += GRAVITY_CONSTANT * delta

        position.x += velocity.x * delta
        position.y += velocity.y * delta

        if (position.y < 0) {
            landOnSurface(0f)
        }
    }

    fun isColliding(rect: Rectangle): Boolean {
        if (rect.x + rect.width < position.x || rect.x > position.x + WIDTH || rect.y + rect.height < position.y ) {
            return false
        }

        // Are we falling (good, we landed on an object), or moving forward into an object?
        if (position.y > rect.y + rect.height - CLIMB_THRESHOLD) {

            if (velocity.y <= 0) {
                landOnSurface(rect.y + rect.height)
            }

            return false
        }

        return true
    }

    private fun landOnSurface(height: Float) {
        state = State.RUNNING
        velocity.y = 0f
        position.y = height
        jumpCount = 0
    }

    fun hit(obstacle: Obstacle) {
        hitAnimation = HIT_ANIMATION_DURATION
        colour.set(1f, 0f, 0f, 1f)

        if (!hitObstacles.contains(obstacle)) {
            hitObstacles.add(obstacle)
            health -= 5 // TODO: Change this based on the size of the obstacle.
        }
    }

    companion object {
        const val WIDTH = 0.8f
        const val HEIGHT = 0.8f
        const val HIT_ANIMATION_DURATION = 0.1f

        /**
         * Be a little bit generous. If there are many obstacles that are almost the same height,
         * just let the player climb onto the next one if it is more or less a similar height.
         */
        const val CLIMB_THRESHOLD = 0.2f

        /**
         * Only allow double jumps when you are close to the top of your first jump. Seems to
         * offer a nice experience.
         */
        const val DOUBLE_JUMP_THRESHOLD = 3f

        const val GRAVITY_CONSTANT = -9.8f * 2f

        const val JUMP_VELOCITY = 5f
    }

}
