package com.serwylo.beatgame.game.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.game.components.CameraComponent
import com.serwylo.beatgame.game.components.PositionComponent
import com.serwylo.beatgame.game.components.ShapeComponent

/**
 * Inspired by https://github.com/RoaringCatGames/libgdx-ashley-box2d-example/blob/master/core/src/com/roaringcatgames/testgame/systems/RenderingSystem.java
 */
class RenderingSystem(private val shapeRenderer: ShapeRenderer) :
        IteratingSystem(Family.all(PositionComponent::class.java, ShapeComponent::class.java).get()) {

    private val camera = OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)

    private val positionM = ComponentMapper.getFor(PositionComponent::class.java)
    private val shapeM = ComponentMapper.getFor(ShapeComponent::class.java)

    private val renderQueue = mutableListOf<Entity>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        renderQueue.add(entity)
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val cameraEntity = engine.getEntitiesFor(Family.all(CameraComponent::class.javaObjectType).get()).first()
        val cameraPosition = positionM.get(cameraEntity).position
        camera.position.set(cameraPosition, 0f)
        camera.update()

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        renderQueue.forEach {
            val pos = positionM.get(it)
            val shape = shapeM.get(it)

            shapeRenderer.identity()
            shapeRenderer.translate(pos.position.x, pos.position.y, 0f)
            shapeRenderer.color = shape.colour

            shapeRenderer.polyline(shape.polygons)

        }

        shapeRenderer.end()
        renderQueue.clear()

    }

    companion object {

        private const val VIEWPORT_WIDTH = 20f
        private const val VIEWPORT_HEIGHT = 20f

    }

}