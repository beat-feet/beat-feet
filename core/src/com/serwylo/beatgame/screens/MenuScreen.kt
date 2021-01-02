package com.serwylo.beatgame.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.serwylo.beatgame.graphics.makeCamera

abstract class MenuScreen: ScreenAdapter() {

    protected val camera = makeCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)

    protected var bigFont = BitmapFont().apply { data.scale(0.5f) }
    protected var mediumFont = BitmapFont().apply { data.scale(-0.2f) }
    protected var smallFont = BitmapFont().apply { data.scale(-0.5f) }

    init {
        camera.translate((camera.viewportWidth / 2) - ITEM_HEIGHT * ITEM_SPACING, (camera.viewportHeight / 4))
        camera.update()
    }

    companion object {

        @JvmStatic
        private val VIEWPORT_WIDTH = 400

        @JvmStatic
        private val VIEWPORT_HEIGHT = 200

        @JvmStatic
        protected val ITEM_HEIGHT = 20f

        @JvmStatic
        protected val ITEM_SPACING = 1.2f

    }
}