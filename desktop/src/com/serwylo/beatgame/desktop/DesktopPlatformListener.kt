package com.serwylo.beatgame.desktop

import com.serwylo.beatgame.PlatformListener
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.lang.IllegalStateException

class DesktopPlatformListener: PlatformListener {

    override fun copyToClipboard(text: String) {
        Toolkit.getDefaultToolkit()?.systemClipboard?.setContents(StringSelection(text), null)
    }

}