package com.serwylo.beatgame.graphics

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.scenes.scene2d.Actor

// From https://jvm-gaming.org/t/particle-effects-in-libgdx/41758
class ParticleEffectActor(private val effect: ParticleEffect) : Actor() {

    init {
        effect.scaleEffect(0.75f, 0.25f, 1f)
        effect.start()
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        effect.draw(batch)
    }

    override fun act(delta: Float) {
        super.act(delta)
        effect.setPosition(x, y)
        effect.update(delta)
    }

}