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
    val game = if (arg.contains("playground")) AudioAnalysisPlaygroundGame() else BeatFeetGame(verbose)

    LwjglApplication(game, config)
}