package com.serwylo.beatgame.bin

import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.serwylo.beatgame.desktop.SongExtract

fun main(arg: Array<String>) {
    HeadlessApplication(SongExtract(arg))
}

