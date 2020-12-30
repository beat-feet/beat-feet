package com.serwylo.beatgame.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

data class VelocityComponent(
        val velocity: Vector2 = Vector2()
) : Component