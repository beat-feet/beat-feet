package com.serwylo.beatgame.entities

import com.serwylo.beatgame.graphics.ParallaxCamera

interface Entity {
    fun update(delta: Float)

    /**
     * The delta here is really just used for animations. Actual logic dependent on the passage
     * of time should use [update].
     */
    fun render(camera: ParallaxCamera, isPaused: Boolean)
}