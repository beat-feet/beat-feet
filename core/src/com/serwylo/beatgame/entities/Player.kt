package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.serwylo.beatgame.screens.PlatformGameScreen


class Player(
        private val world: World,
        private val calcHeightAtPosition: (pos: Float) -> Float) {

    private var body: Body

    init {

        body = world.createBody(BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(0f, calcHeightAtPosition(1f))
        })

        val fixtureDef = FixtureDef().apply {
            shape = CircleShape().apply { radius = 0.3f }
            density = 30f
            friction = 0.4f
        }

        val fixture = body.createFixture(fixtureDef)

        body.userData = this
    }

    fun performJump() {
        body.setLinearVelocity(5f, 0f)
        body.applyLinearImpulse(0f, 70f, 0f, 0f, true)
    }

    fun render(camera: OrthographicCamera) {

        body.setLinearVelocity(5f, body.linearVelocity.y)
/*
        val player = ShapeRenderer(50)
        player.projectionMatrix = camera.combined
        player.color = Color.CYAN
        player.begin(ShapeRenderer.ShapeType.Filled)
        player.circle(
                position,
                if (jumpTime >= 0) jumpStartHeight + jumpHeight + 0.3f else calcHeightAtPosition(position) + 0.3f,
                0.3f,
                8)
        player.end()*/

    }

}