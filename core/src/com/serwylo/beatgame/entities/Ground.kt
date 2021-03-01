package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.ParallaxCamera

/**
 * Renders a series of tiles representing the ground, but be smart about only rendering enough tiles
 * to cover the left of the screen to the right.
 */
class Ground(private val sprite: TextureRegion) : Entity {

    override fun update(delta: Float) {
    }

    override fun render(batch: SpriteBatch, camera: ParallaxCamera, isPaused: Boolean) {
        val numTiles = (camera.viewportWidth / ObstacleBuilder.TILE_SIZE).toInt() + 3
        val startX = camera.unproject(Vector3(0f, 0f, 0f)).x
        val startTileX = startX - startX % ObstacleBuilder.TILE_SIZE - ObstacleBuilder.TILE_SIZE
        for (i in 0 until numTiles) {
            batch.draw(sprite, startTileX + i * ObstacleBuilder.TILE_SIZE, -ObstacleBuilder.TILE_SIZE, ObstacleBuilder.TILE_SIZE, ObstacleBuilder.TILE_SIZE)
        }
    }
}