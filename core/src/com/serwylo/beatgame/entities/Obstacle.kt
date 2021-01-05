package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.graphics.TiledSprite

class Obstacle(val rect: Rectangle, private val sprite: TiledSprite) : Entity {

    override fun update(delta: Float) {
    }

    override fun render(camera: Camera, isPaused: Boolean) {

        sprite.render()

        /*val r = Globals.shapeRenderer
        r.projectionMatrix = camera.combined
        r.color = Color.WHITE
        r.begin(ShapeRenderer.ShapeType.Line)
        r.rect(rect.x, rect.y, rect.width, rect.height)
        r.end()*/

    }

    companion object {

        const val STRENGTH_TO_HEIGHT = 4f

        const val MIN_HEIGHT = 0.1f
    }

}