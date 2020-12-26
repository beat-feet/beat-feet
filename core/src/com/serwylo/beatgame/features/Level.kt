package com.serwylo.beatgame.features

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.serwylo.beatgame.Globals
import kotlin.math.max
import kotlin.math.min

//
// TODO:
//   Small features are animals to jump on (or cosume for energy?)
//   Mid sized features are platforms to jump on
//   Large sized features need to be destroyed (by using consumed energy)
//   Items which start above the ground and finish below are no good (and vice verca) as they render weird)
//
class Level(
        val music: Music,
        val heightMap: Array<Vector2>,
        val features: List<Feature>,
        private val scaleX: Float,
) {

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

    fun init(world: World, scaleX: Float) {

        generateGround(world, scaleX)
        generateFeatures(world)

    }

    private fun generateFeatures(world: World) {

        boxes.forEach {
            val startHeight = heightAtPosition(it.x)
            val endHeight = heightAtPosition(it.x + it.width)

            val body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
                position.set(0f, 0f)
            })

            val shape = PolygonShape().apply {
                set(floatArrayOf(
                        it.x, startHeight,
                        it.x, startHeight + it.height,
                        it.x + it.width, startHeight + it.height,
                        it.x + it.width, endHeight
                ))
            }

            body.createFixture(shape, 0f)

        }

    }

    private fun generateGround(world: World, scaleX: Float) {
        val body = world.createBody(BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(0f, 0f)
        })

        var i = 0

        while (i < heightMap.size) {

            val positions = heightMap.slice(IntRange(i, min(heightMap.size - 1, i + 3)))
            if (positions.size < 2) {
                break
            }

            val shape = generateGroundVertices(world, positions, scaleX)

            body.createFixture(shape, 0f)

            i += 3 // Overlap with the previous vertex. Even though we are pulling out 4 vertices
                   // at a time, only advance forward 3.

        }

    }

    private fun generateGroundVertices(world: World, heightValues: List<Vector2>, scaleX: Float): Shape {

        // Turn the line into a polygon that extends below the bottom of the world.
        // To make it look better, we could instead repeat the line a metre below, may make more sense.
        val positions = heightValues + heightValues.reversed().map { Vector2(it.x, it.y - 0.1f) }

        val vertices = FloatArray(positions.size * 2)
        positions.forEachIndexed { i, v ->
            vertices[i * 2] = positions[i].x * scaleX
            vertices[i * 2 + 1] = positions[i].y
        }

        return PolygonShape().apply {
            set(vertices)
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

        val platform = Globals.shapeRenderer

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

    fun dispose() {
        music.stop()
        music.dispose()
    }

    companion object {
        private const val MAX_FEATURE_HEIGHT = 3f
    }
}