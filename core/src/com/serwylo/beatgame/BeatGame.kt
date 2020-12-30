package com.serwylo.beatgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.screens.LoadingScreen
import com.serwylo.beatgame.screens.MainMenuScreen
import com.serwylo.beatgame.screens.PlatformGameScreen

class BeatGame(private val verbose: Boolean) : Game() {

    private lateinit var shapeRenderer: ShapeRenderer

    override fun create() {
        if (verbose) {
            Gdx.app.logLevel = Application.LOG_DEBUG
        }

        shapeRenderer = ShapeRenderer()

        setScreen(MainMenuScreen(this))
    }

    fun loadGame(musicFile: FileHandle, songName: String) {
        setScreen(LoadingScreen(this, musicFile, songName))
    }

    fun startGame(world: World) {
        setScreen(PlatformGameScreen(this, world, shapeRenderer))
    }

    fun showMenu() {
        setScreen(MainMenuScreen(this))
    }

}
