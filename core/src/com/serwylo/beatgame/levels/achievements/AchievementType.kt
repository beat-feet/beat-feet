package com.serwylo.beatgame.levels.achievements

abstract class AchievementType(
        val id: String,
        val label: String
)

class FinishedLevel: AchievementType("finished-level", "Finished Level")
class ComboX10: AchievementType("combo-10", "10 x Combo")
