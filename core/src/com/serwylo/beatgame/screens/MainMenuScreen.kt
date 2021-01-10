package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.BeatGame

class MainMenuScreen(private val game: BeatGame): MenuScreen(keys, labels, "Beat Game", "logo") {

    override fun onMenuItemSelected(selectedIndex: Int) {

        when (keys[selectedIndex]) {
            "play" -> game.showLevelSelectMenu()
            "about" -> game.showAboutScreen()
            "quit" -> Gdx.app.exit()
        }

    }

    companion object {

        private val keys = listOf(
                "play",
                "about",
                "quit"
        )

        private val labels = listOf(
                "Play",
                "About",
                "Quit"
        )

    }

}