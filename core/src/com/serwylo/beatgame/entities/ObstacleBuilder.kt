package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.graphics.TiledSprite
import kotlin.math.ceil

object ObstacleBuilder {

    fun makeObstacle(rect: Rectangle, atlas: TextureAtlas): Obstacle {

        val narrow = rect.width < TILE_SIZE
        val short = rect.height < TILE_SIZE

        return if (narrow && short) {
            makeSmallObstacle(atlas, rect.x)
        } else if (short) {
            makeShortObstacle(atlas, rect.x, rect.width)
        } else if (narrow) {
            makeNarrowObstacle(atlas, rect.x, rect.height)
        } else {
            makeBigObstacle(atlas, rect)
        }

    }

    private fun sizeToTileCount(size: Float): Int = ceil(size / TILE_SIZE).toInt()

    private fun makeBigObstacle(atlas: TextureAtlas, rect: Rectangle): Obstacle {
        // Building

        val tilesWide = sizeToTileCount(rect.width)
        val tilesHigh = sizeToTileCount(rect.height)

        val sprites = Array(tilesHigh) { arrayOfNulls<TextureRegion?>(tilesWide) }

        for (x in 0 until tilesWide) {
            for (y in 0 until tilesHigh) {
                val spriteName: String = if (x == 0 && y == 0) {
                    BUILDING_BRICK_BOTTOM_LEFT
                } else if (x == 0 && y == tilesHigh - 1) {
                    BUILDING_BRICK_TOP_LEFT
                } else if (x == 0) {
                    BUILDING_BRICK_LEFT
                } else if (x == tilesWide - 1 && y == 0) {
                    BUILDING_BRICK_BOTTOM_RIGHT
                } else if (x == tilesWide - 1 && y == tilesHigh - 1) {
                    BUILDING_BRICK_TOP_RIGHT
                } else if (x == tilesWide - 1) {
                    BUILDING_BRICK_RIGHT
                } else if (y == 0) {
                    BUILDING_BRICK_BOTTOM
                } else if (y == tilesHigh - 1) {
                    BUILDING_BRICK_TOP
                } else {
                    BUILDING_BRICK_INNER
                }

                sprites[y][x] = atlas.findRegion(spriteName)
            }
        }

        val boundingBox = Rectangle(rect.x, rect.y, tilesWide * TILE_SIZE, tilesHigh * TILE_SIZE)
        return Obstacle(boundingBox, TiledSprite(Vector2(boundingBox.x, boundingBox.y), sprites))
    }

    private fun makeNarrowObstacle(atlas: TextureAtlas, x: Float, height: Float): Obstacle {
        // Light poles, trees, etc.
        val boundingBox = Rectangle(x, 0f, TILE_SIZE, height)
        val tilesHigh = sizeToTileCount(height)
        val sprites = Array<Array<TextureRegion?>>(tilesHigh) { arrayOf( atlas.findRegion(BARREL_C) ) }
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), sprites))
    }

    private fun makeShortObstacle(atlas: TextureAtlas, x: Float, width: Float): Obstacle {
        // Fence or row of cars or seats, etc.
        val boundingBox = Rectangle(x, 0f, width, TILE_SIZE)
        val tilesWide = sizeToTileCount(width)
        val sprites = Array<Array<TextureRegion?>>(1) {
            Array<TextureRegion?>(tilesWide) { atlas.findRegion( FENCE_A ) }
        }
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), sprites))
    }

    private fun makeSmallObstacle(atlas: TextureAtlas, x: Float): Obstacle {
        // Barrel or barrier, etc.
        val boundingBox = Rectangle(x, 0f, TILE_SIZE, TILE_SIZE)
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), atlas.findRegion(BARRIER_A)))
    }

    private const val TILE_SIZE = TiledSprite.TILE_SIZE

    private const val BUILDING_BRICK_INNER = "building_inner_brick"
    private const val BUILDING_BRICK_TOP_LEFT = "building_top_left_brick"
    private const val BUILDING_BRICK_TOP = "building_top_brick"
    private const val BUILDING_BRICK_TOP_RIGHT = "building_top_right_brick"
    private const val BUILDING_BRICK_RIGHT = "building_right_brick"
    private const val BUILDING_BRICK_BOTTOM_RIGHT = "building_bottom_right_brick"
    private const val BUILDING_BRICK_BOTTOM = "building_bottom_brick"
    private const val BUILDING_BRICK_BOTTOM_LEFT = "building_bottom_left_brick"
    private const val BUILDING_BRICK_LEFT = "building_left_brick"

    private const val DOOR_A = "door_a"
    private const val DOOR_B = "door_b"
    private const val DOOR_C = "door_c"
    private const val DOOR_D = "door_d"
    private val DOORS = arrayOf(DOOR_A, DOOR_B, DOOR_C, DOOR_D)

    private const val BARREL_A = "barrel_a"
    private const val BARREL_B = "barrel_b"
    private const val BARREL_C = "barrel_c"
    private val BARRELS = arrayOf(BARREL_A, BARREL_B, BARREL_C)

    private const val FENCE_A = "fence_a"
    private const val FENCE_B = "fence_b"
    private val FENcES = arrayOf(FENCE_A, FENCE_B)

    private const val BARRIER_A = "barrier_a"
    private const val BARRIER_B = "barrier_b"
    private const val BARRIER_C = "barrier_c"
    private const val BARRIER_D = "barrier_d"
    private val BARRIERS = arrayOf(BARRIER_A, BARRIER_B, BARRIER_C, BARRIER_D)

}