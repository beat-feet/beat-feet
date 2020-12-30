package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.serwylo.beatgame.game.components.*

class CollisionResolutionSystem :
        IteratingSystem(Family.all(CollisionComponent::class.java).get()) {

    private val collisionM = ComponentMapper.getFor(CollisionComponent::class.java)
    private val shapeM = ComponentMapper.getFor(ShapeComponent::class.java)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val collision = collisionM.get(entity)

        if (collision.entity1.getComponent(JumpingComponent::class.java) != null) {
            playerCollision(collision.entity1, collision.entity2)
        } else if (collision.entity2.getComponent(JumpingComponent::class.java) != null) {
            playerCollision(collision.entity2, collision.entity1)
        }

        engine.removeEntity(entity)
    }

    private fun playerCollision(player: Entity, other: Entity) {

        val p = player.getComponent(JumpingComponent::class.java)
        p.isJumping = false

        val playerJump = player.getComponent(JumpingComponent::class.java)
        val playerPos = player.getComponent(PositionComponent::class.java)
        val playerVel = player.getComponent(VelocityComponent::class.java)
        val otherPos = other.getComponent(ShapeComponent::class.java)
        
        if (isLanding(playerPos, otherPos)) {
            playerPos.position.y = otherPos.boundingBox.y + otherPos.boundingBox.height
            playerVel.velocity.y = 0f
            playerJump.isJumping = false
        } else /* is crashing into in some way or another */ {
            
        }
        // playerPos.position.y = /*otherPos.boundingBox.y +*/ otherPos.boundingBox.height

    }

    private fun isLanding(playerPos: PositionComponent?, otherPos: ShapeComponent?): Boolean {
        return true
    }

}