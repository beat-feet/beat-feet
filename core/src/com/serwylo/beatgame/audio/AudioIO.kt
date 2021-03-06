package com.serwylo.beatgame.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.google.gson.Gson
import com.serwylo.beatgame.audio.features.Feature
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.audio.fft.FFTWindow
import com.serwylo.beatgame.audio.fft.calculateMp3FFTWithValues
import com.serwylo.beatgame.audio.playground.*
import java.io.File
import kotlin.math.ln

private const val TAG = "WorldCache"

fun loadWorldFromMp3(musicFile: FileHandle): World {

    val precompiled = loadPrecompiled(musicFile)
    if (precompiled != null) {
        Gdx.app.debug(TAG, "Loaded precompiled world")
        return precompiled
    }

    val fromCache = loadFromCache(musicFile)
    if (fromCache != null) {
        Gdx.app.debug(TAG, "Loaded world from cache")
        return fromCache
    }

    Gdx.app.debug(TAG, "No cached version of world, processing MP3 from disk and caching...")
    val fromDisk = loadFromDisk(musicFile)
    cacheWorld(musicFile, fromDisk)
    return fromDisk

}

fun customMp3(): FileHandle {
    return Gdx.files.external("BeatChange${File.separator}custom.mp3")
}

fun loadFromDisk(musicFile: FileHandle): World {

    Gdx.app.debug(TAG, "Generating world from ${musicFile.path()}...")

    Gdx.app.debug(TAG, "Calculating FFT")
    val spectogram = calculateMp3FFTWithValues(musicFile.read())
    // val spectogram = smoothFFT(rawSpectogram, 13).toResult()

    Gdx.app.debug(TAG, "Extracting and smoothing features")
    val extractors = arrayOf(
            { window: FFTWindow -> window.meanFirst },
            { window: FFTWindow -> window.meanSecond },
            { window: FFTWindow -> window.meanThird }
    )

    val features = extractors.map {
        val featureSeries = seriesFromFFTWindows(spectogram.windows, it)
        val smoothFeatureSeries = smoothSeriesMedian(featureSeries, 13)
        extractFeaturesFromSeries(smoothFeatureSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate)
    }

    Gdx.app.debug(TAG, "Extracting and smoothing height map")
    val heightMapSeries = seriesFromFFTWindows(spectogram.windows) { it: FFTWindow ->
        val freq = it.dominantFrequency
        if (freq.toInt() == 0) 0.0 else ln(freq)
    }

    val smoothHeightMapSeries = smoothSeriesMean(heightMapSeries, 15)
    val heightMap = extractHeightMapFromSeries(smoothHeightMapSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate, 3f)

    Gdx.app.debug(TAG, "Samples: ${spectogram.mp3Data.pcmSamples.size} @ ${spectogram.mp3Data.sampleRate}Hz (duration: ${spectogram.mp3Data.pcmSamples.size / spectogram.mp3Data.sampleRate})")
    val duration = spectogram.mp3Data.pcmSamples.size / spectogram.mp3Data.sampleRate

    Gdx.app.debug(TAG, "Finished generating world")
    return World(musicFile, duration, heightMap, features[0], features[1], features[2])

}

private fun loadFromCache(musicFile: FileHandle): World? {

    val file = getCacheFile(musicFile).file()
    if (!file.exists()) {
        Gdx.app.debug(TAG, "Cache file for world ${musicFile.path()} at ${file.absolutePath} doesn't exist")
        return null
    }

    try {

        val json = file.readText()
        val data = Gson().fromJson(json, CachedWorldData::class.java)

        if (data.version != CachedWorldData.currentVersion) {
            Gdx.app.log(TAG, "Found cached data, but it is version ${data.version} whereas we only know how to handle version ${CachedWorldData.currentVersion} with certainty. Deleting the file and we will refresh the cache.")
            file.delete()
            return null
        }

        return World(musicFile, data.duration, arrayOf(), data.featuresLow, data.featuresMid, data.featuresHigh)

    } catch (e: Exception) {
        // Be pretty liberal at throwing away cached files here. That gives us the freedom to change
        // the data structure if required without having to worry about if this will work or not.
        Gdx.app.error(TAG, "Error occurred while reading cache file for world ${musicFile.path()} at ${file.absolutePath}. Will remove file so it can be cached anew.", e)
        file.delete()
        return null
    }

}

private fun loadPrecompiled(musicFile: FileHandle): World? {

    val file = getPrecompiledFile(musicFile)
    if (file == null || !file.exists()) {
        // Could be the case for custom songs, or for when we are experimenting adding new songs.
        Gdx.app.debug(TAG, "Precompiled file for world ${musicFile.path()} doesn't exist")
        return null
    }

    try {

        val json = file.readString()
        val data = Gson().fromJson(json, CachedWorldData::class.java)

        if (data.version != CachedWorldData.currentVersion) {
            error("Precompiled world data is version ${data.version}, whereas we only know how to handle version ${CachedWorldData.currentVersion} with certainty. Perhaps we need to compile again using :song-extract:processSongs?")
        }

        return World(musicFile, data.duration, arrayOf(), data.featuresLow, data.featuresMid, data.featuresHigh)

    } catch (e: Exception) {
        // Be pretty liberal at throwing away cached files here. That gives us the freedom to change
        // the data structure if required without having to worry about if this will work or not.
        throw RuntimeException("Error while reading precompiled world data for ${musicFile.path()}.", e)
    }

}

private fun cacheWorld(musicFile: FileHandle, world: World) {

    val file = getCacheFile(musicFile)

    Gdx.app.debug(TAG, "Caching world for ${musicFile.path()} to ${file.file().absolutePath}")

    saveWorldToDisk(file, world)

}

fun saveWorldToDisk(file: FileHandle, world: World) {

    val json = Gson().toJson(CachedWorldData(world.duration, world.featuresLow, world.featuresMid, world.featuresHigh))
    file.writeString(json, false)

}

private val CACHE_DIR = ".cache${File.separator}world"

private fun getCacheFile(musicFile: FileHandle): FileHandle {

    val dir = Gdx.files.local(CACHE_DIR)
    if (!dir.exists()) {
        dir.mkdirs()
    }

    val name = if (musicFile.nameWithoutExtension() == "custom") {
        "custom-${musicFile.lastModified()}"
    } else {
        musicFile.nameWithoutExtension()
    }

    return Gdx.files.local("${CACHE_DIR}${File.separator}$name.json")

}

private fun getPrecompiledFile(musicFile: FileHandle): FileHandle? {

    val name = musicFile.nameWithoutExtension()
    if (name == "custom") {
        return null
    }

    val dataFile = Gdx.files.internal("songs${File.separator}data${File.separator}${name}.json")
    if (dataFile.exists()) {
        return dataFile
    }

    return null

}

/**
 * This used to save the height map, but seeing as we are not using that yet, don't save it. It takes
 * up several hundred kilobytes for each song which we just don't need (even though it compresses well).
 * Once we decide to do something interesting with the heightmap, add it back to the constructor
 * and bump the version number.
 */
private data class CachedWorldData(
        val duration: Int,
        val featuresLow: List<Feature>,
        val featuresMid: List<Feature>,
        val featuresHigh: List<Feature>
) {

    val version = currentVersion

    companion object {

        /**
         * Change this every time we modify the signature of this class, to ensure we don't accidentally
         * try to process a legacy .json file of a different format after upgrading the game.
         * Bumping the version will just result in such old cache files being removed, and thus regenerated.
         */
        const val currentVersion = 2

    }

}