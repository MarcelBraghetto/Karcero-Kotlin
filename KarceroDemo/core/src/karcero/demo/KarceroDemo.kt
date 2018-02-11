package karcero.demo

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import karcero.engine.models.Direction
import karcero.engine.models.Dungeon
import karcero.engine.models.TerrainType

class KarceroDemo : ApplicationAdapter() {
    private val cameraSpeed = 20.0f
    private val cameraZoomSpeed = 3.0f

    private lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var renderer: ShapeRenderer
    private lateinit var dungeon: Dungeon

    // Used to paint each room a random colour to show where they are in the dungeon
    private val roomColours = listOf(
            Color.NAVY,
            Color.ROYAL,
            Color.SLATE,
            Color.SKY,
            Color.CYAN,
            Color.TEAL,
            Color.GREEN,
            Color.CHARTREUSE,
            Color.LIME,
            Color.FOREST,
            Color.OLIVE,
            Color.YELLOW,
            Color.GOLD,
            Color.GOLDENROD,
            Color.ORANGE,
            Color.BROWN,
            Color.TAN,
            Color.FIREBRICK,
            Color.RED,
            Color.SCARLET,
            Color.CORAL,
            Color.SALMON,
            Color.PINK,
            Color.MAGENTA,
            Color.PURPLE,
            Color.VIOLET,
            Color.MAROON
    )

    override fun create() {
        batch = SpriteBatch()
        renderer = ShapeRenderer()
        camera = OrthographicCamera().apply {
            zoom = 1.33f
            position.x = 25.5f
            position.y = 24.5f
        }

        // Create a world viewport 40 units wide and tall. Each dungeon cell will be 1 unit in size.
        viewport = FitViewport(40.0f, 40.0f, camera)

        // Create a dungeon instance with default configuration. A custom configuration can also be used.
        dungeon = Dungeon.create()
    }

    override fun render() {
        processInput()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        renderer.projectionMatrix = camera.combined
        batch.begin()

        drawFloors()
        drawWalls()

        batch.end()
    }

    private fun processInput() {
        val deltaTime = Gdx.graphics.deltaTime

        // Arrow keys to move the dungeon around, W/S to zoom in and out.
        camera.run {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) zoom -= cameraZoomSpeed * deltaTime
            if (Gdx.input.isKeyPressed(Input.Keys.S)) zoom += cameraZoomSpeed * deltaTime
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) position.x -= cameraSpeed * deltaTime
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) position.x += cameraSpeed * deltaTime
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) position.y += cameraSpeed * deltaTime
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) position.y -= cameraSpeed * deltaTime
            update()
        }
    }

    private fun drawFloors() {
        renderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw all the non-room cell floors first.
        dungeon.grid.forEach { row ->
            row.forEach { cell ->

                when (cell.terrainType) {
                    TerrainType.ROCK -> renderer.color = Color.DARK_GRAY
                    else -> renderer.color = Color.GRAY
                }

                if (!cell.isInRoom) {
                    renderer.rect(cell.column.toFloat(), cell.row.toFloat(), 1f, -1f)
                }
            }
        }

        // Then go to each room in the dungeon and draw it in a different colour.
        dungeon.rooms.forEach { room ->
            val colour = roomColours[room.roomId % roomColours.size]

            dungeon.getRoomCells(room).forEach { cell ->
                renderer.color = colour
                renderer.rect(cell.column.toFloat(), cell.row.toFloat(), 1f, -1f)
            }
        }

        renderer.end()
    }

    private fun drawWalls() {
        renderer.color = Color.WHITE
        renderer.begin(ShapeRenderer.ShapeType.Line)

        dungeon.grid.forEach { row ->
            row.forEach { cell ->

                if (cell.terrainType == TerrainType.DOOR_NORTH_SOUTH) {
                    renderer.line(cell.column.toFloat() + 0.0f, cell.row.toFloat() - 0.5f,
                            cell.column.toFloat() + 1.0f, cell.row.toFloat() - 0.5f)
                }

                if (cell.terrainType == TerrainType.DOOR_EAST_WEST) {
                    renderer.line(cell.column.toFloat() + 0.5f, cell.row.toFloat(),
                            cell.column.toFloat() + 0.5f, cell.row.toFloat() - 1.0f)
                }

                if (cell.terrainType != TerrainType.ROCK) {
                    if (!cell.isPassable(Direction.NORTH)) {
                        renderer.line(cell.column.toFloat() + 0.0f, cell.row.toFloat() - 1.0f,
                                cell.column.toFloat() + 1.0f, cell.row.toFloat() - 1.0f)
                    }

                    if (!cell.isPassable(Direction.EAST)) {
                        renderer.line(cell.column.toFloat() + 1.0f, cell.row.toFloat() - 1.0f,
                                cell.column.toFloat() + 1.0f, cell.row.toFloat() - 0.0f)
                    }

                    if (!cell.isPassable(Direction.SOUTH)) {
                        renderer.line(cell.column.toFloat() + 0.0f, cell.row.toFloat() - 0.0f,
                                cell.column.toFloat() + 1.0f, cell.row.toFloat() - 0.0f)
                    }

                    if (!cell.isPassable(Direction.WEST)) {
                        renderer.line(cell.column.toFloat() + 0.0f, cell.row.toFloat() - 1.0f,
                                cell.column.toFloat() + 0.0f, cell.row.toFloat() - 0.0f)
                    }
                }
            }
        }

        renderer.end()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun dispose() {
        batch.dispose()
        renderer.dispose()
    }
}
