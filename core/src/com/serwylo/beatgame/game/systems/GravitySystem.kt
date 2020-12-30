package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.serwylo.beatgame.game.components.GravityComponent
import com.serwylo.beatgame.game.components.PositionComponent
import com.serwylo.beatgame.game.components.ScrollingComponent
import com.serwylo.beatgame.game.components.VelocityComponent

class GravitySystem : IteratingSystem(
        Family.all(VelocityComponent::class.java, GravityComponent::class.java).get()) {

    private val gravityM = ComponentMapper.getFor(GravityComponent::class.java)
    private val velocityM = ComponentMapper.getFor(VelocityComponent::class.java)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val g = gravityM.get(entity)
        val v = velocityM.get(entity)

        v.velocity.y += g.gravity * deltaTime
    }

}
