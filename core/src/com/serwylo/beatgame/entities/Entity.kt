package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera

interface Entity {
    fun update(delta: Float)

    /**
     * The delta here is really just used for animations. Actual logic dependent on the passage
     * of time should use [update].
     */
    fun render(camera: Camera, isPaused: Boolean)
}