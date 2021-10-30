package com.serwylo.beatgame.bin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.Padding
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.*


fun main(arg: Array<String>) {
    packTextures()
    generateFonts()
}

private fun packTextures() {
    val settings = TexturePacker.Settings()
    settings.maxWidth = 2048
    settings.maxHeight = 2048
    settings.grid = false

    TexturePacker.process(settings, "sprites/", "../android/assets", "sprites")
}

private fun generateFonts() {
    HeadlessApplication(object : ApplicationAdapter() {
        override fun create() {

            val chars = requiredCharacters()

            Gdx.app.log("Font generation", "Creating fonts of size 18, 26, 58, 72 for characters: $chars")

            val fonts = mapOf(
                "noto" to Gdx.files.absolute("/usr/share/fonts/truetype/noto/NotoMono-Regular.ttf"),
                "noto_cjk" to Gdx.files.absolute("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"),
            )

            fonts.onEach { (name, font) ->
                outputFont(font, name, 18, chars, 256)
                outputFont(font, name, 26, chars, 256)
                outputFont(font, name, 58, chars, 512)
                outputFont(font, name, 72, chars, 512)
            }

            Gdx.app.exit()
        }
    })
}


/**
 * Parse every i18n properties file, and gather up every unique character that is in use.
 * Will remove duplicates and sort somewhat.
 */
private fun requiredCharacters(): String {
    val propertiesDir = FileSystems.getDefault().getPath("../android/assets/i18n")
    val chars: String = Files.newDirectoryStream(propertiesDir)
        .filter { it.toString().endsWith(".properties") }
        .map { Properties().apply {
            load(InputStreamReader(Files.newInputStream(it), Charset.forName("UTF-8")))
        }}
        .fold("0123456789xX", { allChars, properties ->
            val values = properties.values
            val allValuesInOne = values.fold("", { allMessages, message ->
                allMessages + message
            })

            allChars + allValuesInOne
        })

    return chars.toSet().sorted().joinToString("")
}

private fun outputFont(font: FileHandle, name: String, size: Int, chars: String, pageSize: Int) {

    Gdx.app.log("Font generation", "Creating fonts of size $size. Saving as ${name}_$size in skin/beat-game-skin_data/")

    val info = BitmapFontWriter.FontInfo()
    info.padding = Padding(1, 1, 1, 1)
    info.size = size

    val param = FreeTypeFontParameter()
    param.size = size
    param.characters = chars
    param.packer = PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false)

    val generator = FreeTypeFontGenerator(font)

    val data = generator.generateData(param)

    val imagePages = BitmapFontWriter.writePixmaps(
        param.packer.pages,
        Gdx.files.absolute("../skin/beat-game-skin_data/"),
        "${name}_$size"
    )

    BitmapFontWriter.writeFont(
        data,
        imagePages,
        Gdx.files.absolute("../skin/beat-game-skin_data/${name}_$size.fnt"),
        info,
        pageSize,
        pageSize
    )

}

