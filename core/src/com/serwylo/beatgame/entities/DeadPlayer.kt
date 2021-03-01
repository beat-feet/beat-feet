package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.ParallaxCamera

class DeadPlayer(
        sprites: Assets.Sprites
) : Entity {

    private val position = Vector2()
    private var animation: Animation<TextureRegion> = Animation(
            0.5f,
            sprites.character_a_hit,
            sprites.character_a_duck,
            sprites.ghost,
            sprites.ghost_x
    )

    var deathTime = 0f

    fun setup(position: Vector2) {
        this.position.set(position)
        this.deathTime = Globals.animationTimer
    }

    override fun render(batch: SpriteBatch, camera: ParallaxCamera, isPaused: Boolean) {

        val sprite = animation.getKeyFrame(Globals.animationTimer - deathTime, false)

        val y = if (getDuration() <= KEYFRAME_SPEED * 2) position.y else {
            position.y + (getDuration() - (KEYFRAME_SPEED * 2)) * FLOAT_SPEED
        }

        batch.draw(sprite, position.x, y, Player.WIDTH, Player.HEIGHT)

    }

    private fun getDuration() = Globals.animationTimer - deathTime

    override fun update(delta: Float) {

    }

    companion object {

        const val KEYFRAME_SPEED = 0.5f
        const val FLOAT_SPEED = 0.2f

    }

}
