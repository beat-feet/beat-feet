package com.serwylo.beatgame.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Rectangle
import com.serwylo.beatgame.graphics.ParallaxCamera
import com.serwylo.beatgame.graphics.SpriteRenderer

class Obstacle(val rect: Rectangle, private val sprite: SpriteRenderer) : Entity {

    override fun update(delta: Float) {
    }

    override fun render(camera: ParallaxCamera, isPaused: Boolean) {

        sprite.render()

        /*val r = Globals.shapeRenderer
        r.projectionMatrix = camera.combined
        r.color = Color.WHITE
        r.begin(ShapeRenderer.ShapeType.Line)
        r.rect(rect.x, rect.y, rect.width, rect.height)
        r.end()*/

    }

    companion object {

        const val STRENGTH_TO_HEIGHT = 3f

        const val MIN_HEIGHT = 0.1f
    }

}