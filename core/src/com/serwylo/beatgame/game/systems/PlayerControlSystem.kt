package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.serwylo.beatgame.game.components.JumpingComponent
import com.serwylo.beatgame.game.components.VelocityComponent

class PlayerControlSystem : EntitySystem() {

    override fun update(deltaTime: Float) {

        val input = processInput()

        val playerEntity = engine.getEntitiesFor(Family.all(JumpingComponent::class.java).get()).first()

        val player = playerEntity.getComponent(JumpingComponent::class.java)
        val velocity = playerEntity.getComponent(VelocityComponent::class.java)

        if (input.shouldJump && !player.isJumping) {
            player.isJumping = true
            velocity.velocity.y = player.jumpStrength
        }

    }

    private fun processInput(): InputIntents {

        return InputIntents(
                shouldJump = Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isTouched
        )

    }

}

data class InputIntents(val shouldJump: Boolean)