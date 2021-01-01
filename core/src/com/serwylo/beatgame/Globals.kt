package com.serwylo.beatgame

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

object Globals {
    lateinit var shapeRenderer: ShapeRenderer
    lateinit var spriteBatch: SpriteBatch

    /**
     * Often times we want to pull out a sprite from a series of animated spites.
     * This is straightforward using the [com.badlogic.gdx.graphics.g2d.Animation] class, but that
     * requires a timer. Instead of every entity maintaining its own copy of the timer, lets do
     * it globally, and when you need to use the animator you can reach into this.
     * class from libGdx
     */
    var animationTimer = 0f
}