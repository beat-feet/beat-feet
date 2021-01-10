package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals

class SuccessPlayer(
        atlas: TextureAtlas
) : Entity {

    private val position = Vector2()
    private val spriteFront = atlas.findRegion("character_a_front")

    private var animation: Animation<TextureRegion> = Animation(
            0.2f,
            atlas.findRegion("character_a_dance_a"),
            atlas.findRegion("character_a_dance_b")
    )

    var successTime = 0f

    fun setup(position: Vector2) {
        this.position.set(position)
        this.successTime = Globals.animationTimer
    }

    override fun render(camera: Camera, isPaused: Boolean) {

        val sprite = if (Globals.animationTimer - successTime < STAND_TIME) spriteFront else animation.getKeyFrame(Globals.animationTimer, true)

        val batch = Globals.spriteBatch
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(sprite, position.x, position.y, Player.WIDTH, Player.HEIGHT)
        batch.end()

    }

    override fun update(delta: Float) {

    }

    companion object {
        private const val STAND_TIME = 1f
    }

}
