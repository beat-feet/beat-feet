package com.serwylo.beatgame.game.components

import com.badlogic.ashley.core.Component

data class JumpingComponent(
        var isJumping: Boolean = false,
        val jumpStrength: Float = 5f
) : Component
