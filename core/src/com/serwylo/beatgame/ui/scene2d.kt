package com.serwylo.beatgame.ui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.calcDensityScaleFactor

fun makeStage() =
    Stage(ExtendViewport(UI_WIDTH, UI_HEIGHT))

fun makeButton(label: String, styles: Assets.Styles, onClick: () -> Unit): Button {
    return TextButton(label, styles.textButton.medium).apply {
        addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onClick()
            }
        })
    }
}

fun makeLargeButton(label: String, styles: Assets.Styles, onClick: () -> Unit): Button {
    return TextButton(label, styles.textButton.large).apply {
        addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onClick()
            }
        })
    }
}

fun makeIcon(sprite: TextureRegion, size: Float = UI_SPACE * 4): Image {
    return Image(
        TextureRegionDrawable(sprite).apply {
            setMinSize(size, size)
        }
    )
}

fun makeHeading(title: String, icon: TextureRegion, styles: Assets.Styles): HorizontalGroup {
    return HorizontalGroup().apply {
        space(UI_SPACE)
        padBottom(UI_SPACE)
        addActor(makeIcon(icon, 75f))
        addActor(Label(title, styles.label.huge))
    }
}

val UI_WIDTH = 1024f / calcDensityScaleFactor()
val UI_HEIGHT = 768f / calcDensityScaleFactor()
const val UI_SPACE = 10f
