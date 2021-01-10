package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.serwylo.beatgame.BeatGame

class AboutScreen(private val game: BeatGame): InfoScreen(
        "Credits",
        "Music\nThe Haunted Mansion / CC-BY-SA 3.0\nJohn Harrison w/ Wichita State University Chamber - CC-BY-SA 3.0\n\nGraphics: Kenney.nl / CCO 1.0",
        "logo"
) {

    override fun show() {
        super.show()

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                }

                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                game.showMenu()
                return true
            }

        }

    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

}