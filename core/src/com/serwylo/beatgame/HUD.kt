package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.entities.Player
import com.serwylo.beatgame.graphics.ParticleEffectActor
import kotlin.math.floor


class HUD(private val atlas: TextureAtlas) {

    private val stage = Stage(ExtendViewport(400f, 300f))

    private val padding: Float

    private val font = BitmapFont()

    private val textureHeartFull: TextureRegionDrawable
    private val textureHeartHalf: TextureRegionDrawable
    private val textureHeartEmpty: TextureRegionDrawable
    private val textureScore: TextureRegion
    private val textureDistance: TextureRegion
    private val labelStyle: Label.LabelStyle

    private val heartImages: MutableList<Image> = mutableListOf()

    private val distanceLabel: Label
    private val scoreLabel: Label
    private val healthLabel: Label
    private val bottomWidget: HorizontalGroup

    private val scaleSounds: List<Sound>

    private var previousMultiplier = 1f
    private var previousHealth = 100

    init {

        scaleSounds = SCALE_SOUND_FILES.map { Gdx.audio.newSound(Gdx.files.internal("sounds/scales/soundset_vibraphone/${it}")) }

        padding = stage.width / 50

        textureHeartFull = TextureRegionDrawable(atlas.findRegion("heart"))
        textureHeartHalf = TextureRegionDrawable(atlas.findRegion("heart_half"))
        textureHeartEmpty = TextureRegionDrawable(atlas.findRegion("heart_empty"))

        textureScore = atlas.findRegion("score")
        textureDistance = atlas.findRegion("right_sign")

        labelStyle = Label.LabelStyle(font, Color.WHITE)

        val healthWidget = HorizontalGroup()
        healthWidget.space(padding / 2)

        healthLabel = Label("", labelStyle)
        healthWidget.addActor(healthLabel)

        val heartContainer = HorizontalGroup()
        for (i in 1..5) {
            val heart = Image(textureHeartFull)
            heartImages.add(heart)
            heartContainer.addActor(heart)
        }
        healthWidget.addActor(heartContainer)

        distanceLabel = Label("", labelStyle)
        scoreLabel = Label("", labelStyle)

        bottomWidget = HorizontalGroup()
        bottomWidget.space(padding / 2)
        bottomWidget.addActor(Image(textureDistance))
        bottomWidget.addActor(distanceLabel)
        bottomWidget.addActor(Image(textureScore))
        bottomWidget.addActor(scoreLabel)

        val table = Table()
        table.setFillParent(true)
        table.pad(padding)
        table.add(healthWidget).top().right().expand()
        table.row()
        table.add(bottomWidget).bottom().left()

        stage.addActor(table)

    }

    fun render(distancePercent: Float, player: Player) {
        val distance = (distancePercent * 100).toInt().toString() + "%"
        val multiplier = if (player.scoreMultiplier <= 1) "" else " x ${player.scoreMultiplier.toInt()}"

        healthLabel.setText(player.getHealth())
        distanceLabel.setText(distance)
        scoreLabel.setText("${player.getScore()}$multiplier")

        if (previousHealth != player.getHealth()) {
            val previousNumHalfHearts = previousHealth / 10
            val newNumHalfHearts = player.getHealth() / 10
            previousHealth = player.getHealth()

            if (previousNumHalfHearts != newNumHalfHearts) {

                for (i in newNumHalfHearts until previousNumHalfHearts) {
                    val imageToOverlay = heartImages[i / 2]
                    val pos = imageToOverlay.parent.localToStageCoordinates(Vector2(imageToOverlay.x, imageToOverlay.y))

                    val p = ParticleEffect()
                    p.load(Gdx.files.internal("effects/health.p"), atlas)
                    val pActor = ParticleEffectActor(p)
                    pActor.setPosition(pos.x, pos.y)
                    stage.addActor(pActor)
                }

                heartImages.forEachIndexed { i, image ->
                    val fullThreshold = (i + 1) * 20
                    val halfThreshold = fullThreshold - 10

                    image.drawable = when {
                        player.getHealth() >= fullThreshold -> textureHeartFull
                        player.getHealth() >= halfThreshold -> textureHeartHalf
                        else -> textureHeartEmpty
                    }
                }

            }
        }

        // Bring the increasing multiplier to the players attention by showing
        // a floating up, increasing size, reducing alpha label explaining the new multiplier.
        if (previousMultiplier != player.scoreMultiplier) {
            previousMultiplier = player.scoreMultiplier

            // Only show feedback for whole numbers.
            if (player.scoreMultiplier > 1f && floor(player.scoreMultiplier) == player.scoreMultiplier) {
                stage.addActor(createIncreasedMultiplier(player.scoreMultiplier))

                // Play an ever increasing xylophone sound for long combos
                val scaleIndex = player.scoreMultiplier.toInt().coerceAtMost(scaleSounds.size - 1)
                val volume = (SCALE_SOUND_VOLUME * scaleIndex).coerceAtMost(1f)
                scaleSounds[scaleIndex].play(volume)
            }
        }

        stage.act()
        stage.draw()
    }

    private fun createIncreasedMultiplier(scoreMultiplier: Float): Actor {
        val label = Container<Label>(Label("x ${scoreMultiplier.toInt()}", labelStyle))
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
        label.x = padding + bottomWidget.width - scoreLabel.height
        label.y = padding

        return label
    }

    fun dispose() {
        scaleSounds.forEach { it.dispose() }
    }

    companion object {

        private const val SCALE_SOUND_VOLUME = 0.05f

        private val SCALE_SOUND_FILES = listOf(
                "n01.mp3",
                "n02.mp3",
                "n03.mp3",
                "n04.mp3",
                "n05.mp3",
                "n06.mp3",
                "n07.mp3",
                "n08.mp3",
                "n09.mp3",
                "n10.mp3",
                "n11.mp3",
                "n12.mp3",
                "n13.mp3",
                "n14.mp3",
                "n15.mp3",
                "n16.mp3",
                "n17.mp3",
                "n18.mp3",
                "n19.mp3",
                "n20.mp3",
                "n21.mp3",
                "n22.mp3",
                "n23.mp3",
                "n24.mp3"
        )

    }

}