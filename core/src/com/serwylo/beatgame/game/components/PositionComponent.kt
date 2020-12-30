package com.serwylo.beatgame.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

data class PositionComponent(
        val position: Vector2 = Vector2()
) : Component