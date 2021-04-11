package com.serwylo.beatgame.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.ScreenUtils
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.loadWorldFromMp3
import com.serwylo.beatgame.levels.Levels
import com.serwylo.beatgame.levels.Score
import com.serwylo.beatgame.levels.achievements.*
import com.serwylo.beatgame.levels.clearAllHighScores
import com.serwylo.beatgame.levels.saveHighScore
import com.serwylo.beatgame.screens.*
import java.io.File
import java.util.*


class BeatFeetGameForScreenshots(private val verbose: Boolean) : BeatFeetGame(DesktopPlatformListener(), verbose) {

    private val input = MockInput()

    override fun getLocale(): Locale {
        return Locale("nb", "NO")
    }

    @Suppress("LibGDXLogLevel") // Optional flag to make more verbose.
    override fun create() {
        super.create()

        Gdx.input = input

        setScreen(MainMenuScreen(this))
        screenshot("01_main_menu.png")

        val level = Levels.Maintenance
        val file = Gdx.files.internal("songs${File.separator}mp3${File.separator}${level.mp3Name}")
        val world = loadWorldFromMp3(file)

        val courtyardGame = PlatformGameScreen(this, world)
        setScreen(courtyardGame)

        // Start the game by initiating a jump, then wait 16 seconds to the cool part of The Courtyard
        // after 16 seconds of running, where the buildings step up quickly and consistently.
        // However, we can't just run in a straight line because it will just kill us, so instead jump
        // crazily for a while in the hope that it keeps us alive.
        input.simulatePress(Input.Keys.SPACE)
        for (i in 0..(10*4)) {
            input.simulatePress(Input.Keys.SPACE)
            timeStep(0.25f)
        }

        // Then try to double jump up in preparation for a screenshot.
        input.simulatePress(Input.Keys.SPACE)
        timeStep(0.3f)

        input.simulatePress(Input.Keys.SPACE)
        timeStep(0.3f)

        screenshot("02_in_game.png")

        timeStep(20f)
        screenshot("03_death.png")

        setScreen(PlatformGameScreen(this, world))
        screenshot("04_in_game_2.png")

        clearAllHighScores()
        clearAllAchievements()

        saveAchievements(Levels.TheLaundryRoom, listOf(DistanceX10(), DistanceX25(), DistanceX50(), DistanceX75(), ComboX5(), ComboX10(), ComboX25()))
        saveAchievements(Levels.TheCourtyard, listOf(DistanceX10(), ComboX5()))
        saveAchievements(Levels.Maintenance, listOf(DistanceX10(), DistanceX25(), DistanceX50(), ComboX5()))
        saveAchievements(Levels.ForcingTheGamecard, listOf(DistanceX10(), DistanceX25(), ComboX5(), ComboX10()))

        saveHighScore(Levels.TheLaundryRoom, Score(169986f, 0.95f))
        saveHighScore(Levels.TheCourtyard, Score(7847f, 0.23f))
        saveHighScore(Levels.Maintenance, Score(19634f, 0.66f))
        saveHighScore(Levels.ForcingTheGamecard, Score(16948f, 0.25f))

        setScreen(LevelSelectScreen(this))
        screenshot("05_level_select.png")

        setScreen(AchievementsScreen(this))
        screenshot("07_achievements.png")

        Gdx.app.exit()
    }

    private fun timeStep(seconds: Float) {
        val screen = getScreen() ?: return

        val timeStep = 1f / 60f
        var total = 0f
        while (total < seconds) {
            screen.render(timeStep)
            total += timeStep
        }
    }

    private fun screenshot(name: String) {
        // Ensure it has something to show so that a screenshot can be taken immediately after changing screens.
        screen?.render(0.0001f)
        screen?.resize(2148, 1080)

        val pixels: ByteArray = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight, true)

        // This loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
        var i = 4
        while (i <= pixels.size) {
            pixels[i - 1] = 255.toByte()
            i += 4
        }

        val pixmap = Pixmap(Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight, Pixmap.Format.RGBA8888)

        BufferUtils.copy(pixels, 0, pixmap.pixels, pixels.size)
        PixmapIO.writePNG(Gdx.files.absolute("/tmp/${name}"), pixmap)
        pixmap.dispose()
    }

    class MockInput : com.badlogic.gdx.backends.headless.mock.input.MockInput() {

        private val keyPressMap = mutableSetOf<Int>()

        fun simulatePress(key: Int) {
            keyPressMap.add(key)
        }

        override fun isKeyJustPressed(key: Int): Boolean {
            val isPressed = keyPressMap.contains(key)
            keyPressMap.remove(key)
            return isPressed
        }

    }

}
