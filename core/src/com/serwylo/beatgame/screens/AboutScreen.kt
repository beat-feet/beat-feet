package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.serwylo.beatgame.BeatGame

class AboutScreen(private val game: BeatGame): InfoScreen(
        game,
        "Credits",
        CREDITS,
        game.assets.getSprites().logo
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

    companion object {
        private const val CREDITS = """
Music:
The Haunted Mansion / CC-BY-SA 3.0
Awakening / CC-BY-SA 3.0
Health and Safety / CC-BY-SA 3.0
John Harrison w/ Wichita State University Chamber / CC-BY-SA 3.0

Graphics:
Kenney.nl / CCO 1.0
disabledpaladin / CC-BY-SA 4.0
        """
    }

}