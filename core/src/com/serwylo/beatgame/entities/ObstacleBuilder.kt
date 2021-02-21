package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.LayeredTiledSprite
import com.serwylo.beatgame.graphics.TiledSprite
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

object ObstacleBuilder {

    const val TILE_SIZE = TiledSprite.TILE_SIZE

    fun roundToNearestTile(size: Float): Float {
        return (size / TILE_SIZE).toInt() * TILE_SIZE + TILE_SIZE
    }

    /**
     * In pixels, each tile is 16x16 pixels in size.
     * In game units, they are each [TILE_SIZE]x[TILE_SIZE] units large.
     */
    fun px2Unit(tilePixels: Int): Float {
        return tilePixels.toFloat() * (TILE_SIZE) / 16f
    }

    fun makeObstacle(rect: Rectangle, sprites: Assets.Sprites): Obstacle {

        val narrow = rect.width <= TILE_SIZE
        val short = rect.height <= TILE_SIZE

        return if (narrow && short) {
            makeSmallObstacle(sprites, rect)
        } else if (short) {
            makeShortObstacle(sprites, rect.x, rect.width)
        } else if (narrow) {
            makeNarrowObstacle(sprites, rect.x, rect.height)
        } else {
            makeBigObstacle(sprites, rect)
        }

    }

    private fun sizeToTileCount(size: Float): Int = ceil(size / TILE_SIZE).toInt()

    private fun makeBigObstacle(sprites: Assets.Sprites, rect: Rectangle): Obstacle {
        // Building

        val tilesWide = sizeToTileCount(rect.width)
        val tilesHigh = sizeToTileCount(rect.height)

        val baseSprites = Array(tilesHigh) { arrayOfNulls<TextureRegion?>(tilesWide) }
        val buildingSprites = BuildingSprites.random()

        for (x in 0 until tilesWide) {
            for (y in 0 until tilesHigh) {
                val sprite: RegionFetcher = if (x == 0 && y == 0) {
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

                baseSprites[y][x] = sprite(sprites)
            }
        }

        val featureSprites = Array(tilesHigh) { arrayOfNulls<TextureRegion?>(tilesWide) }

        val doorSprite = DoorSprite.random()
        val doorPosition = (Math.random() * tilesWide).toInt().coerceAtMost(tilesWide - 1 /* In case Math.random() returns 1.0... can it do this?*/)
        featureSprites[0][doorPosition] = doorSprite.closed(sprites)

        val woodenWindowTexture = WoodenWindowSprite.random().sprite(sprites)

        if (tilesHigh == 2) {
            if (tilesWide == 2) {
                // Small square building, make the image appear in the opposite corner to the door.
                featureSprites[1][(doorPosition + 1) % 2] = woodenWindowTexture
            } else {
                // Low building, put windows everywhere except above the door (seems to look okay).
                for (i in 0 until tilesWide) {
                    if (i != doorPosition) {
                        featureSprites[1][i] = woodenWindowTexture
                    }
                }
            }
        } else {
            for (i in 1 until tilesHigh) {
                if (i % 2 != tilesHigh % 2) {
                    for (j in 0 until tilesWide) {
                        featureSprites[i][j] = woodenWindowTexture
                    }
                }
            }
        }

        val boundingBox = Rectangle(rect.x, rect.y, tilesWide * TILE_SIZE, tilesHigh * TILE_SIZE)

        val baseLayer = TiledSprite(Vector2(boundingBox.x, boundingBox.y), baseSprites)
        val featureLayer = TiledSprite(Vector2(boundingBox.x, boundingBox.y), featureSprites)

        return Obstacle(boundingBox, LayeredTiledSprite(arrayOf(baseLayer, featureLayer)))
    }

    private fun makeNarrowObstacle(sprites: Assets.Sprites, x: Float, height: Float): Obstacle {
        // Light poles, trees, etc.
        val tilesHigh = sizeToTileCount(height)
        val boundingBox = Rectangle(x, 0f, TILE_SIZE, tilesHigh * TILE_SIZE)
        val streetlight = StreetlightSprites.random()
        val spriteTextures = Array<Array<TextureRegion?>>(tilesHigh) { index ->
            when (index) {
                0 -> arrayOf(streetlight.base(sprites))
                (tilesHigh - 1) -> arrayOf(streetlight.top(sprites))
                else -> arrayOf(streetlight.post(sprites))
            }
        }
        return Obstacle(boundingBox, TiledSprite(Vector2(x, 0f), spriteTextures))
    }

    class BuildingSprites(val topLeft: RegionFetcher, val top: RegionFetcher, val topRight: RegionFetcher, val right: RegionFetcher, val bottomRight: RegionFetcher, val bottom: RegionFetcher, val bottomLeft: RegionFetcher, val left: RegionFetcher, val inner: RegionFetcher) {
        companion object {
            private val all = arrayOf(
                    BuildingSprites({ it.building_a_top_left }, { it.building_a_top }, { it.building_a_top_right }, { it.building_a_right }, { it.building_a_bottom_right }, { it.building_a_bottom }, { it.building_a_bottom_left }, { it.building_a_left }, { it.building_a_inner }),
                    BuildingSprites({ it.building_b_top_left }, { it.building_b_top }, { it.building_b_top_right }, { it.building_b_right }, { it.building_b_bottom_right }, { it.building_b_bottom }, { it.building_b_bottom_left }, { it.building_b_left }, { it.building_b_inner }),
                    BuildingSprites({ it.building_c_top_left }, { it.building_c_top }, { it.building_c_top_right }, { it.building_c_right }, { it.building_c_bottom_right }, { it.building_c_bottom }, { it.building_c_bottom_left }, { it.building_c_left }, { it.building_c_inner })
            )

            fun random(): BuildingSprites {
                return all.random()
            }
        }
    }

    class StreetlightSprites(val base: RegionFetcher, val post: RegionFetcher, val top: RegionFetcher) {
        companion object {
            private val all = arrayOf(
                    StreetlightSprites({ it.streetlight_a_base }, { it.streetlight_a_post }, { it.streetlight_a_top }),
                    StreetlightSprites({ it.streetlight_b_base }, { it.streetlight_b_post }, { it.streetlight_b_top }),
                    StreetlightSprites({ it.streetlight_c_base }, { it.streetlight_c_post }, { it.streetlight_c_top }),
                    StreetlightSprites({ it.streetlight_d_base }, { it.streetlight_d_post }, { it.streetlight_d_top }),
                    StreetlightSprites({ it.streetlight_e_base }, { it.streetlight_e_post }, { it.streetlight_e_top }),
                    StreetlightSprites({ it.streetlight_f_base }, { it.streetlight_f_post }, { it.streetlight_f_top })
            )

            fun random(): StreetlightSprites {
                return all.random()
            }
        }
    }

    class WallSprites(val left: RegionFetcher, val inner: Array<RegionFetcher>, val right: RegionFetcher) {

        constructor(left: RegionFetcher, inner: RegionFetcher, right: RegionFetcher):
                this(left, arrayOf(inner), right)

        companion object {
            private val all = arrayOf(
                WallSprites({ it.wall_a_left }, { it.wall_a_inner }, { it.wall_a_right }),
                WallSprites({ it.wall_b_left }, { it.wall_b_inner }, { it.wall_b_right }),
                WallSprites({ it.wall_c_left }, { it.wall_c_inner }, { it.wall_c_right }),
                WallSprites(
                        { it.fence_left },
                        arrayOf<RegionFetcher>({ it.fence_inner }, { it.fence_inner }, { it.fence_inner }, { it.fence_inner }, { it.fence_inner_broken_a }, { it.fence_inner_broken_b }),
                        { it.fence_right }
                )
            )

            fun random(): WallSprites {
                return all.random()
            }
        }
    }

    class BushSprite(val sprite: RegionFetcher) {
        companion object {
            private val all = arrayOf(
                    BushSprite { it.bush_small_a },
                    BushSprite { it.bush_small_a },
                    BushSprite { it.bush_small_b },
                    BushSprite { it.bush_small_c },
                    BushSprite { it.bush_medium_a },
                    BushSprite { it.bush_medium_b },
                    BushSprite { it.bush_medium_c }
            )

            fun random(): BushSprite {
                return all.random()
            }
        }
    }

    class DoorSprite(
            val closed: RegionFetcher,
            val open: RegionFetcher,
            val covered: RegionFetcher) {

        companion object {

            private val all = arrayOf(
                    DoorSprite( { it.door_a_closed }, { it.door_a_open }, { it.door_a_covered } ),
                    DoorSprite( { it.door_b_closed }, { it.door_b_open }, { it.door_b_covered } ),
                    DoorSprite( { it.door_c_closed }, { it.door_c_open }, { it.door_c_covered } ),
                    DoorSprite( { it.door_d_closed }, { it.door_d_open }, { it.door_d_covered } ),
                    DoorSprite( { it.door_e_closed }, { it.door_e_open }, { it.door_e_covered } ),
                    DoorSprite( { it.door_f_closed }, { it.door_f_open }, { it.door_f_covered } )
            )

            fun random(): DoorSprite {
                return all.random()
            }

        }
    }

    class WoodenWindowSprite(val sprite: RegionFetcher) {

        companion object {
            private val all = arrayOf(
                    WoodenWindowSprite { it.window_wood_a },
                    WoodenWindowSprite { it.window_wood_b },
                    WoodenWindowSprite { it.window_wood_c },
                    WoodenWindowSprite { it.window_wood_d },
                    WoodenWindowSprite { it.window_wood_e },
                    WoodenWindowSprite { it.window_wood_f },
                    WoodenWindowSprite { it.window_wood_g },
                    WoodenWindowSprite { it.window_wood_h },
                    WoodenWindowSprite { it.window_wood_i },
                    WoodenWindowSprite { it.window_wood_j }
            )

            fun random(): WoodenWindowSprite {
                return all.random()
            }
        }
    }

    class SmallObstacle(private val sprite: RegionFetcher, val width: Float, val height: Float, val offsetX: Float = 0f, val offsetY: Float = 0f) {

        private var diagnoal = sqrt(width * width + height * height)

        fun getSprite(sprites: Assets.Sprites): TextureRegion = sprite(sprites)

        companion object {
            private val all = arrayOf(
                    SmallObstacle({ it.barrel_a }, px2Unit(8), px2Unit(12), px2Unit(4), px2Unit(2)),
                    SmallObstacle({ it.barrier_a }, px2Unit(16), px2Unit(11)),
                    SmallObstacle({ it.box_small }, px2Unit(10), px2Unit(10), px2Unit(3), px2Unit(2)),
                    SmallObstacle({ it.box_medium }, px2Unit(12), px2Unit(12), px2Unit(2), px2Unit(2)),
                    SmallObstacle({ it.hydrant }, px2Unit(8), px2Unit(13), px2Unit(4), px2Unit(2)),
                    SmallObstacle({ it.tyres_small }, px2Unit(8), px2Unit(10), px2Unit(4), px2Unit(2)),
                    SmallObstacle({ it.tyres_medium }, px2Unit(8), px2Unit(11), px2Unit(4), px2Unit(2)),
                    SmallObstacle({ it.tyres_large }, px2Unit(12), px2Unit(14), px2Unit(2), px2Unit(1))
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

    private fun makeShortObstacle(sprites: Assets.Sprites, x: Float, width: Float): Obstacle {
        // Fence or row of cars or seats, etc.
        val tilesWide = sizeToTileCount(width)
        val boundingBox = Rectangle(x, 0f, tilesWide * TILE_SIZE, TILE_SIZE)
        val wallSprites = WallSprites.random()
        val baseLayerSprites = Array<TextureRegion?>(tilesWide) {
            when (it) {
                0 -> wallSprites.left(sprites)
                (tilesWide - 1) -> wallSprites.right(sprites)
                else -> wallSprites.inner.random()(sprites)
            }
        }

        val flourishLayerSprites = Array<TextureRegion?>(tilesWide) {
            if (Math.random() < 0.25) {
                BushSprite.random().sprite(sprites)
            } else {
                null
            }
        }

        val position = Vector2(x, 0f)

        val layers = LayeredTiledSprite(arrayOf(
                TiledSprite(position, arrayOf(baseLayerSprites)),
                TiledSprite(position, arrayOf(flourishLayerSprites))
        ))

        return Obstacle(boundingBox, layers)
    }

    private fun makeSmallObstacle(sprites: Assets.Sprites, rect: Rectangle): Obstacle {
        // Barrel or barrier, etc.
        val smallThing = SmallObstacle.closest(rect)
        val boundingBox = Rectangle(rect.x, rect.y, smallThing.width, smallThing.height)
        return Obstacle(boundingBox, TiledSprite(Vector2(rect.x, 0f), smallThing.getSprite(sprites), Vector2(smallThing.offsetX, smallThing.offsetY)))
    }

    fun makeGround(sprites: Assets.Sprites): Ground {
        return Ground(GroundSprite.random().getSprite(sprites))
    }

    class GroundSprite(private val sprite: RegionFetcher) {

        fun getSprite(sprites: Assets.Sprites): TextureRegion = sprite(sprites)

        companion object {

            private val all = arrayOf(
                    GroundSprite { it.ground_a },
                    GroundSprite { it.ground_b },
                    GroundSprite { it.ground_c },
                    GroundSprite { it.ground_d },
                    GroundSprite { it.ground_e },
                    GroundSprite { it.ground_f }
            )

            fun random(): GroundSprite {
                return all.random()
            }
        }
    }

}

typealias RegionFetcher = (sprites: Assets.Sprites) -> TextureRegion