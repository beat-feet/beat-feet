package com.serwylo.beatgame.levels

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Since
import com.mpatric.mp3agic.Mp3File
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*


private const val TAG = "customLevels"

private const val CUSTOM_LEVELS_JSON_VERSION = 1.0

private val gson = GsonBuilder().setPrettyPrinting().setVersion(CUSTOM_LEVELS_JSON_VERSION).create()

fun customWorldFile(): FileHandle = Gdx.files.local("custom-worlds.json")

fun loadCustomWorld(): World? {
    val file = customWorldFile()
    return if (!file.exists() && LegacyCustomLevel.getMp3File().exists()) {
        Gdx.app.log(TAG, "Existing custom level exists, so will create a custom world for the first time.")
        createCustomWorld()
    } else if (!file.exists()) {
        null
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
        listOf(CustomWorldDTO.CustomLevelDTO("custom.mp3", readMp3Title(LegacyCustomLevel.getMp3File()), LegacyCustomLevel.getMp3File().file().absolutePath))
    } else {
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
        mp3File.nameWithoutExtension()
    }
}

fun customLevelMp3Folder() = Gdx.files.external("BeatFeet").child("songs")

fun addCustomLevel(sourceMp3: FileHandle): CustomWorld {
    val title = readMp3Title(sourceMp3)

    Gdx.app.log(TAG, "Adding new custom level. Song title: \"$title\".")
    val destMp3File = customLevelMp3Folder().child(sanitiseFilename(title) + ".mp3")

    Gdx.app.log(TAG, "Copying ${sourceMp3.path()} to ${destMp3File.path()}")
    sourceMp3.file().copyTo(destMp3File.file())

    val jsonFile = customWorldFile()
    val existingDto = gson.fromJson(jsonFile.readString(), CustomWorldDTO::class.java)
    val newLevelDto = CustomWorldDTO.CustomLevelDTO(
        destMp3File.nameWithoutExtension(),
        title,
        destMp3File.file().absolutePath
    )
    val newDto = existingDto.copy(levels = existingDto.levels + newLevelDto)
    jsonFile.writeString(gson.toJson(newDto), false)

    return newDto.toCustomWorld()
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
