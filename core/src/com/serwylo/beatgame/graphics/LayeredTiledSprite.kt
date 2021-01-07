package com.serwylo.beatgame.graphics

class LayeredTiledSprite(private val layers: Array<TiledSprite>): SpriteRenderer {

    override fun render() {
        layers.forEach { it.render() }
    }

}