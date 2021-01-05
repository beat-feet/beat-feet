package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals

class DeadPlayer(
        atlas: TextureAtlas
) : Entity {

    private val position = Vector2()
    private var animation: Animation<TextureRegion> = Animation(
            0.5f,
            atlas.findRegion("character_a_hit"),
            atlas.findRegion("character_a_duck"),
            atlas.findRegion("ghost"),
            atlas.findRegion("ghost_x")
    )

    var deathTime = 0f

    fun setup(position: Vector2) {
        this.position.set(position)
        this.deathTime = Globals.animationTimer
    }

    override fun render(camera: Camera, isPaused: Boolean) {

        val sprite = animation.getKeyFrame(Globals.animationTimer - deathTime, false)

        val y = if (getDuration() <= KEYFRAME_SPEED * 2) position.y else {
            position.y + (getDuration() - (KEYFRAME_SPEED * 2)) * FLOAT_SPEED
        }

        val batch = Globals.spriteBatch
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(sprite, position.x, y, Player.WIDTH, Player.HEIGHT)
        batch.end()

    }

    private fun getDuration() = Globals.animationTimer - deathTime

    override fun update(delta: Float) {

    }

    companion object {

        const val KEYFRAME_SPEED = 0.5f
        const val FLOAT_SPEED = 0.2f

    }

}
