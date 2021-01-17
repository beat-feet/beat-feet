package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.screens.PlatformGameScreen

class Background(private val heightMap: Array<Vector2>) : Entity {

    private var previousStartIndex = 0

    companion object {

        private const val OFFSET = PlatformGameScreen.WARM_UP_TIME * PlatformGameScreen.SCALE_X

        private val TERRAIN_COLOUR = Color(0f, 0.15f, 0f, 1f)
        private val TERRAIN_CAP_COLOUR = Color(0f, 0.3f, 0f, 1f)
        private val SKY_COLOUR = Color(0f, 0.0f, 0.12f, 1f)

        /**
         * Round to the nearest block of this size so that it looks equally as pixelated as other parts
         * of the game.
         */
        private const val CELL_SIZE = ObstacleBuilder.TILE_SIZE / 10

    }

    override fun update(delta: Float) {
    }

    override fun render(camera: Camera, isPaused: Boolean) {
        val r = Globals.shapeRenderer
        r.projectionMatrix = camera.combined
        // r.translate(PlatformGameScreen.WARM_UP_TIME * PlatformGameScreen.SCALE_X, 0f, 0f)
        r.begin(ShapeRenderer.ShapeType.Filled)

        val cameraLeft = camera.unproject(Vector3(0f, 0f, 0f)).x
        val startX = cameraLeft + OFFSET
        val endX = startX + camera.viewportWidth

        /*

            A * __
              |    -- __
              |          * B
              |          |
            D *----------* C

           To fill in this polygon, pick two adjacent points in the heightmap A, B...
           Draw two filled triangles:
            - A, B, D
            - B, C, D

         */

        val scaleX = PlatformGameScreen.SCALE_X
        val scaleY = 5f

        r.color = SKY_COLOUR
        r.rect(cameraLeft, 0f, camera.viewportWidth, camera.viewportHeight)

        for (i in previousStartIndex until heightMap.size - 1) {

            val aX = heightMap[i].x * scaleX + OFFSET
            val aY = heightMap[i].y * scaleY - scaleY / 4
            val bX = heightMap[i + 1].x * scaleX + OFFSET

            val tilesHigh = (aY / CELL_SIZE).toInt()
            val actualHeight = (tilesHigh * CELL_SIZE).coerceAtLeast(0f)

            // Advance this counter forward so that next time we don't draw anything that is off to
            // the left of the screen.
            if (cameraLeft in aX..bX) {
                previousStartIndex = i
            }

            r.color = TERRAIN_CAP_COLOUR
            r.rect(aX, actualHeight, bX - aX, CELL_SIZE)

            r.color = TERRAIN_COLOUR
            r.rect(aX, 0f, bX - aX, actualHeight)

            // Don't draw anything off the right of the screen.
            if (bX > endX) {
                break
            }

        }

        r.end()

    }

}
