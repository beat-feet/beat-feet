package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.serwylo.beatgame.entities.Player

object HUD {

    private const val VIEWPORT_WIDTH = 400f
    private val VIEWPORT_HEIGHT: Float

    private val camera: Camera

    private val padding: Float

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    init {

        val aspectRatio = Gdx.graphics.height.toFloat() / Gdx.graphics.width
        VIEWPORT_HEIGHT = VIEWPORT_WIDTH * aspectRatio

        padding = VIEWPORT_WIDTH / 25

        camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
        camera.translate(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2)
        camera.update()

        batch.projectionMatrix = camera.combined

    }

    fun render(player: Player) {
        batch.begin()
        font.draw(batch, player.getHealth().toString(), padding, padding + 5f)
        batch.end()
    }

}