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
class Ground(private val width: Float) {

    fun init(world: World) {

        val body = world.createBody(BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(0f, 0f)
        })

        body.userData = this

        val shape = PolygonShape().apply {
            setAsBox(
                    width,
                    0.02f,
                    Vector2(width / 2, -0.02f), // TODO: This width is just a guess, make it based on the level size.
                    0f
            )
        }
        body.createFixture(shape, 0f)
    }

}