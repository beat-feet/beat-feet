package com.serwylo.beatgame

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.badlogic.gdx.backends.android.AndroidApplication
import games.spooky.gdx.nativefilechooser.NativeFileChooser
import games.spooky.gdx.nativefilechooser.android.AndroidFileChooser

class AndroidPlatformListener(private val context: Context, private val application: AndroidApplication) : PlatformListener {

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("Text", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun fileChooser(): NativeFileChooser {
        return AndroidFileChooser(application)
    }

}