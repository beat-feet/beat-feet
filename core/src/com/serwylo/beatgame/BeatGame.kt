package com.serwylo.beatgame

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.features.World
import com.serwylo.beatgame.screens.LoadingScreen
import com.serwylo.beatgame.screens.MainMenuScreen
import com.serwylo.beatgame.screens.PlatformGameScreen

class BeatGame : Game() {

    override fun create() {
        Globals.shapeRenderer = ShapeRenderer()
        setScreen(MainMenuScreen(this))
    }

    fun loadGame(musicFile: FileHandle, songName: String) {
        setScreen(LoadingScreen(this, musicFile, songName))
    }

    fun startGame(world: World) {
        setScreen(PlatformGameScreen(this, world))
    }

    fun showMenu() {
        setScreen(MainMenuScreen(this))
    }

}
