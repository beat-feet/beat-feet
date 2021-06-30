package com.serwylo.beatgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AssetLoader
import com.badlogic.gdx.assets.loaders.I18NBundleLoader
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Logger
import com.crashinvaders.vfx.VfxManager
import com.crashinvaders.vfx.effects.VignettingEffect
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets
import java.util.*


@Suppress("PropertyName") // Allow underscores in variable names here, because it better reflects the source files things come from.
class Assets(private val locale: Locale) {

    private val manager = AssetManager()
    private lateinit var skin: Skin
    private lateinit var styles: Styles
    private lateinit var sprites: Sprites
    private lateinit var particles: Particles
    private lateinit var sounds: Sounds
    private lateinit var effects: Effects

    @GDXAssets(propertiesFiles = ["android/assets/i18n/messages.properties"])
    private lateinit var strings: I18NBundle

    fun initSync() {

        manager.load("i18n/messages", I18NBundle::class.java, I18NBundleLoader.I18NBundleParameter(locale))
        manager.load("skin.json", Skin::class.java)
        manager.load("sprites.atlas", TextureAtlas::class.java)
        manager.load("effects/rainbow.p", ParticleEffect::class.java)
        manager.load("effects/health.p", ParticleEffect::class.java)
        manager.load("effects/shield.p", ParticleEffect::class.java)

        // TODO: This doubles the loading time on my PC from 200ms to 400ms. Is it worth it?
        //       Perhaps we could procedurally generate the sound instead as it is relatively straightforward.
        manager.load("sounds/vibraphone_base_pitch.mp3", Sound::class.java)

        val startTime = System.currentTimeMillis()
        Gdx.app.debug(TAG, "Loading assets...")

        manager.finishLoading()

        effects = Effects()
        strings = manager.get("i18n/messages")
        skin = manager.get("skin.json")

        styles = Styles(skin, locale)
        sprites = Sprites(manager.get("sprites.atlas"))
        particles = Particles(manager)
        sounds = Sounds(manager)

        Gdx.app.debug(TAG, "Finished loading assets (${System.currentTimeMillis() - startTime}ms)")

    }

    fun getStrings() = strings
    fun getSkin() = skin
    fun getStyles() = styles
    fun getSprites() = sprites
    fun getParticles() = particles
    fun getSounds() = sounds
    fun getEffects() = effects

    class Effects {

        private val manager = VfxManager(Pixmap.Format.RGBA8888)

        init {
            manager.addEffect(VignettingEffect(false).apply {
                setCenter(0.4f, 0.5f)
                vignetteX = 0.85f
                vignetteY = 0.15f
            })
        }

        fun render(closure: () -> Unit) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

            manager.cleanUpBuffers()
            manager.beginInputCapture()

            closure()

            manager.endInputCapture()
            manager.applyEffects()
            manager.renderToScreen()

            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        fun resize(width: Int, height: Int) {
            manager.resize(width, height)
        }

    }

    /**
     * Depending on the [locale], we may need to use some different fonts.
     * The default font set is based on Kenney's great pixel fonts. However, they are only able to
     * maintain a character set that is so large. Therefore, to cater for a broader range of
     * characters beyond the bulk of the ASCII set, we fall back to using the Google Noto font family.
     */
    class Styles(private val skin: Skin, private val locale: Locale) {

        private val useNoto = localeRequiresNotoFonts(locale)

        val label = Labels()
        val textButton = TextButtons()

        inner class Labels {

            val small = skin.get("small", Label.LabelStyle::class.java)
            val medium = skin.get("default", Label.LabelStyle::class.java)
            val large = skin.get("large", Label.LabelStyle::class.java)
            val huge = skin.get("huge", Label.LabelStyle::class.java)

            init {
                if (useNoto) {
                    val smallNoto = skin.get("small-noto", Label.LabelStyle::class.java)
                    val mediumNoto = skin.get("default-noto", Label.LabelStyle::class.java)
                    val largeNoto = skin.get("large-noto", Label.LabelStyle::class.java)
                    val hugeNoto = skin.get("huge-noto", Label.LabelStyle::class.java)

                    huge.font = hugeNoto.font
                    large.font = largeNoto.font
                    medium.font = mediumNoto.font
                    small.font = smallNoto.font
                }
            }
        }

        inner class TextButtons {
            val small = skin.get("small", TextButton.TextButtonStyle::class.java)
            val medium = skin.get("default", TextButton.TextButtonStyle::class.java)
            val large = skin.get("large", TextButton.TextButtonStyle::class.java)
            val huge = skin.get("huge", TextButton.TextButtonStyle::class.java)

            init {
                if (useNoto) {

                    val smallNoto = skin.get("small-noto", TextButton.TextButtonStyle::class.java)
                    val mediumNoto = skin.get("default-noto", TextButton.TextButtonStyle::class.java)
                    val largeNoto = skin.get("large-noto", TextButton.TextButtonStyle::class.java)
                    val hugeNoto = skin.get("huge-noto", TextButton.TextButtonStyle::class.java)

                    huge.font = hugeNoto.font
                    large.font = largeNoto.font
                    medium.font = mediumNoto.font
                    small.font = smallNoto.font
                }
            }
        }
    }

    class Particles(manager: AssetManager) {
        val jump: ParticleEffect = manager.get("effects/rainbow.p")
        val health: ParticleEffect = manager.get("effects/health.p")
        val shield: ParticleEffect = manager.get("effects/shield.p")
    }

    class Sounds(manager: AssetManager) {
        val scale: Sound = manager.get("sounds/vibraphone_base_pitch.mp3")
    }

    class Sprites(atlas: TextureAtlas) {
        val barrel_a = atlas.findRegion("barrel_a")
        val barrel_b = atlas.findRegion("barrel_b")
        val barrel_c = atlas.findRegion("barrel_c")
        val barrier_a = atlas.findRegion("barrier_a")
        val barrier_b = atlas.findRegion("barrier_b")
        val barrier_c = atlas.findRegion("barrier_c")
        val barrier_d = atlas.findRegion("barrier_d")
        val bin_medium_a = atlas.findRegion("bin_medium_a")
        val bin_medium_b = atlas.findRegion("bin_medium_b")
        val bin_small_a = atlas.findRegion("bin_small_a")
        val bin_small_b = atlas.findRegion("bin_small_b")
        val box_large = atlas.findRegion("box_large")
        val box_medium = atlas.findRegion("box_medium")
        val box_small = atlas.findRegion("box_small")
        val building_a_bottom_left = atlas.findRegion("building_a_bottom_left")
        val building_a_bottom = atlas.findRegion("building_a_bottom")
        val building_a_bottom_right = atlas.findRegion("building_a_bottom_right")
        val building_a_inner = atlas.findRegion("building_a_inner")
        val building_a_left = atlas.findRegion("building_a_left")
        val building_a_right = atlas.findRegion("building_a_right")
        val building_a_top_left = atlas.findRegion("building_a_top_left")
        val building_a_top = atlas.findRegion("building_a_top")
        val building_a_top_right = atlas.findRegion("building_a_top_right")
        val building_b_bottom_left = atlas.findRegion("building_b_bottom_left")
        val building_b_bottom = atlas.findRegion("building_b_bottom")
        val building_b_bottom_right = atlas.findRegion("building_b_bottom_right")
        val building_b_inner = atlas.findRegion("building_b_inner")
        val building_b_left = atlas.findRegion("building_b_left")
        val building_b_right = atlas.findRegion("building_b_right")
        val building_b_top_left = atlas.findRegion("building_b_top_left")
        val building_b_top = atlas.findRegion("building_b_top")
        val building_b_top_right = atlas.findRegion("building_b_top_right")
        val building_c_bottom_left = atlas.findRegion("building_c_bottom_left")
        val building_c_bottom = atlas.findRegion("building_c_bottom")
        val building_c_bottom_right = atlas.findRegion("building_c_bottom_right")
        val building_c_inner = atlas.findRegion("building_c_inner")
        val building_c_left = atlas.findRegion("building_c_left")
        val building_c_right = atlas.findRegion("building_c_right")
        val building_c_top_left = atlas.findRegion("building_c_top_left")
        val building_c_top = atlas.findRegion("building_c_top")
        val building_c_top_right = atlas.findRegion("building_c_top_right")
        val bush_medium_a = atlas.findRegion("bush_medium_a")
        val bush_medium_b = atlas.findRegion("bush_medium_b")
        val bush_medium_c = atlas.findRegion("bush_medium_c")
        val bush_small_a = atlas.findRegion("bush_small_a")
        val bush_small_b = atlas.findRegion("bush_small_b")
        val bush_small_c = atlas.findRegion("bush_small_c")
        val character_a_dance_a = atlas.findRegion("character_a_dance_a")
        val character_a_dance_b = atlas.findRegion("character_a_dance_b")
        val character_a_duck = atlas.findRegion("character_a_duck")
        val character_a_face = atlas.findRegion("character_a_face")
        val character_a_face_small = atlas.findRegion("character_a_face_small")
        val character_a_front = atlas.findRegion("character_a_front")
        val character_a_hit = atlas.findRegion("character_a_hit")
        val character_a_jump = atlas.findRegion("character_a_jump")
        val character_a_walk = atlas.findRegions("character_a_walk")
        val cloud_a = atlas.findRegion("cloud_a")
        val cloud_b = atlas.findRegion("cloud_b")
        val cloud_c = atlas.findRegion("cloud_c")
        val cloud_d = atlas.findRegion("cloud_d")
        val cloud_e = atlas.findRegion("cloud_e")
        val cloud_f = atlas.findRegion("cloud_f")
        val cloud_g = atlas.findRegion("cloud_g")
        val cloud_h = atlas.findRegion("cloud_h")
        val cloud_i = atlas.findRegion("cloud_i")
        val cloud_j = atlas.findRegion("cloud_j")
        val cloud_k = atlas.findRegion("cloud_k")
        val door_a_closed = atlas.findRegion("door_a_closed")
        val door_a_covered = atlas.findRegion("door_a_covered")
        val door_a_open = atlas.findRegion("door_a_open")
        val door_b_closed = atlas.findRegion("door_b_closed")
        val door_b_covered = atlas.findRegion("door_b_covered")
        val door_b_open = atlas.findRegion("door_b_open")
        val door_c_closed = atlas.findRegion("door_c_closed")
        val door_c_covered = atlas.findRegion("door_c_covered")
        val door_c_open = atlas.findRegion("door_c_open")
        val door_d_closed = atlas.findRegion("door_d_closed")
        val door_d_covered = atlas.findRegion("door_d_covered")
        val door_d_open = atlas.findRegion("door_d_open")
        val door_e_closed = atlas.findRegion("door_e_closed")
        val door_e_covered = atlas.findRegion("door_e_covered")
        val door_e_open = atlas.findRegion("door_e_open")
        val door_f_closed = atlas.findRegion("door_f_closed")
        val door_f_covered = atlas.findRegion("door_f_covered")
        val door_f_open = atlas.findRegion("door_f_open")
        val fence_inner_broken_a = atlas.findRegion("fence_inner_broken_a")
        val fence_inner_broken_b = atlas.findRegion("fence_inner_broken_b")
        val fence_inner = atlas.findRegion("fence_inner")
        val fence_left = atlas.findRegion("fence_left")
        val fence_right = atlas.findRegion("fence_right")
        val ghost = atlas.findRegion("ghost")
        val ghost_x = atlas.findRegion("ghost_x")
        val ground_a = atlas.findRegion("ground_a")
        val ground_b = atlas.findRegion("ground_b")
        val ground_c = atlas.findRegion("ground_c")
        val ground_d = atlas.findRegion("ground_d")
        val ground_e = atlas.findRegion("ground_e")
        val ground_f = atlas.findRegion("ground_f")
        val heart_empty = atlas.findRegion("heart_empty")
        val heart_half = atlas.findRegion("heart_half")
        val heart = atlas.findRegion("heart")
        val hydrant = atlas.findRegion("hydrant")
        val logo = atlas.findRegion("logo")
        val mail_box = atlas.findRegion("mail_box")
        val particle_pixel = atlas.findRegion("particle_pixel")
        val rainbow_bar = atlas.findRegion("rainbow_bar")
        val right_sign = atlas.findRegion("right_sign")
        val score = atlas.findRegion("score")
        val shield = atlas.findRegion("shield")
        val shield_empty = atlas.findRegion("shield_empty")
        val shield_half = atlas.findRegion("shield_half")
        val shield_full = atlas.findRegion("shield_full")
        val skyline_day = atlas.findRegion("skyline_day")
        val skyline_sunset = atlas.findRegion("skyline_sunset")
        val skyline_evening = atlas.findRegion("skyline_evening")
        val star = atlas.findRegion("star")
        val streetlight_a_base = atlas.findRegion("streetlight_a_base")
        val streetlight_a_post = atlas.findRegion("streetlight_a_post")
        val streetlight_a_top = atlas.findRegion("streetlight_a_top")
        val streetlight_b_base = atlas.findRegion("streetlight_b_base")
        val streetlight_b_post = atlas.findRegion("streetlight_b_post")
        val streetlight_b_top = atlas.findRegion("streetlight_b_top")
        val streetlight_c_base = atlas.findRegion("streetlight_c_base")
        val streetlight_c_post = atlas.findRegion("streetlight_c_post")
        val streetlight_c_top = atlas.findRegion("streetlight_c_top")
        val streetlight_d_base = atlas.findRegion("streetlight_d_base")
        val streetlight_d_post = atlas.findRegion("streetlight_d_post")
        val streetlight_d_top = atlas.findRegion("streetlight_d_top")
        val streetlight_e_base = atlas.findRegion("streetlight_e_base")
        val streetlight_e_post = atlas.findRegion("streetlight_e_post")
        val streetlight_e_top = atlas.findRegion("streetlight_e_top")
        val streetlight_f_base = atlas.findRegion("streetlight_f_base")
        val streetlight_f_post = atlas.findRegion("streetlight_f_post")
        val streetlight_f_top = atlas.findRegion("streetlight_f_top")
        val tyre = atlas.findRegion("tyre")
        val tyres_large = atlas.findRegion("tyres_large")
        val tyres_medium = atlas.findRegion("tyres_medium")
        val tyres_small = atlas.findRegion("tyres_small")
        val wall_a_inner = atlas.findRegion("wall_a_inner")
        val wall_a_left = atlas.findRegion("wall_a_left")
        val wall_a_right = atlas.findRegion("wall_a_right")
        val wall_b_inner = atlas.findRegion("wall_b_inner")
        val wall_b_left = atlas.findRegion("wall_b_left")
        val wall_b_right = atlas.findRegion("wall_b_right")
        val wall_c_inner = atlas.findRegion("wall_c_inner")
        val wall_c_left = atlas.findRegion("wall_c_left")
        val wall_c_right = atlas.findRegion("wall_c_right")
        val window_steel_a = atlas.findRegion("window_steel_a")
        val window_steel_b = atlas.findRegion("window_steel_b")
        val window_steel_c = atlas.findRegion("window_steel_c")
        val window_steel_d = atlas.findRegion("window_steel_d")
        val window_steel_e = atlas.findRegion("window_steel_e")
        val window_steel_f = atlas.findRegion("window_steel_f")
        val window_steel_g = atlas.findRegion("window_steel_g")
        val window_steel_h = atlas.findRegion("window_steel_h")
        val window_steel_i = atlas.findRegion("window_steel_i")
        val window_steel_j = atlas.findRegion("window_steel_j")
        val window_wood_a = atlas.findRegion("window_wood_a")
        val window_wood_b = atlas.findRegion("window_wood_b")
        val window_wood_c = atlas.findRegion("window_wood_c")
        val window_wood_d = atlas.findRegion("window_wood_d")
        val window_wood_e = atlas.findRegion("window_wood_e")
        val window_wood_f = atlas.findRegion("window_wood_f")
        val window_wood_g = atlas.findRegion("window_wood_g")
        val window_wood_h = atlas.findRegion("window_wood_h")
        val window_wood_i = atlas.findRegion("window_wood_i")
        val window_wood_j = atlas.findRegion("window_wood_j")
    }

    companion object {

        private const val TAG = "Assets"

        /**
         * Unfortunately despite the awesome effort of community translators, some languages
         * are yet to be supported.
         *
         * Part of that is something we can solve, by choosing a bitmap font with a wider set of
         * characters. This can be done, e.g. using tools such as Heiro (https://github.com/libgdx/libgdx/wiki/Hiero)
         * and fonts such as Noto. Using such a font will cover a bigger range of characters, but
         * lose the nice styles provided by the Kenney fonts used now. Therefore, it would be great
         * to only enable these fonts for languages which require them, and leave Kenney fonts for others.
         *
         * The other part is an issue with libgdx, whereby it doesn't natively support RTL languages,
         * or languages where glyphs are combined together such as Persian. This will be harder to
         * accomplish unfortunately.
         */
        private val supportedLocales = setOf(
            // "bn", // Glyphs are currently unsupported.
            "de",
            "en",
            "eo",
            "es",
            // "fa", // Glyphs and RTL currently not supported.
            "fr",
            "id",
            "it",
            "mk",
            "nb",
            "pl",
            "pt",
            "ru",
            "vi"
        )

        private val notoLocales = setOf(
            "vi", "ru", "pl", "mk"
        )

        private fun isLocaleSupported(locale: Locale): Boolean {
            val country = locale.language.toLowerCase(Locale.ENGLISH)
            return supportedLocales.contains(country)
        }

        private fun localeRequiresNotoFonts(locale: Locale): Boolean {
            val country = locale.language.toLowerCase(Locale.ENGLISH)
            return notoLocales.contains(country)
        }

        fun getLocale(): Locale {
            // Even though Weblate is allowing this game to be translated into many different languages,
            // only some of them are supported by libgdx. Ensure that we don't pick up an unsupported locale
            // which *does* have translation files available, because it will render invalid glyphs and
            // make the game unusable.
            val systemLocale = Locale.getDefault()

            return if (isLocaleSupported(systemLocale)) {
                systemLocale
            } else {
                Gdx.app.error(TAG, "Unsupported locale: $systemLocale, falling back to English.")
                Locale.ENGLISH
            }
        }

        private val SCALE_SOUND_FILES = listOf(
                "n01.mp3",
                "n02.mp3",
                "n03.mp3",
                "n04.mp3",
                "n05.mp3",
                "n06.mp3",
                "n07.mp3",
                "n08.mp3",
                "n09.mp3",
                "n10.mp3",
                "n11.mp3",
                "n12.mp3",
                "n13.mp3",
                "n14.mp3",
                "n15.mp3",
                "n16.mp3",
                "n17.mp3",
                "n18.mp3",
                "n19.mp3",
                "n20.mp3",
                "n21.mp3",
                "n22.mp3",
                "n23.mp3",
                "n24.mp3"
        )

    }

}
