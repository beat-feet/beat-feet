package com.serwylo.beatgame.features

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.max
import kotlin.math.min

//
// TODO:
//   Small features are animals to jump on (or cosume for energy?)
//   Mid sized features are platforms to jump on
//   Large sized features need to be destroyed (by using consumed energy)
//   Items which start above the ground and finish below are no good (and vice verca) as they render weird)
//
class World(private val heightMap: Array<Vector2>, private val features: List<Feature>, private val scaleX: Float) {

    private val platform = ShapeRenderer()
    private val platformLine: FloatArray = FloatArray(heightMap.size * 2)
    private val boxes: List<Rectangle> = features.map { feature ->
        val x = feature.startTimeInSeconds * scaleX
        val width = feature.durationInSeconds * scaleX
        val height = feature.strength * MAX_FEATURE_HEIGHT

        val y = heightAtTime(feature.startTimeInSeconds)

        Rectangle(x, y, width, height)
    }

    init {
        heightMap.forEachIndexed { i, pos ->
            platformLine[i * 2] = pos.x * scaleX
            platformLine[i * 2 + 1] = pos.y
        }
    }

    fun heightAtTime(timeInSeconds: Float) =
            heightMap.findLast { it.x < timeInSeconds }?.y ?: 0f

    fun heightAtPosition(xPosition: Float) =
            heightAtTime(xPosition / scaleX)

    private fun renderVisiblePlatform(viewport: Rectangle): FloatArray {
        val startX = max(0, heightMap.indexOfLast { it.x * scaleX < viewport.x }) * 2
        val endX = min(heightMap.size - 1, heightMap.indexOfFirst { it.x * scaleX > viewport.x + viewport.width }) * 2
        return platformLine.sliceArray(IntRange(startX, endX + 1 + 6))
    }

    fun render(camera: Camera, viewport: Rectangle) {

        platform.projectionMatrix = camera.combined
        platform.color = Color.GREEN
        platform.begin(ShapeRenderer.ShapeType.Line)
        platform.polyline(renderVisiblePlatform(viewport))

        platform.color = Color.WHITE
        boxes.filter { it.x < viewport.x + viewport.width && it.x + it.width > viewport.x }.forEach {
            val startHeight = heightAtPosition(it.x)
            val endHeight = heightAtPosition(it.x + it.width)
            platform.line(it.x, startHeight, it.x, startHeight + it.height)
            platform.line(it.x, startHeight + it.height, it.x + it.width, startHeight + it.height)
            platform.line(it.x + it.width, startHeight + it.height, it.x + it.width, endHeight)
        }

        platform.end()

    }

    companion object {
        private const val MAX_FEATURE_HEIGHT = 3f

        fun generate(heightMap: Array<Vector2>, features: List<Feature>, scrollSpeed: Float): World {
            return World(heightMap, features, scrollSpeed)
        }
    }
}