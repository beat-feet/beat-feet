package com.serwylo.beatgame

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.entities.Player

class HUD(atlas: TextureAtlas) {

    private val stage = Stage(ExtendViewport(400f, 300f))

    private val padding: Float

    private val font = BitmapFont()

    private val textureHeartFull: TextureRegionDrawable
    private val textureHeartHalf: TextureRegionDrawable
    private val textureHeartEmpty: TextureRegionDrawable
    private val textureScore: TextureRegion
    private val textureDistance: TextureRegion

    private val heartImages: MutableList<Image> = mutableListOf()

    private val distanceLabel: Label
    private val scoreLabel: Label
    private val healthLabel: Label

    init {

        padding = stage.width / 50

        textureHeartFull = TextureRegionDrawable(atlas.findRegion("heart"))
        textureHeartHalf = TextureRegionDrawable(atlas.findRegion("heart_half"))
        textureHeartEmpty = TextureRegionDrawable(atlas.findRegion("heart_empty"))

        textureScore = atlas.findRegion("score")
        textureDistance = atlas.findRegion("right_sign")

        val labelStyle = Label.LabelStyle(font, Color.WHITE)

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

        val bottomWidget = HorizontalGroup()
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
        val multiplier = if (player.scoreMultiplier <= 1) "" else " x ${player.scoreMultiplier}"
        val score = "${player.getScore()}$multiplier"

        healthLabel.setText(player.getHealth())
        distanceLabel.setText(distance)
        scoreLabel.setText(score)

        heartImages.forEachIndexed { i, image ->
            val fullThreshold = (i + 1) * 20
            val halfThreshold = fullThreshold - 10

            image.drawable = if (player.getHealth() >= fullThreshold) {
                textureHeartFull
            } else if (player.getHealth() >= halfThreshold) {
                textureHeartHalf
            } else {
                textureHeartEmpty
            }
        }

        stage.act()
        stage.draw()
    }

}