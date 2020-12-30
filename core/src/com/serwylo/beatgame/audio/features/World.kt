package com.serwylo.beatgame.audio.features

import com.badlogic.gdx.audio.Music
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
class World(val music: Music, val heightMap: Array<Vector2>, val features: List<Feature>, private val scaleX: Float) {

    fun dispose() {
        music.stop()
        music.dispose()
    }

}