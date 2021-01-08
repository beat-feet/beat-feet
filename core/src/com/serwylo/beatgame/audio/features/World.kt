package com.serwylo.beatgame.audio.features

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.serwylo.beatgame.Globals
import kotlin.math.max
import kotlin.math.min

class World(val music: Music, val duration: Int, val heightMap: Array<Vector2>, val features: List<Feature>) {

    fun dispose() {
        music.stop()
        music.dispose()
    }

}