package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.serwylo.beatgame.Globals

class Obstacle(val rect: Rectangle) : Entity {

    override fun update(delta: Float) {
    }

    override fun render(camera: Camera) {
        val r = Globals.shapeRenderer
        r.projectionMatrix = camera.combined
        r.color = Color.GREEN
        r.begin(ShapeRenderer.ShapeType.Filled)
        r.rect(rect.x, rect.y, rect.width, rect.height)
        r.end()
    }

    companion object {

        const val STRENGTH_TO_HEIGHT = 2f
        const val MIN_HEIGHT = 0.05f

    }

}