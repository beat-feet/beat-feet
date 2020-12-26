package com.serwylo.beatgame.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.google.gson.Gson
import com.serwylo.beatgame.analysis.*
import com.serwylo.beatgame.entities.Level
import com.serwylo.beatgame.fft.FFTWindow
import com.serwylo.beatgame.fft.calculateMp3FFT
import com.serwylo.beatgame.screens.PlatformGameScreen
import java.io.File
import kotlin.math.ln

private const val TAG = "LevelCache"

fun loadLevelFromMp3(musicFile: FileHandle): Level {

    val fromCache = loadFromCache(musicFile)
    if (fromCache != null) {
        Gdx.app.debug(TAG, "Loaded level from cache")
        return fromCache
    }

    Gdx.app.debug(TAG, "No cached version of level, processing MP3 from disk and caching...")
    val fromDisk = loadFromDisk(musicFile)
    cacheLeve(musicFile, fromDisk)
    return fromDisk

}

private fun loadFromDisk(musicFile: FileHandle): Level {

    Gdx.app.debug(TAG, "Generating level from ${musicFile.path()}...")

    Gdx.app.debug(TAG, "Calculating FFT")
    val spectogram = calculateMp3FFT(musicFile.read())

    Gdx.app.debug(TAG, "Extracting and smoothing features")
    val featureSeries = seriesFromFFTWindows(spectogram.windows) { it.median() }
    val smoothFeatureSeries = smoothSeriesMedian(featureSeries, 13)
    val features = extractFeaturesFromSeries(smoothFeatureSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate)

    Gdx.app.debug(TAG, "Extracting and smoothing height map")
    val heightMapSeries = seriesFromFFTWindows(spectogram.windows) { it: FFTWindow ->
        val freq = it.dominantFrequency()
        if (freq.toInt() == 0) 0.0 else ln(freq)
    }

    val smoothHeightMapSeries = smoothSeriesMean(heightMapSeries, 15)
    val heightMap = extractHeightMapFromSeries(smoothHeightMapSeries, spectogram.windowSize, spectogram.mp3Data.sampleRate, 3f)

    val music = Gdx.audio.newMusic(musicFile)

    Gdx.app.debug(TAG, "Finished generating level")
    return Level(music, heightMap, features, PlatformGameScreen.SCALE_X)

}

private fun loadFromCache(musicFile: FileHandle): Level? {

    val file = getCacheFile(musicFile).file()
    if (!file.exists()) {
        Gdx.app.debug(TAG, "Cache file for level ${musicFile.path()} at ${file.absolutePath} doesn't exist")
        return null
    }

    try {

        val json = file.readText()
        val data = Gson().fromJson(json, CacheLevelData::class.java)

        if (data.version != CacheLevelData.currentVersion) {
            Gdx.app.log(TAG, "Found cached data, but it is version ${data.version} whereas we only know how to handle version ${CacheLevelData.currentVersion} with certainty. Deleting the file and we will refresh the cache.")
            file.delete()
            return null
        }

        return Level(Gdx.audio.newMusic(musicFile), data.heightMap, data.features, PlatformGameScreen.SCALE_X)

    } catch (e: Exception) {
        // Be pretty liberal at throwing away cached files here. That gives us the freedom to change
        // the data structure if required without having to worry about if this will work or not.
        Gdx.app.error(TAG, "Error occurred while reading cache file for level ${musicFile.path()} at ${file.absolutePath}. Will remove file so it can be cached anew.", e)
        file.delete()
        return null
    }

}

private fun cacheLeve(musicFile: FileHandle, level: Level) {

    val file = getCacheFile(musicFile)

    Gdx.app.debug(TAG, "Caching level for ${musicFile.path()} to ${file.file().absolutePath}")

    val json = Gson().toJson(CacheLevelData(level.features, level.heightMap))
    file.writeString(json, false)

}

private val CACHE_DIR = ".cache${File.separator}level"

private fun getCacheFile(musicFile: FileHandle): FileHandle {

    val dir = Gdx.files.local(CACHE_DIR)
    if (!dir.exists()) {
        dir.mkdirs()
    }

    return Gdx.files.local("${CACHE_DIR}${File.separator}${musicFile.nameWithoutExtension()}.json")

}

private data class CacheLevelData(val features: List<AudioFeature>, val heightMap: Array<Vector2>) {
    val version = currentVersion

    companion object {
        // Change this every time we modify the signature of this class, to ensure we don't accidentally
        // try to process a legacy .json file of a different format after upgrading the game.
        // Bumping the version will just result in such old cache files being removed, and thus regenerated.
        const val currentVersion = 1
    }
}