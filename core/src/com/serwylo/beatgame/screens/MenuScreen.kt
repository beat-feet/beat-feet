package com.serwylo.beatgame.screens

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.TextureArray
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

abstract class MenuScreen(
        protected val keys: List<String>,
        protected val values: List<String>,
        private val title: String? = null,
        private val titleSprite: String? = null
): ScreenAdapter() {

    private val stage = Stage(FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))

    protected var bigFont = BitmapFont().apply { data.scale(0.5f) }
    protected var mediumFont = BitmapFont().apply { data.scale(-0.2f) }
    protected var smallFont = BitmapFont().apply { data.scale(-0.5f) }

    private val shapeRenderer = ShapeRenderer(50)
    private lateinit var labels: List<TextButton>

    private val selectedBackground = object: Actor() {
        override fun draw(batch: Batch?, parentAlpha: Float) {
            batch?.end()

            shapeRenderer.projectionMatrix = batch?.projectionMatrix;
            shapeRenderer.transformMatrix = batch?.transformMatrix;
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

    override fun show() {

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
                val atlas = TextureAtlas(Gdx.files.internal("sprites.atlas"))
                val texture = atlas.findRegion(titleSprite)
                val image = Image(texture)
                horizontalGroup.addActor(image)
            }

            val titleStyle = Label.LabelStyle()
            titleStyle.font = bigFont

            val title = Label(title, titleStyle)
            horizontalGroup.addActor(title)

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
            label.addListener(object: ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    onMenuItemSelected(i)
                }
            })

            group.addActor(label)

            label

        }

        stage.draw() // Do this to force a layout calculation, so that we can set the background to the right place.
        updateSelectedBackground()

        Gdx.input.inputProcessor = InputMultiplexer(stage, object : InputAdapter() {

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

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

    }

    override fun dispose() {
        stage.dispose()
        Gdx.input.inputProcessor = null
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