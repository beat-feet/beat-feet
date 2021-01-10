package com.serwylo.beatgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.screens.*

class BeatGame(private val verbose: Boolean) : Game() {

    override fun create() {
        if (verbose) {
            Gdx.app.logLevel = Application.LOG_DEBUG
        }

        Globals.shapeRenderer = ShapeRenderer()
        Globals.spriteBatch = SpriteBatch()

        setScreen(MainMenuScreen(this))
    }

    fun loadGame(musicFile: FileHandle, songName: String) {
        Gdx.app.postRunnable {
            setScreen(LoadingScreen(this, musicFile, songName))
        }
    }

    fun startGame(world: World) {
        Gdx.app.postRunnable {
            setScreen(PlatformGameScreen(this, world))
        }
    }

    fun showMenu() {
        Gdx.app.postRunnable {
            setScreen(MainMenuScreen(this))
        }
    }

    fun endGame(world: World, score: Int, distancePercent: Float) {
        Gdx.app.postRunnable {
            setScreen(EndGameScreen(this, world, score, distancePercent))
        }
    }

    fun showLevelSelectMenu() {
        Gdx.app.postRunnable {
            setScreen(LevelSelectScreen(this))
        }
    }

    fun explainCustomSongs() {
        Gdx.app.postRunnable {
            setScreen(ExplainCustomSongsScreen(this))
        }
    }

    fun showAboutScreen() {
        Gdx.app.postRunnable {
            setScreen(AboutScreen(this))
        }
    }

}
