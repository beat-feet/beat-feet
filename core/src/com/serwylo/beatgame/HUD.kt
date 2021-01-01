package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.serwylo.beatgame.entities.Player

class HUD(atlas: TextureAtlas) {

    private val camera: Camera

    private val padding: Float

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val textureFace: TextureRegion

    init {

        val aspectRatio = Gdx.graphics.height.toFloat() / Gdx.graphics.width
        val viewportWidth = 400f
        val viewportHeight = viewportWidth * aspectRatio

        padding = viewportWidth / 35

        camera = OrthographicCamera(viewportWidth, viewportHeight)
        camera.translate(viewportWidth / 2, viewportHeight / 2)
        camera.update()

        batch.projectionMatrix = camera.combined

        textureFace = atlas.findRegion("character_a_face_small")

    }

    fun render(player: Player) {
        batch.begin()
        batch.draw(textureFace, padding, padding, padding, padding)
        font.draw(batch, player.getHealth().toString(), padding * 3, padding + 12f)
        batch.end()
    }

}