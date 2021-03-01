package com.serwylo.beatgame.graphics

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals

class TiledSprite(
        private val position: Vector2,
        private val sprites: Array<Array<TextureRegion?>>,
        private val offset: Vector2 = Vector2(0f, 0f)
): SpriteRenderer {

    constructor(
            position: Vector2,
            sprite: TextureRegion,
            offset: Vector2 = Vector2(0f, 0f)
    ): this(
            position,
            arrayOf(arrayOf<TextureRegion?>(sprite)),
            offset
    )

    override fun render(batch: SpriteBatch) {
        sprites.forEachIndexed { y, rows ->
            rows.forEachIndexed { x, textureRegion ->
                if (textureRegion != null) {
                    batch.draw(textureRegion, position.x - offset.x + x * TILE_SIZE, position.y - offset.y + y * TILE_SIZE, TILE_SIZE, TILE_SIZE)
                }
            }
        }
    }

    companion object {
        const val TILE_SIZE = 0.5f
    }

}