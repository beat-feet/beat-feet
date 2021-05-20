package com.serwylo.beatgame

import de.tomgrill.gdxtesting.GdxTestRunner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(GdxTestRunner::class)
class I18nTests {

    private fun locales() = listOf(
        Locale.ENGLISH,
        Locale("bn"),
        Locale("de"),
        Locale("es"),
        Locale("fa"),
        Locale("fr"),
        Locale("it"),
        Locale("mk"),
        Locale("nb", "NO"),
        Locale("pl")
    )

    @Test
    fun testStringFormats() {
        locales().forEach { locale ->
            val assets = Assets(locale)
            assets.initSync()

            val strings = assets.getStrings()

            val string = strings.format("achievements.num-left", 1)

            assertNotNull(string)
            assertNotEquals("", string)
        }

    }

}
