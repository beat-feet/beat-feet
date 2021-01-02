package com.serwylo.beatgame.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera

fun makeCamera(maxWidth: Int, maxHeight: Int, scaleFactor: Float = 1f): OrthographicCamera {

    val width: Float
    val height: Float

    if (Gdx.graphics.width > Gdx.graphics.height) {
        val aspectRatio = Gdx.graphics.height.toFloat() / Gdx.graphics.width
        width = maxWidth.toFloat()
        height = maxWidth * aspectRatio
    } else {
        val aspectRatio = Gdx.graphics.width.toFloat() / Gdx.graphics.height
        width = maxHeight * aspectRatio
        height = maxHeight.toFloat()
    }

    return OrthographicCamera(width / scaleFactor, height / scaleFactor)

}

fun calcDensityScaleFactor(): Float {
    return (Gdx.graphics.density - 1).coerceAtLeast(1f)
}