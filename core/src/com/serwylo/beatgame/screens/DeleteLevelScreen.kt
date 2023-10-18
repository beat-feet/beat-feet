package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.serwylo.beatgame.BeatFeetGame
import com.serwylo.beatgame.levels.CustomLevel
import com.serwylo.beatgame.levels.deleteCustomLevel
import com.serwylo.beatgame.ui.UI_SPACE
import com.serwylo.beatgame.ui.makeButton
import com.serwylo.beatgame.ui.makeHeading
import com.serwylo.beatgame.ui.makeStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeleteLevelScreen(private val game: BeatFeetGame, private val level: CustomLevel): ScreenAdapter() {

    private val stage = makeStage()
    private lateinit var btnConfirm: Cell<Button>
    private lateinit var btnCancel: Cell<Button>

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    init {
        val sprites = game.assets.getSprites()
        val styles = game.assets.getStyles()
        val strings = game.assets.getStrings()

        stage.addActor(Table().apply {
            pad(UI_SPACE)
            padTop(UI_SPACE * 2)
            setFillParent(true)

            add(
                makeHeading("Delete Level", sprites.logo, styles, strings) {
                    game.showLevelSelectMenu(level.getWorld())
                }
            ).top().expandY().colspan(2)

            row()

            add(
                Label("Delete level, including achievements and scores?", styles.label.medium)
            ).spaceBottom(UI_SPACE).colspan(2)

            row()

            add(
                Label(level.getLabel(strings), styles.label.small)
            ).spaceBottom(UI_SPACE).colspan(2)

            row()

            btnConfirm = add(
                makeButton("Delete", styles) {
                    onConfirmDelete()
                }
            ).right()

            btnCancel = add(
                makeButton("Cancel", styles) {
                    game.showLevelSelectMenu(level.getWorld())
                }
            ).left()

            row()

            add().expandY()
        })
    }

    private fun onConfirmDelete() {
        // This wont visually change the buttons, but it will prevent them registering
        // a click event which will stop race conditions with deleting level-related data twice.
        btnConfirm.actor.isDisabled = true
        btnCancel.actor.isDisabled = true

        scope.launch(Dispatchers.IO) {
            val world = deleteCustomLevel(level)
            game.showLevelSelectMenu(world)
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

}