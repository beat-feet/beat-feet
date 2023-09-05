package com.serwylo.beatgame

import games.spooky.gdx.nativefilechooser.NativeFileChooser

interface PlatformListener {
    fun copyToClipboard(text: String)

    fun fileChooser(): NativeFileChooser
}