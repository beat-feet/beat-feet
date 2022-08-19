package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.World
import com.serwylo.beatgame.levels.loadAllWorlds
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.newSingleThreadAsyncContext
import ktx.async.onRenderingThread

class AboutScreen(private val game: BeatFeetGame): ScreenAdapter() {

    private val stage = makeStage()
    private val wrapper: Cell<Actor>

    private val job = Job()
    private val scope = CoroutineScope(newSingleThreadAsyncContext("LoadingScreen") + job)

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        stage.addActor(
            ScrollPane(
                Table().apply {
                    add(
                        makeHeading(strings["about.title"], sprites.logo, styles, strings) {
                            game.showMenu()
                        }
                    )
                    row()
                    wrapper = add().expand()
                    wrapper.setActor<Label>(
                        Label(strings["loading-screen.loading"], styles.label.medium).apply {
                            color.a
                            addAction(
                                Actions.sequence(
                                    Actions.delay(0.5f),
                                    Actions.fadeIn(0.2f),
                                )
                            )
                        }
                    )
                }
            ).apply {
                setFillParent(true)
                setScrollingDisabled(true, false)
                setupOverscroll(width / 4, 30f, 200f)
            }
        )

        scope.launch {
            val worlds = loadAllWorlds()
            onRenderingThread {
                wrapper.setActor(
                    VerticalGroup().also { container ->

                        container.space(UI_SPACE)

                        credits(strings, worlds).entries.forEach { entry ->

                            val heading = entry.key
                            val values = entry.value

                            container.addActor(
                                Label(heading, styles.label.large).apply {
                                    setAlignment(Align.center)
                                }
                            )

                            values.entries.forEach { (label, url) ->

                                container.addActor(
                                    Label(label, styles.label.medium).apply {
                                        setAlignment(Align.center)
                                        touchable = Touchable.enabled
                                        onClick {
                                            Gdx.net.openURI(url)
                                        }
                                    }
                                )

                            }
                        }
                    }
                )
            }
        }

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.inputProcessor = InputMultiplexer(stage, object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    game.showMenu()
                    return true
                }

                return false
            }

        })
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    private fun credits(strings: I18NBundle, worlds: List<World>) = mapOf(

        // Intentionally leave these un-internationalised, because they need to refer back
        // to the original source and license to avoid ambiguity

        strings["about.credits.graphics"] to mapOf(
            strings["about.credits.graphics-kenney"] to "https://kenney.nl",
            strings["about.credits.graphics-disabledpaladin"] to "https://www.deviantart.com/disabledpaladin",
        ),

        strings["about.credits.music"] to worlds
            .map { it.getAttribution() }
            .flatten()
            .associate {
                listOfNotNull(it.name, it.author, it.license).joinToString(" / ") to it.sourceUrl
            },

    )

}