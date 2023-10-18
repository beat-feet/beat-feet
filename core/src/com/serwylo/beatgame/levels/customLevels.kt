package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Since
import com.mpatric.mp3agic.Mp3File
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.achievements.clearAllAchievements
import com.serwylo.beatgame.levels.achievements.deleteAchievementsForLevel
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FilenameFilter


private const val TAG = "customLevels"

private const val CUSTOM_LEVELS_JSON_VERSION = 1.0

private val gson = GsonBuilder().setPrettyPrinting().setVersion(CUSTOM_LEVELS_JSON_VERSION).create()

fun loadCustomWorld(): World {
    val file = customWorldFile()
    return if (!file.exists() && LegacyCustomLevel.getMp3File().exists()) {
        Gdx.app.log(TAG, "Existing custom level exists, so will create a custom world for the first time.")
        createCustomWorld()
    } else if (!file.exists()) {
        createCustomWorld()
    } else {
        Gdx.app.log(TAG, "Loading custom world from ${file.path()}")
        val dto = gson.fromJson(file.readString(), CustomWorldDTO::class.java)
        dto.toCustomWorld()
    }
}

fun createCustomWorld(): CustomWorld {
    val customWorldFile = customWorldFile()

    Gdx.app.log(TAG, "Creating custom world for first time. Saving to ${customWorldFile.path()}.")
    val initialLevels: List<CustomWorldDTO.CustomLevelDTO> = if (LegacyCustomLevel.getMp3File().exists()) {
        Gdx.app.log(TAG, "Migrating legacy custom level ${LegacyCustomLevel.getMp3File().path()} to new custom world.")

        // Hardcode the id to "custom.mp3" to ensure we retain any achievements from the previous
        // attempts at this level.
        val legacyCustomLevelDto = copyExternalMp3ToGameFolder(LegacyCustomLevel.getMp3File()).copy(id = "custom.mp3")

        // Copy custom level data if it exists (it takes a while to generate, so don't
        // penalise those with slow phones by making them generate again.
        if (LegacyCustomLevel.getLevelDataFile().exists()) {
            // Shouldn't matter that the world passed in here is a bit junkey, because we are only
            // interested in the getLevelDataFile() function which we are (fairly) sure doesn't
            // depend on this.
            val customLevel = CustomLevel(CustomWorld(emptyList()), legacyCustomLevelDto.id, legacyCustomLevelDto.label, LegacyCustomLevel.getMp3File())

            Gdx.app.log(TAG, "Migrating legacy custom level data from ${LegacyCustomLevel.getLevelDataFile().path()} to new custom world (${customLevel.getLevelDataFile()}).")
            LegacyCustomLevel.getLevelDataFile().moveTo(customLevel.getLevelDataFile())
        }

        // We really should delete this now, but I just can't bring myself to right now, because
        // it may cause people to lose a file they manually put here which they want back one day.
        // Hence, the following line is commented out:
        // LegacyCustomLevel.getMp3File().delete()

        listOf(legacyCustomLevelDto)
    } else {
        Gdx.app.log(TAG, "No legacy custom level in existence, will not attempt to migrate it.")
        emptyList()
    }

    val worldDTO = CustomWorldDTO(initialLevels)
    customWorldFile.writeString(gson.toJson(worldDTO), false)

    return worldDTO.toCustomWorld()
}

fun readMp3Title(mp3File: FileHandle): String {
    val mp3file = Mp3File(mp3File.file().absolutePath)
    val titleTag: String? = if(mp3file.hasId3v2Tag()) {
        Gdx.app.log(TAG, "Reading filename from id3v2Tag title: ${mp3file.id3v2Tag.title}")
        mp3file.id3v2Tag.title
    } else if (mp3file.hasId3v1Tag()) {
        Gdx.app.log(TAG, "Reading filename from id3v1Tag title: ${mp3file.id3v2Tag.title}")
        mp3file.id3v1Tag.title
    } else {
        null
    }

    return if (titleTag != null) titleTag else {
        Gdx.app.log(TAG, "No id3v1 or id3v2 title tags present, falling back to filename.")

        // Trim leading '~' from filename because the way the file provider + native-file-chooser
        // library (not sure which one) works is to copy the file into /data/user/0/com.serwylo.beatgame/cache/
        // first, and prefix it with that character.
        mp3File.nameWithoutExtension().trimStart('~')
    }
}

fun customWorldFile(): FileHandle = Gdx.files.local("custom-world").child("world.json")

fun customLevelDataFile(level: CustomLevel): FileHandle {
    return Gdx.files.local("custom-world")
        .child("${level.getId()}.json")
}

fun customMp3File(levelId: String): FileHandle {
    return Gdx.files.local("custom-world")
        .child("${levelId}.mp3")
}

fun customLevelId(mp3Title: String) = sanitiseFilename(mp3Title)

fun copyExternalMp3ToGameFolder(sourceMp3: FileHandle): CustomWorldDTO.CustomLevelDTO {
    val title = readMp3Title(sourceMp3)

    Gdx.app.log(TAG, "Adding new custom level. Song title: \"$title\".")

    val levelId = customLevelId(title)
    val destMp3File = customMp3File(levelId)

    Gdx.app.log(TAG, "Copying ${sourceMp3.path()} to ${destMp3File.path()}")
    sourceMp3.file().copyTo(destMp3File.file())

    return CustomWorldDTO.CustomLevelDTO(
        levelId,
        title,
        destMp3File.file().absolutePath
    )
}

suspend fun deleteCustomLevel(level: CustomLevel): CustomWorld = withContext(Dispatchers.IO) {
    val jsonFile = customWorldFile()
    val existingWorldDto = gson.fromJson(jsonFile.readString(), CustomWorldDTO::class.java)
    val newWorldDto = existingWorldDto.copy(levels = existingWorldDto.levels.filter { it.id != level.getId() })
    jsonFile.writeString(gson.toJson(newWorldDto), false)

    if (level.getLevelDataFile().exists()) {
        level.getLevelDataFile().delete()
    }

    level.getMp3File().delete()

    deleteAchievementsForLevel(level)
    deleteHighScoresForLevel(level)

    newWorldDto.toCustomWorld()
}

fun addCustomLevel(sourceMp3: FileHandle): CustomWorld {
    val jsonFile = customWorldFile()
    val existingWorldDto = gson.fromJson(jsonFile.readString(), CustomWorldDTO::class.java)
    val newLevelDto = copyExternalMp3ToGameFolder(sourceMp3)
    val newWorldDto = existingWorldDto.copy(levels = existingWorldDto.levels + newLevelDto)
    jsonFile.writeString(gson.toJson(newWorldDto), false)

    return newWorldDto.toCustomWorld()
}

fun onAddNewLevel(game: BeatFeetGame, onAdded: (world: CustomWorld) -> Unit) {
    val conf = NativeFileChooserConfiguration()
    conf.directory = Gdx.files.absolute(System.getProperty("user.home"));

    // Filter out all files which do not have the .ogg extension and are not of an audio MIME type - belt and braces
    conf.mimeFilter = "audio/*"
    conf.nameFilter = FilenameFilter { dir, name -> name.endsWith("mp3") }
    conf.title = "Choose MP3 file";


    game.platformListener.fileChooser().chooseFile(conf, object : NativeFileChooserCallback {
        override fun onFileChosen(file: FileHandle) {
            val world = addCustomLevel(file)
            onAdded(world)
        }

        override fun onCancellation() {
        }

        override fun onError(exception: Exception) {
        }
    })
}

data class CustomWorldDTO(

    @SerializedName("levels")
    @Since(1.0)
    val levels: List<CustomLevelDTO>

) {
    fun toCustomWorld(): CustomWorld = CustomWorld(levels)

    data class CustomLevelDTO(
        @SerializedName("id")
        @Since(1.0)
        val id: String,

        @SerializedName("label")
        @Since(1.0)
        val label: String,

        @SerializedName("mp3Path")
        @Since(1.0)
        val mp3Path: String,
    )
}
