package com.serwylo.beatgame.audio.features

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals
import com.serwylo.beatgame.levels.Level
import com.serwylo.beatgame.levels.Level.Companion.levels
import kotlin.math.max
import kotlin.math.min

class World(
        val music: Music,
        val musicFileName: String,
        val duration: Int,
        val heightMap: Array<Vector2>,
        val featuresLow: List<Feature>,
        val featuresMid: List<Feature>,
        val featuresHigh: List<Feature>
) {

    fun level(): Level {
        return levels.find { it.mp3Name == musicFileName }!!
    }

    fun dispose() {
        music.stop()
        music.dispose()
    }

}