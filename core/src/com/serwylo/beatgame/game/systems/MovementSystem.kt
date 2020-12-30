package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.serwylo.beatgame.game.components.PositionComponent
import com.serwylo.beatgame.game.components.ScrollingComponent
import com.serwylo.beatgame.game.components.VelocityComponent

class MovementSystem : IteratingSystem(
        Family.all(PositionComponent::class.java, VelocityComponent::class.java).get()) {

    private val positionM = ComponentMapper.getFor(PositionComponent::class.java)
    private val velocityM = ComponentMapper.getFor(VelocityComponent::class.java)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val p = positionM.get(entity)
        val v = velocityM.get(entity)

        p.position.x += v.velocity.x * deltaTime
        p.position.y += v.velocity.y * deltaTime
    }

}
