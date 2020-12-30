package com.serwylo.beatgame.game.entities

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.audio.features.Feature
import com.serwylo.beatgame.game.components.*

fun makeCamera(engine: Engine): Entity {
    return engine.createEntity().apply {
        add(PositionComponent())
        add(VelocityComponent())
        add(ScrollingComponent(5f))
        add(CameraComponent())
    }
}

fun makePlayer(engine: Engine): Entity {

    val width = 0.3f
    val height = 1.0f

    val shape = floatArrayOf(
            0f, 0f,
            0f, height,
            width, height,
            width, 0f,
            0f, 0f
    )

    return engine.createEntity().apply {
        add(ShapeComponent(shape, Color.RED))
        add(PositionComponent())
        add(ScrollingComponent(5f))
        add(VelocityComponent())
        add(GravityComponent(-30f))
        add(JumpingComponent(jumpStrength = 8f))
    }

}

private const val MAX_OBSTACLE_HEIGHT = 7f

fun makeObstacle(engine: Engine, feature: Feature, scaleX: Float): Entity {

    val startX = feature.startTimeInSeconds * scaleX
    val startY = 0f

    val width = feature.durationInSeconds * scaleX
    val height = feature.strength * MAX_OBSTACLE_HEIGHT

    val shape = floatArrayOf(
        0f, 0f,
        0f, height,
        width, height,
        width, 0f,
        0f, 0f
    )

    return engine.createEntity().apply {
        add(PositionComponent(Vector2(startX, startY)))
        add(ShapeComponent(shape, Color.YELLOW))
    }

}

fun makeGround(engine: Engine): Entity {

    val shape = floatArrayOf(
        -100f, 0f,
        -100f, -0.01f,
        1000f, -0.01f,
        1000f, 0f,
        -100f, 0f
    )

    return engine.createEntity().apply {
        add(PositionComponent())
        add(ShapeComponent(shape, Color.GREEN))
    }

}