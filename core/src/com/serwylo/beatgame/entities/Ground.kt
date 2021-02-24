package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.ParallaxCamera

class Ground(private val sprite: TextureRegion) : Entity {

    override fun update(delta: Float) {
    }

    override fun render(camera: ParallaxCamera, isPaused: Boolean) {
        val batch = Globals.spriteBatch
        batch.begin()
        val numTiles = (camera.viewportWidth / ObstacleBuilder.TILE_SIZE).toInt() + 3
        val startX = camera.unproject(Vector3(0f, 0f, 0f)).x
        val startTileX = startX - startX % ObstacleBuilder.TILE_SIZE - ObstacleBuilder.TILE_SIZE
        for (i in 0 until numTiles) {
            batch.draw(sprite, startTileX + i * ObstacleBuilder.TILE_SIZE, -ObstacleBuilder.TILE_SIZE, ObstacleBuilder.TILE_SIZE, ObstacleBuilder.TILE_SIZE)
        }
        batch.end()

        /*val r = Globals.shapeRenderer

        r.projectionMatrix = camera.combined
        r.color = Color.GREEN
        r.begin(ShapeRenderer.ShapeType.Line)
        r.line(-100f, 0f, 10000f, 0f)
        r.end()*/
    }
}