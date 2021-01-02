package com.serwylo.beatgame.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont

abstract class MenuScreen(): ScreenAdapter() {

    protected val camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT).apply {
        translate((VIEWPORT_WIDTH / 2) - ITEM_HEIGHT * ITEM_SPACING, (VIEWPORT_HEIGHT / 4))
        update()
    }

    protected var bigFont = BitmapFont().apply { data.scale(0.5f) }
    protected var mediumFont = BitmapFont().apply { data.scale(-0.2f) }
    protected var smallFont = BitmapFont().apply { data.scale(-0.5f) }

    companion object {

        @JvmStatic
        protected val VIEWPORT_WIDTH = 400f

        @JvmStatic
        protected val VIEWPORT_HEIGHT = 200f

        @JvmStatic
        protected val ITEM_HEIGHT = 20f

        @JvmStatic
        protected val ITEM_SPACING = 1.2f

    }
}