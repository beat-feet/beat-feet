package com.serwylo.beatgame.graphics

import com.badlogic.gdx.graphics.g2d.SpriteBatch

class LayeredTiledSprite(private val layers: Array<TiledSprite>): SpriteRenderer {

    override fun render(batch: SpriteBatch) {
        layers.forEach { it.render(batch) }
    }

}