package com.serwylo.beatgame.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.audio.playground.AudioAnalysisPlaygroundGame

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()

    val verbose = arg.contains("--verbose") || arg.contains("-v")

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
        BeatFeetGame(DesktopPlatformListener(), verbose)
    }

    LwjglApplication(game, config)
}