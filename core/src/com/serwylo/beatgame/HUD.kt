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

    private val viewportHeight: Float
    private val viewportWidth: Float

    private val camera: Camera

    private val padding: Float

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val textureFace: TextureRegion
    private val textureScore: TextureRegion

    init {

        val aspectRatio = Gdx.graphics.height.toFloat() / Gdx.graphics.width
        viewportWidth = 400f
        viewportHeight = viewportWidth * aspectRatio

        padding = viewportWidth / 50

        camera = OrthographicCamera(viewportWidth, viewportHeight)
        camera.translate(viewportWidth / 2, viewportHeight / 2)
        camera.update()

        batch.projectionMatrix = camera.combined

        textureFace = atlas.findRegion("character_a_face_small")
        textureScore = atlas.findRegion("score")

    }

    fun render(player: Player) {
        batch.begin()

        batch.draw(textureFace, padding, padding, padding * 1.5f, padding * 1.5f)
        font.draw(batch, player.getHealth().toString(), padding * 3, padding + 12f)

        batch.draw(textureScore, padding * 8, padding, padding * 1.5f, padding * 1.5f)
        font.draw(batch, player.getScore().toString(), padding * 10, padding + 12f)

        batch.end()
    }

}