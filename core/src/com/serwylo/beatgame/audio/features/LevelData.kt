package com.serwylo.beatgame.audio.features

import com.badlogic.gdx.math.Vector2

class LevelData(
        val duration: Int,
        val heightMap: Array<Vector2>,
        val featuresLow: List<Feature>,
        val featuresMid: List<Feature>,
        val featuresHigh: List<Feature>
)