package com.serwylo.beatgame.audio.features

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Levels

class World(
        val musicFile: FileHandle,
        val duration: Int,
        val heightMap: Array<Vector2>,
        val featuresLow: List<Feature>,
        val featuresMid: List<Feature>,
        val featuresHigh: List<Feature>
) {

    fun level(): Level {
        return Levels.bySong(musicFile.name())
    }

}