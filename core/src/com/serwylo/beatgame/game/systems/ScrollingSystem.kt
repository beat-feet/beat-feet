package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.serwylo.beatgame.game.components.ScrollingComponent
import com.serwylo.beatgame.game.components.VelocityComponent

class ScrollingSystem :
        IteratingSystem(Family.all(ScrollingComponent::class.java, VelocityComponent::class.java).get()) {

    private val scrollingM = ComponentMapper.getFor(ScrollingComponent::class.java)
    private val velocityM = ComponentMapper.getFor(VelocityComponent::class.java)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val scrolling = scrollingM.get(entity)
        val velocity = velocityM.get(entity)

        velocity.velocity.x = scrolling.speed
    }

}