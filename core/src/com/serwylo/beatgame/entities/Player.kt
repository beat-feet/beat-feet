package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*


class Player(private val world: World) {

    private var body: Body
    private var isOnGround = false
    private var isJumping = false

    init {

        body = world.createBody(BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(0f, 0f)
            linearVelocity.x = 5f
            linearDamping = 0f
        })

        val fixtureDef = FixtureDef().apply {
            shape = CircleShape().apply { radius = 0.15f }
            density = 100f
            friction = 0f
            restitution  = 0f
        }

        body.createFixture(fixtureDef)
        body.userData = this
        body.gravityScale = 2f
    }

    fun performJump() {
        if (isOnGround) {
            body.applyLinearImpulse(Vector2(0f, 77f), body.position, true)
            isOnGround = false
            isJumping = true
        }
    }

    fun render(camera: OrthographicCamera) {

        if (body.linearVelocity.x < TARGET_SPEED) {
            body.applyLinearImpulse(Vector2(2f, 0f), body.position, true)
        }

        // TODO: If position is less than target position, speed up even a little bit more to catch up.
        // TODO: To deal with when the math above fails, apply an impulse in the other direction to bring back to the desired location too.

    }

    fun beginContact(fixture: Fixture) {
        if (fixture.body.userData is Ground || (fixture.body.userData is Obstacle && fixture.isSensor)) {
            isOnGround = true
        }

    }

    companion object {
        private const val TARGET_SPEED = 5f
    }

}