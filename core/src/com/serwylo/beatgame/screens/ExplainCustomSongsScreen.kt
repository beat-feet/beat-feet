package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.audio.customMp3

class ExplainCustomSongsScreen(private val game: BeatGame): InfoScreen(
        game,
        "Custom Songs",
        "Want a level that matches your favourite song?\n\nCopy an MP3 file to:\n\n${customMp3().file().absolutePath}\n\nWe'll generate a level just for you!"
) {

    override fun show() {
        super.show()

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE || keycode == Input.Keys.BACK) {
                    game.showLevelSelectMenu()
                    return true
                }

                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                game.showLevelSelectMenu()
                return true
            }

        }

    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

}