package com.serwylo.beatgame.bin

import com.badlogic.gdx.tools.texturepacker.TexturePacker

fun main(arg: Array<String>) {
    val settings = TexturePacker.Settings()
    settings.maxWidth = 512
    settings.maxHeight = 512
    settings.grid = true

    TexturePacker.process(settings, "sprites/", "../android/assets", "sprites")
}