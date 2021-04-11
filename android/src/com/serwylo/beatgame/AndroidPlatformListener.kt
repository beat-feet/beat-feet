package com.serwylo.beatgame

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidPlatformListener(private val context: Context) : PlatformListener {

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("Text", text)
        clipboard.setPrimaryClip(clip)
    }

}