package com.serwylo.beatgame.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.google.gson.Gson
import com.serwylo.beatgame.audio.features.Feature
import com.serwylo.beatgame.audio.features.World
import com.serwylo.beatgame.audio.fft.FFTWindow
import com.serwylo.beatgame.audio.fft.calculateMp3FFT
import com.serwylo.beatgame.screens.PlatformGameScreen
import java.io.File
import kotlin.math.ln

private const val TAG = "WorldCache"

fun loadWorldFromMp3(musicFile: FileHandle): World {

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

private fun loadFromDisk(musicFile: FileHandle): World {

    Gdx.app.debug(TAG, "Generating world from ${musicFile.path()}...")

    Gdx.app.debug(TAG, "Calculating FFT")
    val spectogram = calculateMp3FFT(musicFile.read())

    Gdx.app.debug(TAG, "Extracting and smoothing features")
    val featureSeries = com.serwylo.beatgame.audio.playground.seriesFromFFTWindows(spectogram.windows) { it.median() }
    val smoothFeatureSeries = com.serwylo.beatgame.audio.playground.smoothSeriesMedian(featureSeries, 13)
    val features = com.serwylo.beatgame.audio.playground.extractFeaturesFromSeries(smoothFeatureSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate)

    Gdx.app.debug(TAG, "Extracting and smoothing height map")
    val heightMapSeries = com.serwylo.beatgame.audio.playground.seriesFromFFTWindows(spectogram.windows) { it: FFTWindow ->
        val freq = it.dominantFrequency()
        if (freq.toInt() == 0) 0.0 else ln(freq)
    }

    val smoothHeightMapSeries = com.serwylo.beatgame.audio.playground.smoothSeriesMean(heightMapSeries, 15)
    val heightMap = com.serwylo.beatgame.audio.playground.extractHeightMapFromSeries(smoothHeightMapSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate, 3f)

    val music = Gdx.audio.newMusic(musicFile)

    Gdx.app.debug(TAG, "Finished generating world")
    return World(music, heightMap, features, PlatformGameScreen.SCALE_X)

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

        return World(Gdx.audio.newMusic(musicFile), data.heightMap, data.features, PlatformGameScreen.SCALE_X)

    } catch (e: Exception) {
        // Be pretty liberal at throwing away cached files here. That gives us the freedom to change
        // the data structure if required without having to worry about if this will work or not.
        Gdx.app.error(TAG, "Error occurred while reading cache file for world ${musicFile.path()} at ${file.absolutePath}. Will remove file so it can be cached anew.", e)
        file.delete()
        return null
    }

}

private fun cacheWorld(musicFile: FileHandle, world: World) {

    val file = getCacheFile(musicFile)

    Gdx.app.debug(TAG, "Caching world for ${musicFile.path()} to ${file.file().absolutePath}")

    val json = Gson().toJson(CachedWorldData(world.features, world.heightMap))
    file.writeString(json, false)

}

private val CACHE_DIR = ".cache${File.separator}world"

private fun getCacheFile(musicFile: FileHandle): FileHandle {

    val dir = Gdx.files.local(CACHE_DIR)
    if (!dir.exists()) {
        dir.mkdirs()
    }

    return Gdx.files.local("${CACHE_DIR}${File.separator}${musicFile.nameWithoutExtension()}.json")

}

private data class CachedWorldData(val features: List<Feature>, val heightMap: Array<Vector2>) {
    val version = currentVersion

    companion object {
        // Change this every time we modify the signature of this class, to ensure we don't accidentally
        // try to process a legacy .json file of a different format after upgrading the game.
        // Bumping the version will just result in such old cache files being removed, and thus regenerated.
        const val currentVersion = 1
    }
}