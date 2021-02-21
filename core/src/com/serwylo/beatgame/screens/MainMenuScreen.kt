package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.BeatGame

class MainMenuScreen(private val game: BeatGame): MenuScreen(keys, labels, "Beat Game", game.assets.getSprites().logo) {

    override fun onMenuItemSelected(selectedIndex: Int) {

        when (keys[selectedIndex]) {
            "play" -> game.showLevelSelectMenu()
            "achievements" -> game.showAchievements()
            "about" -> game.showAboutScreen()
            "quit" -> Gdx.app.exit()
        }

    }

    companion object {

        private val keys = listOf(
                "play",
                "achievements",
                "about",
                "quit"
        )

        private val labels = listOf(
                "Play",
                "Achievements",
                "About",
                "Quit"
        )

    }

}