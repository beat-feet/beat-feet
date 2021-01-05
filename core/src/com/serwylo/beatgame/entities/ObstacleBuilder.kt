package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.graphics.TiledSprite
import com.sun.xml.internal.fastinfoset.util.StringArray
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

object ObstacleBuilder {

    /**
     * If it is less than a tile high, then just leave the size as is.
     * However, if it is above a tile high, then only deal in tile sizes. This will make it much
     * easier to merge obstacles together for large and interesting buildings.
     */
    fun roundShapeToNearestObstacle(rect: Rectangle): Rectangle {
        if (rect.height <= TILE_SIZE) {
            return rect
        }

        val height = (rect.height / TILE_SIZE).toInt() * TILE_SIZE + TILE_SIZE
        return Rectangle(0f, 0f, rect.width, height)
    }

    /**
     * In pixels, each tile is 16x16 pixels in size.
     * In game units, they are each [TILE_SIZE]x[TILE_SIZE] units large.
     */
    fun px2Unit(tilePixels: Int): Float {
        return tilePixels.toFloat() * (TILE_SIZE) / 16f
    }

    fun makeObstacle(rect: Rectangle, atlas: TextureAtlas): Obstacle {

        val narrow = rect.width < TILE_SIZE
        val short = rect.height < TILE_SIZE

        return if (narrow && short) {
            makeSmallObstacle(atlas, rect)
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
        val buildingSprites = BuildingSprites.random()

        for (x in 0 until tilesWide) {
            for (y in 0 until tilesHigh) {
                val spriteName: String = if (x == 0 && y == 0) {
                    buildingSprites.bottomLeft
                } else if (x == 0 && y == tilesHigh - 1) {
                    buildingSprites.topLeft
                } else if (x == 0) {
                    buildingSprites.left
                } else if (x == tilesWide - 1 && y == 0) {
                    buildingSprites.bottomRight
                } else if (x == tilesWide - 1 && y == tilesHigh - 1) {
                    buildingSprites.topRight
                } else if (x == tilesWide - 1) {
                    buildingSprites.right
                } else if (y == 0) {
                    buildingSprites.bottom
                } else if (y == tilesHigh - 1) {
                    buildingSprites.top
                } else {
                    buildingSprites.inner
                }

                sprites[y][x] = atlas.findRegion(spriteName)
            }
        }

        val boundingBox = Rectangle(rect.x, rect.y, tilesWide * TILE_SIZE, tilesHigh * TILE_SIZE)
        return Obstacle(boundingBox, TiledSprite(Vector2(boundingBox.x, boundingBox.y), sprites))
    }

    private fun makeNarrowObstacle(atlas: TextureAtlas, x: Float, height: Float): Obstacle {
        // Light poles, trees, etc.
        val tilesHigh = sizeToTileCount(height)
        val boundingBox = Rectangle(x, 0f, TILE_SIZE, tilesHigh * TILE_SIZE)
        val streetlight = StreetlightSprites.random()
        val sprites = Array<Array<TextureRegion?>>(tilesHigh) { index ->
            when (index) {
                0 -> arrayOf( atlas.findRegion(streetlight.base))
                (tilesHigh - 1) -> arrayOf(atlas.findRegion(streetlight.top))
                else -> arrayOf( atlas.findRegion(streetlight.post))
            }
        }
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), sprites))
    }

    class BuildingSprites(val topLeft: String, val top: String, val topRight: String, val right: String, val bottomRight: String, val bottom: String, val bottomLeft: String, val left: String, val inner: String) {
        companion object {
            private val all = arrayOf("a", "b", "c").map {
                val prefix = "building_${it}"
                BuildingSprites(
                        "${prefix}_top_left",
                        "${prefix}_top",
                        "${prefix}_top_right",
                        "${prefix}_right",
                        "${prefix}_bottom_right",
                        "${prefix}_bottom",
                        "${prefix}_bottom_left",
                        "${prefix}_left",
                        "${prefix}_inner"
                )
            }

            fun random(): BuildingSprites {
                return all.random()
            }
        }
    }

    class StreetlightSprites(val base: String, val post: String, val top: String) {
        companion object {
            private val all = arrayOf("a", "b", "c", "d", "e", "f").map {
                StreetlightSprites("streetlight_${it}_base", "streetlight_${it}_post", "streetlight_${it}_top")
            }

            fun random(): StreetlightSprites {
                return all.random()
            }
        }
    }

    class WallSprites(val left: String, val inner: Array<String>, val right: String) {

        constructor(left: String, inner: String, right: String):
                this(left, arrayOf(inner), right)

        companion object {
            private val all = arrayOf(
                WallSprites("wall_a_left", "wall_a_inner", "wall_a_right"),
                WallSprites("wall_b_left", "wall_b_inner", "wall_b_right"),
                WallSprites("wall_c_left", "wall_c_inner", "wall_c_right"),
                WallSprites(
                        "fence_left",
                        arrayOf("fence_inner", "fence_inner", "fence_inner", "fence_inner", "fence_inner_broken_a", "fence_inner_broken_b"),
                        "fence_right"
                )
            )

            fun random(): WallSprites {
                return all.random()
            }
        }
    }

    class SmallObstacle(val sprite: String, val width: Float, val height: Float, val offsetX: Float = 0f, val offsetY: Float = 0f) {

        private var diagnoal = sqrt(width * width + height * height)

        companion object {
            private val all = arrayOf(
                    SmallObstacle("barrel_a", px2Unit(8), px2Unit(12), px2Unit(4), px2Unit(2)),
                    SmallObstacle("barrier_a", px2Unit(16), px2Unit(11)),
                    SmallObstacle("box_small", px2Unit(10), px2Unit(10), px2Unit(3), px2Unit(2)),
                    SmallObstacle("box_medium", px2Unit(12), px2Unit(12), px2Unit(2), px2Unit(2)),
                    SmallObstacle("hydrant", px2Unit(8), px2Unit(13), px2Unit(4), px2Unit(2)),
                    SmallObstacle("tyres_small", px2Unit(8), px2Unit(10), px2Unit(4), px2Unit(2)),
                    SmallObstacle("tyres_medium", px2Unit(8), px2Unit(11), px2Unit(4), px2Unit(2)),
                    SmallObstacle("tyres_large", px2Unit(12), px2Unit(14), px2Unit(2), px2Unit(1))
            )

            /**
             * Not sure the best way to do this, but my intuition tells me that a check of the diagonal
             * distance is the best way to proceed at this point.
             *
             * +-----+   +---+
             * |  /  |   |  /|
             * +-----+   |/  |
             *           +---+
             */
            fun closest(rect: Rectangle): SmallObstacle {

                val toCompareDiagonal = sqrt(rect.width * rect.width + rect.height * rect.height)

                var best: SmallObstacle = all[0]
                var bestDifference = abs(best.diagnoal - toCompareDiagonal)

                for (i in 1 until all.size) {

                    val difference = abs(all[i].diagnoal - toCompareDiagonal)
                    if (difference < bestDifference) {
                        bestDifference = difference
                        best = all[i]
                    }

                }

                return best

            }

        }
    }

    private fun makeShortObstacle(atlas: TextureAtlas, x: Float, width: Float): Obstacle {
        // Fence or row of cars or seats, etc.
        val tilesWide = sizeToTileCount(width)
        val boundingBox = Rectangle(x, 0f, tilesWide * TILE_SIZE, TILE_SIZE)
        val wallSprites = WallSprites.random()
        val sprites = Array<TextureRegion?>(tilesWide) {
            when (it) {
                0 -> atlas.findRegion(wallSprites.left)
                (tilesWide - 1) -> atlas.findRegion(wallSprites.right)
                else -> atlas.findRegion(wallSprites.inner.random())
            }
        }
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), arrayOf(sprites)))
    }

    private fun makeSmallObstacle(atlas: TextureAtlas, rect: Rectangle): Obstacle {
        // Barrel or barrier, etc.
        val smallThing = SmallObstacle.closest(rect)
        val boundingBox = Rectangle(rect.x, rect.y, smallThing.width, smallThing.height)
        return Obstacle(boundingBox, TiledSprite(Vector2(rect.x, 0f), atlas.findRegion(smallThing.sprite), Vector2(smallThing.offsetX, smallThing.offsetY)))
    }

    private const val TILE_SIZE = TiledSprite.TILE_SIZE

}