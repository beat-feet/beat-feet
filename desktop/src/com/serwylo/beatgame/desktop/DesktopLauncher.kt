package com.serwylo.beatgame.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.analysis.AudioAnalysisPlaygroundGame

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()

    // The playground is used to explore fourier transforms and various analysis based on the
    // results of fourier transforms. It is not intended as part of the playable game.
    val game = if (arg.contains("playground")) AudioAnalysisPlaygroundGame() else BeatGame()

    LwjglApplication(game, config)
}