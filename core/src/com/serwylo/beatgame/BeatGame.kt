package com.serwylo.beatgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.serwylo.beatgame.features.Level
import com.serwylo.beatgame.screens.LoadingScreen
import com.serwylo.beatgame.screens.MainMenuScreen
import com.serwylo.beatgame.screens.PlatformGameScreen

class BeatGame(private val verbose: Boolean) : Game() {

    override fun create() {
        if (verbose) {
            Gdx.app.logLevel = Application.LOG_DEBUG
        }

        Globals.shapeRenderer = ShapeRenderer()
        Globals.box2DRenderer = Box2DDebugRenderer()

        setScreen(MainMenuScreen(this))
    }

    fun loadGame(musicFile: FileHandle, songName: String) {
        setScreen(LoadingScreen(this, musicFile, songName))
    }

    fun startGame(world: Level) {
        setScreen(PlatformGameScreen(this, world))
    }

    fun showMenu() {
        setScreen(MainMenuScreen(this))
    }

}
