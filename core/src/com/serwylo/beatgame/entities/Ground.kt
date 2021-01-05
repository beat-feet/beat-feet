package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.Globals

class Ground : Entity {

    override fun update(delta: Float) {
    }

    override fun render(camera: Camera, isPaused: Boolean) {
        val r = Globals.shapeRenderer

        r.projectionMatrix = camera.combined
        r.color = Color.GREEN
        r.begin(ShapeRenderer.ShapeType.Line)
        r.line(-100f, 0f, 10000f, 0f)
        r.end()
    }
}