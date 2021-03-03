package com.serwylo.beatgame

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.graphics.ParticleEffectActor
import com.serwylo.beatgame.levels.Score

class HUD(private val score: Score, skin: Skin, sprites: Assets.Sprites, private val particles: Assets.Particles, private val sounds: Assets.Sounds) {

    private val stage = Stage(ExtendViewport(400f, 300f))
    private val padding = stage.width / 50

    private val font = BitmapFont()
    private val labelStyle = Label.LabelStyle(font, Color.WHITE)

    private val hearts = DiscreteProgressBar(TextureRegionDrawable(sprites.heart), TextureRegionDrawable(sprites.heart_half), TextureRegionDrawable(sprites.heart_empty), particles.health, true)
    private val shields = DiscreteProgressBar(TextureRegionDrawable(sprites.shield_full), TextureRegionDrawable(sprites.shield_half), TextureRegionDrawable(sprites.shield_empty), particles.health, false)

    private val distanceLabel: Label
    private val scoreLabel: Label
    private val bottomWidget = HorizontalGroup()

    private var previousMultiplier = 1

    init {

        val healthAndShield = VerticalGroup()
        healthAndShield.space(padding / 2)
        healthAndShield.pad(padding / 2)
        healthAndShield.columnAlign(Align.right)
        healthAndShield.align(Align.right or Align.top)
        healthAndShield.setFillParent(true)
        healthAndShield.addActor(hearts)
        healthAndShield.addActor(shields)

        distanceLabel = Label("", labelStyle)
        scoreLabel = Label("", labelStyle)

        // It would make much more sense to put all of these widgets (and the ones in the top right)
        // in a Table. However doing so makes it nigh-on impossible to use actions to shake and move
        // them. The reason is that any update to the text of a label (which happens regularly)
        // will always invalidate the label and also its parent (the Table), resetting the position
        // of everything in it, no matter how far through a given Action is from animating it.
        bottomWidget.setPosition(padding, padding * 2)
        bottomWidget.space(padding / 2)
        bottomWidget.addActor(Image(sprites.right_sign))
        bottomWidget.addActor(distanceLabel)
        bottomWidget.addActor(Image(sprites.score))
        bottomWidget.addActor(scoreLabel)

        stage.addActor(bottomWidget)

        healthAndShield.setPosition(0f, 0f)
        stage.addActor(healthAndShield)

    }

    fun render(delta:Float, health: Int, shield: Int) {

        val distance = (score.distancePercent * 100).toInt().toString() + "%"
        val multiplier = if (score.getMultiplier() <= 1) "" else " x ${score.getMultiplier()}"

        distanceLabel.setText(distance)
        scoreLabel.setText("${score.getPoints()}$multiplier")

        hearts.value = health.toFloat()
        shields.value = shield.toFloat()

        // Bring the increasing multiplier to the players attention by showing
        // a floating up, increasing size, reducing alpha label explaining the new multiplier.
        if (previousMultiplier != score.getMultiplier()) {
            previousMultiplier = score.getMultiplier()

            if (score.getMultiplier() > 1) {
                stage.addActor(createIncreasedMultiplier(score.getMultiplier()))
                playScaleSound(score.getMultiplier())
            }
        }

        stage.act(delta)
        stage.draw()

    }

    /**
     * Play an ever increasing xylophone sound for long combos
     */
    private fun playScaleSound(multiplier: Int) {
        val scaleIndex = multiplier.coerceAtMost(SCALE_SOUND_MAX_PITCH)
        val scaleFactor = scaleIndex.toFloat() / SCALE_SOUND_MAX_PITCH
        val volume = (SCALE_SOUND_VOLUME * scaleIndex).coerceAtMost(1f)
        val sound = sounds.scale.play(volume)
        sounds.scale.setPitch(sound, 1f + scaleFactor /* Must be a number between 0.5 and 2.0, but we are choosing one between 1.0 and 2.0 */ )
    }

    private fun createIncreasedMultiplier(scoreMultiplier: Int): Actor {
        val label = Container<Label>(Label("x $scoreMultiplier", labelStyle))
        label.isTransform = true

        label.addAction(
                sequence(
                        parallel(
                                alpha(0.3f, 1f),
                                scaleBy(3f, 3f, 1f),
                                moveBy(0f, scoreLabel.height * 4, 1f)
                        ),
                        removeActor()
                )
        )

        // Weird using the score labels height here, but we don't have an exact place to measure this
        // yet, so just sort of guessing. It doesn't really matter, because the animation of this
        // actor makes it hard to see exactly where it started.
        label.x = padding + bottomWidget.prefWidth - scoreLabel.height
        label.y = padding

        return label
    }

    companion object {

        private const val SCALE_SOUND_VOLUME = 0.05f
        private const val SCALE_SOUND_MAX_PITCH = 25

    }

}

/**
 * Used for health and shield information. Instead of one continuous bar, show the value via a series
 * of full / half empty / empty sprites.
 */
class DiscreteProgressBar(
    private val textureFull: TextureRegionDrawable,
    private val textureHalf: TextureRegionDrawable,
    private val textureEmpty: TextureRegionDrawable,
    private val particleEffect: ParticleEffect,
    private val canShake: Boolean = false
) : HorizontalGroup() {

    private val images = (1..5).map { Image(textureFull) }
    var value = 100f
        set(newValue) {
            changeValue(value, newValue)
            field = newValue
        }

    init {
        images.forEach { addActor(it) }
    }

    private fun changeValue(oldValue: Float, newValue: Float) {

        val previousNumHalves = (oldValue / 10).toInt()
        val newNumHalfHearts = (newValue / 10).toInt()

        if (newValue <= 0) {

            // Don't shake for the end of game screen, looks a bit jarring if we do.
            clearActions()

        }

        if (previousNumHalves == newNumHalfHearts) {
            return
        }

        if (newNumHalfHearts in 1..2) {
            if (canShake) {
                shake((2 - newNumHalfHearts).toFloat())
            }
        }

        updateImages(newValue)
        showParticles(previousNumHalves, newNumHalfHearts)

    }

    /**
     * If we have reduced the number of things to display enough that we needed to update the images,
     * then show a particle effect of the items dissolving.
     */
    private fun showParticles(numPreviousHalves: Int, numNewHalves: Int) {
        for (i in numNewHalves until numPreviousHalves) {
            val imageToOverlay = images[i / 2]
            val pos = imageToOverlay.parent.localToStageCoordinates(Vector2(imageToOverlay.x, imageToOverlay.y))

            val pActor = ParticleEffectActor(ParticleEffect(particleEffect))
            pActor.setPosition(pos.x, pos.y)
            stage.addActor(pActor)
        }
    }

    private fun updateImages(value: Float) {
        images.forEachIndexed { i, image ->
            val fullThreshold = (i + 1) * 20
            val halfThreshold = fullThreshold - 10

            image.drawable = when {
                value >= fullThreshold -> textureFull
                value >= halfThreshold -> textureHalf
                else -> textureEmpty
            }
        }
    }

    /**
     * The [strength] allows us to shake more if there is less stuff left in this bar. A value between
     * 0f and 1f.
     */
    private fun shake(strength: Float) {

        val strengthDivisor = 2 - strength.coerceIn(0f, 1f)
        val shakeDistance = images[0].width / 5f / strengthDivisor
        val shakeTime = 0.03f

        clearActions()
        addAction(
            forever(
                sequence(
                    moveBy(shakeDistance / 2, 0f, shakeTime),
                    moveBy( - shakeDistance, 0f, shakeTime * 2),
                    Actions.moveBy(shakeDistance / 2, shakeTime)
                )
            )
        )
    }

}