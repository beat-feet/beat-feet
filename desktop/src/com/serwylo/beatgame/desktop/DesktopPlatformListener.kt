package com.serwylo.beatgame.desktop

import com.serwylo.beatgame.PlatformListener
import games.spooky.gdx.nativefilechooser.NativeFileChooser
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class DesktopPlatformListener: PlatformListener {

    override fun copyToClipboard(text: String) {
        Toolkit.getDefaultToolkit()?.systemClipboard?.setContents(StringSelection(text), null)
    }

    override fun fileChooser(): NativeFileChooser {
        return DesktopFileChooser()
    }

}