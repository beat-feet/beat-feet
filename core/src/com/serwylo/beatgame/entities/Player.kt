package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.screens.PlatformGameScreen

class Player(private val calcHeightAtPosition: (pos: Float) -> Float) {

    private var position = 0f

    fun getPosition() = position

    private var jumpTime: Float = -1f
    private var maxJumpTime: Float = 0.2f
    private var jumpHeight: Float = 1f
    private var jumpStartHeight: Float = -1f

    fun performJump() {
        jumpTime = 0f
        jumpStartHeight = calcHeightAtPosition(position)
    }

    fun render(camera: OrthographicCamera) {

        val player = ShapeRenderer(50)
        player.projectionMatrix = camera.combined
        player.color = Color.CYAN
        player.begin(ShapeRenderer.ShapeType.Filled)
        player.circle(
                position,
                if (jumpTime >= 0) jumpStartHeight + jumpHeight + 0.3f else calcHeightAtPosition(position) + 0.3f,
                0.3f,
                8)
        player.end()

    }

    fun update(delta: Float) {

        position += delta * PlatformGameScreen.SCALE_X

        if (jumpTime >= 0) {
            jumpTime += delta

            if (jumpTime > maxJumpTime) {
                jumpTime = -1f
                jumpStartHeight = -1f
            }
        }

    }

}