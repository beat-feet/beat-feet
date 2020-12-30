package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.serwylo.beatgame.game.components.*

class CollisionSystem :
        IteratingSystem(Family.all(PositionComponent::class.java, ShapeComponent::class.java).get()) {

    private val velocityM = ComponentMapper.getFor(VelocityComponent::class.java)
    private val positionM = ComponentMapper.getFor(PositionComponent::class.java)
    private val shapeM = ComponentMapper.getFor(ShapeComponent::class.java)

    private val toCheck = mutableListOf<Entity>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        toCheck.add(entity)
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        for (e1 in toCheck) {

            // No point comparing two components that don't have velocity, so ensure at least one
            // has a velocity.
            if (velocityM.get(e1) != null) {

                for (e2 in toCheck) {
                    if (e1 !== e2) {
                        checkCollision(e1, e2)
                    }
                }

            }
        }

        toCheck.clear()
    }

    private fun checkCollision(e1: Entity, e2: Entity) {
        val shape1 = shapeM.get(e1)
        val position1 = positionM.get(e1)

        val shape2 = shapeM.get(e2)
        val position2 = positionM.get(e2)

        if (shape1.boundingBox.setPosition(position1.position).overlaps(shape2.boundingBox.setPosition(position2.position))) {
            val collision = engine.createEntity()
            collision.add(CollisionComponent(e1, e2))
            engine.addEntity(collision)
        }
    }
}