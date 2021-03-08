package com.serwylo.beatgame.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.calcDensityScaleFactor

fun makeStage() =
    Stage(ExtendViewport(UI_WIDTH, UI_HEIGHT))

fun makeButton(label: String, styles: Assets.Styles, onClick: () -> Unit): Button {
    val button = TextButton(label, styles.textButton.medium)
    button.addListener(object: ChangeListener() {
        override fun changed(event: ChangeEvent?, actor: Actor?) {
            onClick()
        }
    })

    return button
}

fun makeLargeButton(label: String, styles: Assets.Styles, onClick: () -> Unit): Button {
    val button = TextButton(label, styles.textButton.large)
    button.addListener(object: ChangeListener() {
        override fun changed(event: ChangeEvent?, actor: Actor?) {
            onClick()
        }
    })

    return button
}

fun makeIcon(sprite: TextureRegion, size: Float = UI_SPACE * 4): Image {
    val drawable = TextureRegionDrawable(sprite)
    drawable.setMinSize(size, size)
    return Image(drawable)
}

val UI_WIDTH = 1024f / calcDensityScaleFactor()
val UI_HEIGHT = 768f / calcDensityScaleFactor()
const val UI_SPACE = 10f
