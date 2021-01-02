package com.serwylo.beatgame.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera

fun makeCamera(maxWidth: Int, maxHeight: Int): OrthographicCamera {
    return if (Gdx.graphics.width > Gdx.graphics.height) {
        val aspectRatio = Gdx.graphics.height.toFloat() / Gdx.graphics.width
        OrthographicCamera(maxWidth.toFloat(), maxWidth * aspectRatio)
    } else {
        val aspectRatio = Gdx.graphics.width.toFloat() / Gdx.graphics.height
        OrthographicCamera(maxHeight * aspectRatio, maxHeight.toFloat())
    }
}