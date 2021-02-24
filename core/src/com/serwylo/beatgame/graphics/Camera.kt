package com.serwylo.beatgame.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3

fun makeCamera(maxWidth: Int, maxHeight: Int, scaleFactor: Float = 1f): ParallaxCamera {

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

    return ParallaxCamera(width / scaleFactor, height / scaleFactor)

}

fun calcDensityScaleFactor(): Float {
    return ((Gdx.graphics.density - 1) * 0.8f).coerceAtLeast(1f)
}

/**
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/ParallaxTest.java
 * http://www.apache.org/licenses/LICENSE-2.0
 */
class ParallaxCamera(viewportWidth: Float, viewportHeight: Float) : OrthographicCamera(viewportWidth, viewportHeight) {

    var parallaxView = Matrix4()
    var parallaxCombined = Matrix4()
    var tmp = Vector3()
    var tmp2 = Vector3()

    fun calculateParallaxMatrix(parallaxX: Float, parallaxY: Float): Matrix4 {

        update()

        tmp.set(position)
        tmp.x *= parallaxX
        tmp.y *= parallaxY

        parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up)
        parallaxCombined.set(projection)

        Matrix4.mul(parallaxCombined.`val`, parallaxView.`val`)

        return parallaxCombined

    }

}