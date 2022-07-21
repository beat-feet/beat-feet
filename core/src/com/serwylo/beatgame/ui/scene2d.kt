package com.serwylo.beatgame.ui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.calcDensityScaleFactor
import com.serwylo.beatgame.levels.LevelGroup
import com.serwylo.beatgame.levels.Levels

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

fun makeSmallButton(label: String, styles: Assets.Styles, onClick: () -> Unit): Button {
    return TextButton(label, styles.textButton.small).apply {
        addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onClick()
            }
        })
    }
}

fun makeIcon(sprite: TextureRegion, size: Float = UI_SPACE * 4): Image {
    return Image(
        makeIconDrawable(sprite, size)
    )
}

fun makeIconDrawable(sprite: TextureRegion, size: Float = UI_SPACE * 4): TextureRegionDrawable {
    return TextureRegionDrawable(sprite).apply {
        setMinSize(size, size)
    }
}

fun makeHeading(title: String, icon: TextureRegion, styles: Assets.Styles, strings: I18NBundle, onBack: (() -> Unit)? = null): HorizontalGroup {
    return HorizontalGroup().apply {
        space(UI_SPACE * 2)
        padBottom(UI_SPACE)
        addActor(makeIcon(icon, 75f))
        addActor(Label(title, styles.label.huge))
        if (onBack != null) {
            addActor(makeSmallButton(strings["btn.back"], styles, onBack))
        }
    }
}

fun makeLevelGroupSelector(styles: Assets.Styles, currentLevelGroup: LevelGroup, onSelected: (newGroup: LevelGroup) -> Unit) =
    Table().apply {
        add(
            if (Levels.groups.first() === currentLevelGroup) null else makeSmallButton("<", styles) {
                onSelected(Levels.groups[Levels.groups.indexOf(currentLevelGroup) - 1])
            }
        ).width(UI_SPACE * 8).spaceRight(UI_SPACE * 2)

        add(
            Label("World ${currentLevelGroup.number}", styles.label.small)
        )

        add(
            if (Levels.groups.last() === currentLevelGroup) null else makeSmallButton(">", styles) {
                onSelected(Levels.groups[Levels.groups.indexOf(currentLevelGroup) + 1])
            }
        ).width(UI_SPACE * 8).spaceLeft(UI_SPACE * 2)
    }

val UI_WIDTH = 1024f / calcDensityScaleFactor()
val UI_HEIGHT = 768f / calcDensityScaleFactor()
const val UI_SPACE = 10f
