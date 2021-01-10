package com.serwylo.beatgame.screens

import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.BeatGame
import com.serwylo.beatgame.Score
import com.serwylo.beatgame.audio.customMp3

class LevelSelectScreen(private val game: BeatGame): MenuScreen(keys, values) {

    override fun onMenuItemSelected(selectedIndex: Int) {
        val mp3 = keys[selectedIndex]
        val name = values[selectedIndex]

        if (mp3 == "custom") {
            val file = customMp3()
            if (!file.exists()) {
                game.explainCustomSongs()
            } else {
                // If the file has changed, clear the high score.
                val score = Score.load(file.name())
                if (file.file().lastModified() > score.timestamp) {
                    Score.save(file.name(), 0f, 0, true)
                }

                game.loadGame(file, "{Custom}")
            }
        } else {
            game.loadGame(Gdx.files.internal(mp3), name)
        }

    }

    companion object {

        private val keys = listOf(
                "the_haunted_mansion_the_courtyard.mp3",
                "the_haunted_mansion_the_exercise_room.mp3",
                "the_haunted_mansion_the_laundry_room.mp3",
                "the_haunted_mansion_the_ballroom.mp3",
                "vivaldi.mp3",
                "custom"
        )

        private val values = listOf(
                "The Courtyard",
                "The Exercise Room",
                "The Laundry Room",
                "The Ballroom",
                "Vivaldi",
                "{Custom}"
        )

    }

}