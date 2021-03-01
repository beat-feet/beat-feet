package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.serwylo.beatgame.graphics.ParallaxCamera

interface Entity {
    fun update(delta: Float)

    /**
     * The delta here is really just used for animations. Actual logic dependent on the passage
     * of time should use [update].
     */
    fun render(batch: SpriteBatch, camera: ParallaxCamera, isPaused: Boolean)
}