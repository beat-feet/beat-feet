package com.serwylo.beatgame.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle

data class ShapeComponent(
        var polygons: FloatArray,
        var colour: Color = Color.WHITE
) : Component {

    val boundingBox: Rectangle

    init {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        for (i in 0 until polygons.size / 2) {
            if (polygons[i * 2] < minX) minX = polygons[i * 2]
            if (polygons[i * 2] > maxX) maxX = polygons[i * 2]
            if (polygons[i * 2 + 1] < minY) minY = polygons[i * 2 + 1]
            if (polygons[i * 2 + 1] > maxY) maxY = polygons[i * 2 + 1]
        }

        boundingBox = Rectangle(minX, minY, maxX - minX, maxY - minY)
    }
}