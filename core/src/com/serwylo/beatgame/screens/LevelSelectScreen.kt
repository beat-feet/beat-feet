package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.BeatGame

class LevelSelectScreen(private val game: BeatGame): MenuScreen(keys, values) {

    override fun onMenuItemSelected(selectedIndex: Int) {
        val mp3 = keys[selectedIndex]
        val name = values[selectedIndex]
        game.loadGame(Gdx.files.internal(mp3), name)
    }

    companion object {

        private val keys = listOf(
                "the_haunted_mansion_the_courtyard.mp3",
                "the_haunted_mansion_the_exercise_room.mp3",
                "the_haunted_mansion_the_laundry_room.mp3",
                "the_haunted_mansion_the_ballroom.mp3",
                "vivaldi.mp3"
        )

        private val values = listOf(
                "The Courtyard",
                "The Exercise Room",
                "The Laundry Room",
                "The Ballroom",
                "Vivaldi"
        )

    }

}