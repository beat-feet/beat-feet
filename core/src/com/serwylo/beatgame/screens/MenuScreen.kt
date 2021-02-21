package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.serwylo.beatgame.BeatGame

abstract class MenuScreen(
        protected val keys: List<String>,
        private val values: List<String>,
        title: String? = null,
        titleSprite: TextureRegion? = null
): ScreenAdapter() {

    private val stage = Stage(FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))

    private var bigFont = BitmapFont().apply { data.scale(0.5f) }
    private var mediumFont = BitmapFont().apply { data.scale(-0.2f) }

    private val shapeRenderer = ShapeRenderer(50)
    private val labels: List<TextButton>
    private val inputProcessor: InputProcessor

    private val selectedBackground = object: Actor() {
        override fun draw(batch: Batch?, parentAlpha: Float) {
            batch?.end()

            shapeRenderer.projectionMatrix = batch?.projectionMatrix
            shapeRenderer.transformMatrix = batch?.transformMatrix
            shapeRenderer.color = Color.DARK_GRAY
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.rect(x, y, width, height)
            shapeRenderer.end()

            batch?.begin()
        }
    }

    private var selectedIndex = 0

    private fun up() {
        selectedIndex --
        if (selectedIndex == -1) {
            selectedIndex = keys.size - 1
        }

        updateSelectedBackground()
    }

    private fun down() {
        selectedIndex = (selectedIndex + 1) % keys.size
        updateSelectedBackground()
    }

    private fun updateSelectedBackground() {
        val label = labels[selectedIndex]
        selectedBackground.x = label.x
        selectedBackground.y = label.y
        selectedBackground.width = label.width
        selectedBackground.height = label.height
    }

    protected abstract fun onMenuItemSelected(selectedIndex: Int)

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    init {
        val group = VerticalGroup()
        group.setFillParent(true)
        group.pad(PADDING)
        group.space(ITEM_SPACE)
        stage.addActor(group)

        group.addActor(selectedBackground)

        if (title != null) {
            val horizontalGroup = HorizontalGroup()
            horizontalGroup.space(10f)

            if (titleSprite != null) {
                val image = Image(titleSprite)
                horizontalGroup.addActor(image)
            }

            val titleStyle = Label.LabelStyle()
            titleStyle.font = bigFont

            val titleLabel = Label(title, titleStyle)
            horizontalGroup.addActor(titleLabel)

            group.addActor(horizontalGroup)

        }

        val labelStyle = TextButton.TextButtonStyle()
        labelStyle.font = mediumFont

        labels = values.mapIndexed { i, item ->

            val label = TextButton(item, labelStyle)
            label.setPosition(0f, (values.size - i) * ITEM_HEIGHT)
            label.height = ITEM_HEIGHT
            label.width = stage.width
            label.pad(ITEM_SPACE)
            label.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    onMenuItemSelected(i)
                }
            })

            group.addActor(label)

            label

        }

        inputProcessor = InputMultiplexer(stage, object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.UP) {

                    up()
                    return true

                } else if (keycode == Input.Keys.DOWN) {

                    down()
                    return true

                } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {

                    onMenuItemSelected(selectedIndex)
                    return true

                }

                return false
            }

        })
    }

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor

        stage.draw() // Do this to force a layout calculation, so that we can set the background to the right place.
        updateSelectedBackground()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

    }

    override fun dispose() {
        stage.dispose()
    }

    companion object {

        @JvmStatic
        private val VIEWPORT_WIDTH = 400f

        @JvmStatic
        private val VIEWPORT_HEIGHT = 200f

        @JvmStatic
        private val ITEM_HEIGHT = 20f

        @JvmStatic
        private val PADDING = 20f

        @JvmStatic
        private val ITEM_SPACE = ITEM_HEIGHT / 5

    }
}