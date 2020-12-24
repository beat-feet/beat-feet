package com.serwylo.beatgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.screens.MainMenuScreen
import com.serwylo.beatgame.screens.PlatformGameScreen

class BeatGame : Game() {

    override fun create() {
        setScreen(MainMenuScreen(this))
    }

    fun startGame(songName: String) {
        setScreen(PlatformGameScreen(this, Gdx.files.internal(songName)))
    }

    fun showMenu() {
        setScreen(MainMenuScreen(this))
    }

}