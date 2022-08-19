package com.serwylo.beatgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.serwylo.beatgame.audio.features.LevelData
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.screens.*
import ktx.async.KtxAsync

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

        KtxAsync.initiate()

        assets = Assets(Assets.getLocale())

        Globals.shapeRenderer = ShapeRenderer()
        Globals.spriteBatch = SpriteBatch()

        assets.initSync()
        setScreen(MainMenuScreen(this))
    }

    fun loadGame(level: Level) {
        Gdx.app.postRunnable {
            setScreen(LoadingScreen(this, level))
        }
    }

    fun startGame(level: Level, levelData: LevelData) {
        Gdx.app.postRunnable {
            setScreen(PlatformGameScreen(this, level, levelData))
        }
    }

    fun showMenu() {
        Gdx.app.postRunnable {
            setScreen(MainMenuScreen(this))
        }
    }

    fun showLevelSelectMenu(world: World) {
        Gdx.app.postRunnable {
            setScreen(LevelSelectScreen(this, world))
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
