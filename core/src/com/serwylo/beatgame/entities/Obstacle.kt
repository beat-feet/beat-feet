package com.serwylo.beatgame.entities

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
//
class Obstacle(
        private val feature: AudioFeature,
) {

    fun init(world: World, scaleX: Float) {

        val x = feature.startTimeInSeconds * scaleX
        val y = 0f
        val width = feature.durationInSeconds * scaleX
        val height = feature.strength * MAX_FEATURE_HEIGHT

        val body = world.createBody(BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(x + width / 2, y + height)
        })

        body.userData = this

        body.createFixture(
                PolygonShape().apply { setAsBox(width, height) },
                0f)

        val sensor = body.createFixture(
                PolygonShape().apply { setAsBox(width - 0.04f, 0.02f, Vector2(0.02f, height), 0f) },
                0f)

        sensor.isSensor = true

    }

    companion object {
        private const val MAX_FEATURE_HEIGHT = 3f
    }
}