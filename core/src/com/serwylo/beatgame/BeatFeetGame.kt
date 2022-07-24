package com.serwylo.beatgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.audio.features.LevelData
import com.serwylo.beatgame.screens.*

open class BeatFeetGame(val platformListener: PlatformListener, private val verbose: Boolean) : Game() {

    // Initialize this in the create() method so that we can access Gdx logging. Helps to diagnose
    // issues with asset loading if we can log meaningful messages.
    // See https://github.com/beat-feet/beat-feet/issues/97.
    lateinit var assets: Assets

    @Suppress("LibGDXLogLevel") // Optional flag to make more verbose.
    override fun create() {
        if (verbose) {
            Gdx.app.logLevel = Application.LOG_DEBUG
        }

        assets = Assets(Assets.getLocale())

        Globals.shapeRenderer = ShapeRenderer()
        Globals.spriteBatch = SpriteBatch()

        assets.initSync()
        setScreen(MainMenuScreen(this))
    }

    fun loadGame(musicFile: FileHandle, songName: String) {
        Gdx.app.postRunnable {
            setScreen(LoadingScreen(this, musicFile, songName))
        }
    }

    fun startGame(levelData: LevelData) {
        Gdx.app.postRunnable {
            setScreen(PlatformGameScreen(this, levelData))
        }
    }

    fun showMenu() {
        Gdx.app.postRunnable {
            setScreen(MainMenuScreen(this))
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

    fun showAchievements() {
        Gdx.app.postRunnable {
            setScreen(AchievementsScreen(this))
        }
    }

}
