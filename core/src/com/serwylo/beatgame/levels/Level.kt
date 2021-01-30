package com.serwylo.beatgame.levels

data class Level(
        val mp3Name: String,
        val label: String
) {

    companion object {
        val levels = listOf(
                Level("health_and_safety_maintenance.mp3", "Maintenance"), // Nice and short, perhaps a good intro song.
                Level("the_haunted_mansion_the_courtyard.mp3", "The Courtyard"),
                Level("health_and_safety_forcing_the_gamecard.mp3", "Forcing the Gamecard"),
                Level("health_and_safety_sharply_bent_wire.mp3", "Sharply Bent Wire"),
                Level("the_haunted_mansion_the_laundry_room.mp3", "The Laundry Room"),
                Level("health_and_safety_eye_twitching.mp3", "Eye Twitching"),
                Level("health_and_safety_light_flashes.mp3", "Light Flashes"),
                Level("health_and_safety_play_in_a_well_lit_room.mp3", "Play in a Well Lit Room"),
                Level("health_and_safety_contact_with_moisture_and_dirt.mp3", "Contact with Moisture and Dirt"),
                Level("the_haunted_mansion_the_ballroom.mp3", "The Ballroom"),
                Level("awakenings_old_clock.mp3", "Old Clock"),
                Level("health_and_safety_regulations_for_equipment_use.mp3", "Regulations for Equipment"),
                Level("health_and_safety_convulsions.mp3", "Convulsions"),
                Level("health_and_safety_contact_with_dust_and_lint.mp3", "Contact with Dust and Lint"),
                Level("the_haunted_mansion_the_exercise_room.mp3", "The Exercise Room"),
                Level("vivaldi.mp3", "Vivaldi"),
                Level("health_and_safety_reorient_the_receiving_antenna.mp3", "Reorient the Receiving Antenna"), // 17min long song... should we?
                Level("custom.mp3", "{Custom}")
        )
    }

}

