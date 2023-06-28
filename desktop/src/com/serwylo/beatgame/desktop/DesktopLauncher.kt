package com.serwylo.beatgame.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.playground.AudioAnalysisPlaygroundGame
import java.util.Locale

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()

    val verbose = arg.contains("--verbose") || arg.contains("-v")

    val lang = arg.find { it.startsWith("--lang=") }?.substringAfter("=")
    val locale = if (lang == null) null else {
        val parts = lang.split(Regex("[-_]"))
        if (parts.size > 1) {
            Locale(parts[0], parts[1])
        } else {
            Locale(parts[0])
        }
    }

    // The playground is used to explore fourier transforms and various analysis based on the
    // results of fourier transforms. It is not intended as part of the playable game.
    val game = if (arg.contains("playground")) {
        AudioAnalysisPlaygroundGame()
    } else if (arg.contains("screenshots")) {
        config.width = 2148
        config.height = 1080
        config.title = "Beat Feet - Screenshots"
        BeatFeetGameForScreenshots(verbose)
    } else {
        BeatFeetGame(DesktopPlatformListener(), verbose, locale)
    }

    LwjglApplication(game, config)
}