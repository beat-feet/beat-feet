package com.serwylo.beatgame.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity

class CollisionComponent(
        val entity1: Entity,
        val entity2: Entity
) : Component
