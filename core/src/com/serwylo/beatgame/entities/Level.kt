package com.serwylo.beatgame.entities

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.serwylo.beatgame.audio.AudioFeature

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
        val features: List<AudioFeature>,
        private val scaleX: Float,
) {

    fun dispose() {
        music.stop()
        music.dispose()
    }

    companion object {
        private const val MAX_FEATURE_HEIGHT = 3f
    }
}