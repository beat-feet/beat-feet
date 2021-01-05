package com.serwylo.beatgame.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals

class TiledSprite(
        private val position: Vector2,
        private val sprites: Array<Array<TextureRegion?>>,
        private val offset: Vector2 = Vector2(0f, 0f)
) {

    constructor(
            position: Vector2,
            sprite: TextureRegion,
            offset: Vector2 = Vector2(0f, 0f)
    ): this(
            position,
            arrayOf(arrayOf<TextureRegion?>(sprite)),
            offset
    )

    fun render() {
        val batch = Globals.spriteBatch
        batch.begin()
        sprites.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, textureRegion ->
                if (textureRegion != null) {
                    batch.draw(textureRegion, position.x - offset.x + x * TILE_SIZE, position.y - offset.y + y * TILE_SIZE, TILE_SIZE, TILE_SIZE)
                }
            }
        }
        batch.end()

        /*
        val r = Globals.shapeRenderer
        r.color = Color.RED
        r.begin(ShapeRenderer.ShapeType.Line)
        sprites.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, textureRegion ->
                if (textureRegion != null) {
                    r.rect(position.x + x * TILE_SIZE, position.y + y * TILE_SIZE, TILE_SIZE, TILE_SIZE)
                }
            }
        }
        r.end()
         */
    }

    companion object {
        const val TILE_SIZE = 0.5f
    }

}