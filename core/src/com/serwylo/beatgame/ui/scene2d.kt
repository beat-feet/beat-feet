package com.serwylo.beatgame.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.serwylo.beatgame.Assets
import com.serwylo.beatgame.graphics.calcDensityScaleFactor
import com.serwylo.beatgame.levels.TheOriginalWorld
import com.serwylo.beatgame.levels.World
import java.net.URLEncoder

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

fun makeWorldSelector(
    strings: I18NBundle,
    styles: Assets.Styles,
    currentWorld: World?,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) =
    Table().apply {
        add(
            if (onPrevious == null) null else makeButton("<", styles, onPrevious)
        ).width(UI_SPACE * 8).spaceRight(UI_SPACE * 2)

        add(
            VerticalGroup().also { col ->
                col.addActor(Label(currentWorld?.getLabel(strings) ?: strings["level-select.more-coming-soon.title"], styles.label.medium))
            }
        ).minWidth(UI_SPACE * 20)

        add(
            if (onNext == null) null else makeButton(">", styles, onNext)
        ).width(UI_SPACE * 8).spaceLeft(UI_SPACE * 2)
    }

fun makeErrorReport(
    strings: I18NBundle,
    styles: Assets.Styles,
    error: Throwable,
    readableMessage: String,
    tryAgain: (() -> Unit)? = null
) =
    VerticalGroup().also { group ->
        group.space(UI_SPACE)
        group.addActor(Label(strings["error.title"], styles.label.large))
        group.addActor(
            Label(readableMessage, styles.label.medium).apply {
                setAlignment(Align.center)
            }
        )

        if (tryAgain != null) {
            group.addActor(
                makeLargeButton(strings["error.try-again"], styles) {
                    tryAgain()
                }
            )
        }

        val errorMessageEncoded = URLEncoder.encode(error.toString(), "utf-8")
        val errorReportBody = URLEncoder.encode(
            // Intentionally don't internationalise this because it is designed to be sent to the authors to help diagnose issues.
"""
The following error occurred during the game:

```
${error.stackTraceToString()}
```
""".trim(),
            "utf-8",
        )

        group.addActor(
            HorizontalGroup().also { buttons ->
                buttons.pad(UI_SPACE)

                buttons.addActor(
                    makeButton(strings["error.show-details"], styles) {
                        buttons.clearChildren()

                        group.addActor(
                            Label(
                                error.stackTraceToString().replace("\t", "    "),
                                styles.label.small,
                            )
                        )

                        buttons.addActor(
                            makeButton(strings["error.report.via-github"], styles) {
                                Gdx.net.openURI("https://github.com/beat-feet/beat-feet/issues/new?title=Error%20report:%20$errorMessageEncoded&labels=bug&body=$errorReportBody")
                            }
                        )

                        buttons.addActor(
                            makeButton(strings["error.report.via-email"], styles) {
                                Gdx.net.openURI("mailto:peter.serwylo+beat-feet-errors@gmail.com?subject=[Beat%20Feet%20Error%20Report]:%20$errorMessageEncoded&body=$errorReportBody")
                            }
                        )
                    }
                )
            }
        )
    }


val UI_WIDTH = 1024f / calcDensityScaleFactor()
val UI_HEIGHT = 768f / calcDensityScaleFactor()
const val UI_SPACE = 10f
