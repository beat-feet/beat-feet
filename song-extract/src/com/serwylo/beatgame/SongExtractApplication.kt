package com.serwylo.beatgame.desktop

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.serwylo.beatgame.audio.loadFromDisk
import com.serwylo.beatgame.audio.saveWorldToDisk
import java.io.File

class SongExtract(private var arg: Array<String>): ApplicationAdapter() {

    override fun create() {

        if (arg.contains("--verbose")) {
            arg = arg.filter { it != "--verbose" }.toTypedArray()
            Gdx.app.logLevel = Application.LOG_DEBUG
        }

        if (arg.size != 2) {
            return usage()
        }

        val srcDir = File(arg[0])
        val destDir = File(arg[1])

        if (!srcDir.exists()) {
            return usage("Source MP3 directory $srcDir does not exist")
        }

        srcDir.listFiles()?.forEach {
            if (it.extension != "mp3") {
                Gdx.app.log(TAG, "Skipping non-MP3 file $it.")
            } else {
                processFile(it, destDir)
            }
        }

        Gdx.app.exit()

    }

    private fun processFile(mp3File: File, destDir: File) {

        val outPath = "${destDir.absolutePath}${File.separator}${mp3File.nameWithoutExtension}.json"
        val outFile = Gdx.files.absolute(outPath)

        if (outFile.exists()) {
            Gdx.app.log(TAG, "Skipping ${mp3File.name} as it already has a data file at $outPath.")
            return
        }

        Gdx.app.log(TAG, "Processing ${mp3File.name}, writing to ${outPath}.")

        val world = loadFromDisk(Gdx.files.absolute(mp3File.path))
        saveWorldToDisk(outFile, world)

    }

    companion object {

        private const val TAG = "SongExtract"

        fun usage(error: String? = null) {
            if (error != null) {
                Gdx.app.error(TAG, error)
            }

            Gdx.app.error(TAG, "Usage: song-extract src-mp3-dir/ dest-data-dir/")
            Gdx.app.exit()
        }

    }

}

