package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.ParallaxCamera
import com.serwylo.beatgame.levels.Score
import kotlin.math.abs

class Player(
        val score: Score,
        private val velocity: Vector2 = Vector2(),
        sprites: Assets.Sprites,
        particles: Assets.Particles
) : Entity {

    val position = Vector2()

    /**
     * For animation purposes, record the last time we hit an obstacle. Use this to show some
     * visual feedback as to how frequently we get hit.
     */
    var hitAnimation = -1f

    /**
     * For one frame, we will record a hit has been performed. This allows the main game to interrogate
     * and respond (e.g. by starting particle effects, shaking camera, vibrating, etc).
     */
    var justHitDamage = 0

    private var jumpCount = 0

    private val textureJump = sprites.character_a_jump
    private val textureJump2 = sprites.character_a_duck
    private val textureHit = sprites.character_a_hit
    private val walkAnimation: Animation<TextureRegion> = Animation(0.2f, sprites.character_a_walk)
    private val deathAnimation = Animation(
            0.5f,
            textureHit,
            sprites.character_a_duck,
            sprites.ghost,
            sprites.ghost_x
    )

    private var health = 100
    private var shield = SHIELD_INITIAL_AMOUNT

    private val jumpParticles = particles.jump

    fun getHealth(): Int { return health }
    fun getShield(): Int { return shield }

    enum class State {
        RUNNING,
        JUMPING,
        DEAD
    }

    var state: State = State.RUNNING

    private val hitObstacles = mutableSetOf<Obstacle>()

    private var deathTime = 0f
    private var lastMultiplerTime = 0f

    fun performJump() {

        if (jumpCount < 2 && abs(velocity.y) <= DOUBLE_JUMP_THRESHOLD) {

            if (jumpCount == 0) {
                jumpParticles.reset()
            }

            velocity.y = JUMP_VELOCITY
            state = State.JUMPING
            jumpCount ++
            currentlyOnObstacles.clear()

            if (score.getMultiplier() >= MIN_MULTIPLIER_FOR_RAINBOW) {
                jumpParticles.emitters.forEach {
                    it.emission.highMin = (score.getMultiplier() - MIN_MULTIPLIER_FOR_RAINBOW) * 10
                    it.emission.highMax = (score.getMultiplier() - MIN_MULTIPLIER_FOR_RAINBOW) * 15
                    it.duration = (score.getMultiplier() - MIN_MULTIPLIER_FOR_RAINBOW) * 15
                    it.maxParticleCount = ((score.getMultiplier() - MIN_MULTIPLIER_FOR_RAINBOW) * 25).toInt()
                }
                jumpParticles.start()

                val increaseJumpPower = ((score.getMultiplier() - MIN_MULTIPLIER_FOR_RAINBOW) * SHIELD_EARNED_PER_JUMP).coerceAtMost(SHIELD_MAX_INCREASE).toInt()
                shield = (shield + increaseJumpPower).coerceAtMost(SHIELD_MAX_AMOUNT)
            }

        }

    }

    private fun sprite(isPaused: Boolean): TextureRegion {

        if (state == State.DEAD) {
            return deathAnimation.getKeyFrame(Globals.animationTimer - deathTime, false)
        }

        if (hitAnimation > 0) {
            return textureHit
        }

        if (state == State.RUNNING) {
            if (isPaused) {
                return walkAnimation.getKeyFrame(0f)
            } else {
                return walkAnimation.getKeyFrame(Globals.animationTimer, true)
            }
        }

        return if (jumpCount == 2) {
            textureJump2
        } else {
            textureJump
        }

    }

    override fun render(batch: SpriteBatch, camera: ParallaxCamera, isPaused: Boolean) {
        batch.draw(sprite(isPaused), position.x, position.y, WIDTH, HEIGHT)

        if (score.getMultiplier() > MIN_MULTIPLIER_FOR_RAINBOW) {
            jumpParticles.draw(batch)
        }
    }

    override fun update(delta: Float) {
        if (state == State.DEAD) {
            return
        }

        hitAnimation -= delta

        velocity.y += GRAVITY_CONSTANT * delta

        position.x += velocity.x * delta
        position.y += velocity.y * delta

        if (position.y < 0) {
            landOnSurface(0f)
        }

        if (state == State.RUNNING && score.getMultiplier() > 1f && Globals.animationTimer - lastMultiplerTime > MULTIPLIER_GRACE_PERIOD) {
            score.resetMultiplier()
            lastMultiplerTime = 0f
        }

        if (score.getMultiplier() > MIN_MULTIPLIER_FOR_RAINBOW) {
            jumpParticles.emitters.forEach {
                it.setPosition(position.x + WIDTH / 2, position.y)
            }
            jumpParticles.update(delta)
        }

    }

    private val currentlyOnObstacles = mutableSetOf<Rectangle>()

    fun isColliding(rect: Rectangle): Boolean {
        if (
                // Don't check the full width width of the player against the building, because
                // the feet only take up less than 100%. Therefore allow the player to drop off
                // objects when stepping off them, even though their head is still above the object.
                rect.x > position.x + WIDTH / 6 * 5 ||
                rect.x + rect.width < position.x + WIDTH / 6 ||

                rect.y + rect.height < position.y
        ) {
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
        // If we were jumping and we landed on a surface (even if the ground), then add a multiplier.
        // This means that falling from a building doesn't count towards multipliers.
        if (state == State.JUMPING) {
            score.increaseMultiplier()
            lastMultiplerTime = Globals.animationTimer
        }

        state = State.RUNNING
        velocity.y = 0f
        position.y = height
        jumpCount = 0

        // No longer emit any more particles.
        jumpParticles.emitters.forEach {
            // This isn't the cleanest way to finish it, but seems to work.
            // Essentially tell the timer that it has been running for a really long time, so it
            // will stop emitting any further.
            it.durationTimer = 1000f
        }
    }

    fun hit(obstacle: Obstacle) {

        hitAnimation = HIT_ANIMATION_DURATION

        if (!hitObstacles.contains(obstacle)) {

            hitObstacles.add(obstacle)

            score.resetMultiplier()
            lastMultiplerTime = 0f

            // Bigger obstacles cause more damage.
            val damage = (obstacle.rect.area() * AREA_TO_DAMAGE).toInt().coerceAtLeast(MIN_DAMAGE)

            // If we are jumping upward and hit the obstacle above half way then we can visually
            // it doesn't look like such a big deal when you hit it, so reduce the damage accordingly.
            val scaledDamage = if (velocity.y > 0) {
                val scale = 1 -  (position.y - obstacle.rect.y) / obstacle.rect.height
                (damage * scale).toInt().coerceAtLeast(MIN_DAMAGE)
            } else {
                damage
            }

            // Take damage from the shield first (at a greater rate than is applied to the player health)
            shield -= scaledDamage * SHIELD_DAMAGE_RATIO
            if (shield < 0) {
                // Any left over damage that the shield couldn't absorbe is applied back to the health.
                val nonShieldDamage = -shield / SHIELD_DAMAGE_RATIO
                health -= nonShieldDamage
                shield = 0
            }

            justHitDamage = scaledDamage

            if (health <= 0) {
                health = 0
                deathTime = Globals.animationTimer
                velocity.set(0f, 0f)
                state = State.DEAD
            }

        }

    }

    fun clearHit() {
        justHitDamage = 0
    }

    companion object {

        const val WIDTH = 0.8f
        const val HEIGHT = 0.8f

        const val HIT_ANIMATION_DURATION = 0.1f

        /**
         * Be a little bit generous. If there are many obstacles that are almost the same height,
         * just let the player climb onto the next one if it is more or less a similar height.
         */
        const val CLIMB_THRESHOLD = 0.4f

        /**
         * Only allow double jumps when you are close to the top of your first jump. Seems to
         * offer a nice experience.
         */
        const val DOUBLE_JUMP_THRESHOLD = 10f

        const val SHIELD_INITIAL_AMOUNT = 40
        const val SHIELD_MAX_AMOUNT = 100

        /**
         * This will increase as a factor of the current multiplier (but not above [SHIELD_MAX_INCREASE])
         */
        const val SHIELD_EARNED_PER_JUMP = 2

        /**
         * Even if you have a combo of 1000x, don't give more than this amount of fhield per jump.
         */
        const val SHIELD_MAX_INCREASE = 10f

        /**
         * The multiplier applied to damage used to subtract from the shield, compared to what would
         * be subtracted if it was taken from the players health. The shield damages more easily
         * than health, so the damage it receives should be greater.
         */
        const val SHIELD_DAMAGE_RATIO = 5

        const val GRAVITY_CONSTANT = -9.8f * 4f

        const val JUMP_VELOCITY = 10f

        /**
         * When hitting an obstacle, multiply the area by this in order to figure out how much damage to do.
         */
        const val AREA_TO_DAMAGE = 6f

        const val MIN_DAMAGE = 1

        /**
         * Once the multiplier is increased, then allow the player to run for this many seconds
         * before restarting the multiplier again.
         */
        const val MULTIPLIER_GRACE_PERIOD = 0.5f

        /**
         * Show rainbows coming out of the character when jumping, if the multiplier is above this value.
         */
        const val MIN_MULTIPLIER_FOR_RAINBOW = 1f

    }

}
