package com.serwylo.beatgame.screens

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeButton
import com.serwylo.beatgame.ui.makeLargeButton

class PauseGameActor(
    game: BeatFeetGame,
    onResume: () -> Unit,
    onReplay: () -> Unit,
    onChangeLevel: () -> Unit,
    onMainMenu: () -> Unit
) : VerticalGroup() {

    private val styles = game.assets.getStyles()
    private val strings = game.assets.getStrings()

    init {

        align(Align.center)
        columnAlign(Align.center)
        space(UI_SPACE)

        val resumeButton = makeLargeButton(strings["btn.resume"], styles) { onResume() }
        val restartButton = makeButton(strings["btn.restart"], styles) { onReplay() }
        val changeLevelButton = makeButton(strings["btn.change-level"], styles) { onChangeLevel() }
        val mainMenuButton = makeButton(strings["btn.main-menu"], styles) { onMainMenu() }

        addActor(Label(strings["paused"], styles.label.huge))
        addActor(resumeButton)
        addActor(HorizontalGroup().apply {
            addActor(restartButton)
            addActor(changeLevelButton)
            addActor(mainMenuButton)
        })
    }

}
