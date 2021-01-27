package com.serwylo.beatgame

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.entities.Player
import kotlin.math.floor

class HUD(atlas: TextureAtlas) {

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

    init {

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

    private var previousMultiplier = 1f

    fun render(distancePercent: Float, player: Player) {
        val distance = (distancePercent * 100).toInt().toString() + "%"
        val multiplier = if (player.scoreMultiplier <= 1) "" else " x ${player.scoreMultiplier.toInt()}"

        healthLabel.setText(player.getHealth())
        distanceLabel.setText(distance)
        scoreLabel.setText("${player.getScore()}$multiplier")

        heartImages.forEachIndexed { i, image ->
            val fullThreshold = (i + 1) * 20
            val halfThreshold = fullThreshold - 10

            image.drawable = when {
                player.getHealth() >= fullThreshold -> textureHeartFull
                player.getHealth() >= halfThreshold -> textureHeartHalf
                else -> textureHeartEmpty
            }
        }

        // Bring the increasing multiplier to the players attention by showing
        // a floating up, increasing size, reducing alpha label explaining the new multiplier.
        if (previousMultiplier != player.scoreMultiplier) {
            previousMultiplier = player.scoreMultiplier

            // Only show feedback for whole numbers.
            if (player.scoreMultiplier > 1f && floor(player.scoreMultiplier) == player.scoreMultiplier) {
                stage.addActor(createIncreasedMultiplier(player.scoreMultiplier))
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

}