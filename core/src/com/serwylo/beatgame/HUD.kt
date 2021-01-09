package com.serwylo.beatgame

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.serwylo.beatgame.entities.Player
import com.serwylo.beatgame.graphics.makeCamera

class HUD(atlas: TextureAtlas) {

    private val camera: OrthographicCamera = makeCamera(400, 300)

    private val padding: Float

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val textureHeartFull: TextureRegion
    private val textureHeartHalf: TextureRegion
    private val textureHeartEmpty: TextureRegion
    private val textureScore: TextureRegion
    private val textureDistance: TextureRegion

    init {

        camera.translate(camera.viewportWidth / 2, camera.viewportHeight / 2)
        camera.update()

        padding = camera.viewportWidth / 50

        batch.projectionMatrix = camera.combined

        textureHeartFull = atlas.findRegion("heart")
        textureHeartHalf = atlas.findRegion("heart_half")
        textureHeartEmpty = atlas.findRegion("heart_empty")
        textureScore = atlas.findRegion("score")
        textureDistance = atlas.findRegion("right_sign")

    }

    fun render(distancePercent: Float, player: Player) {
        batch.begin()

        val heart = if (player.getHealth() > 50) textureHeartFull else if (player.getHealth() > 20) textureHeartHalf else textureHeartEmpty
        batch.draw(heart, padding, padding, padding * 1.5f, padding * 1.5f)
        font.draw(batch, player.getHealth().toString(), padding * 3, padding + 12f)

        batch.draw(textureDistance, padding * 8, padding, padding * 1.5f, padding * 1.5f)
        font.draw(batch, (distancePercent * 100).toInt().toString() + "%", padding * 10, padding + 12f)

        val multiplier = if (player.scoreMultiplier <= 1) "" else " x ${player.scoreMultiplier}"

        batch.draw(textureScore, padding * 15, padding, padding * 1.5f, padding * 1.5f)
        font.draw(batch, "${player.getScore()}$multiplier", padding * 17, padding + 12f)

        batch.end()
    }

}