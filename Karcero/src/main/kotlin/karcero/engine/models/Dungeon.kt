package karcero.engine.models

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.processors.*

class Dungeon(val width: Int, val height: Int) {
    /**
     * Two dimensional array representing the cells in the dungeon.
     */
    val grid = Array(width, { row -> Array(height, { column -> DungeonCell(row, column) }) })

    /**
     * The collection of rooms that this dungeon possesses.
     */
    val rooms = mutableListOf<DungeonRoom>()

    /**
     * Collect all cells within the dungeon as a list.
     */
    val allCells: Iterable<DungeonCell>
        get() {
            return mutableListOf<DungeonCell>().apply {
                grid.forEach { row ->
                    row.forEach { cell ->
                        add(cell)
                    }
                }
            }.toList()
        }

    /**
     * Attempt to get the cell adjacent to the given cell, returns null if there is no adjacent cell.
     */
    fun getAdjacentCell(cell: DungeonCell, direction: Direction, distance: Int = 1): DungeonCell? {
        return when (direction) {
            Direction.NORTH -> tryGetCell(cell.row - distance, cell.column)
            Direction.EAST -> tryGetCell(cell.row, cell.column + distance)
            Direction.SOUTH -> tryGetCell(cell.row + distance, cell.column)
            Direction.WEST -> tryGetCell(cell.row, cell.column - distance)
        }
    }

    /**
     * Attempt to get the cell at the given coordinate, returns null if there is no such cell.
     */
    fun tryGetCell(row: Int, column: Int): DungeonCell? {
        return if (row >= 0 && column >= 0 && row < height && column < width) grid[row][column] else null
    }

    /**
     * Get the cell at the given coordinate - no checking is performed, so getting a cell at a coordinate
     * outside that is outside the grid bounds will cause an exception.
     */
    fun getCell(row: Int, column: Int): DungeonCell {
        return grid[row][column]
    }

    /**
     * Get all the cells that a particular room has.
     */
    fun getRoomCells(room: DungeonRoom): Iterable<DungeonCell> {
        return mutableListOf<DungeonCell>().apply {
            for (row in room.row until Math.min(room.bottom, height)) {
                for (column in room.column until Math.min(room.right, width)) {
                    add(getCell(row, column))
                }
            }
        }.toList()
    }

    companion object {
        /**
         * Create a new dungeon with the given properties. If any pre or post processors are
         * passed, they will be evaluated during the dungeon construction.
         */
        fun create(configuration: DungeonConfiguration = DungeonConfiguration.createDefault(),
                   preProcessors: Iterable<DungeonProcessor>? = null,
                   postProcessors: Iterable<DungeonProcessor>? = null): Dungeon {

            val randomizer = Randomizer()
            var dungeon = Dungeon(configuration.width, configuration.height)
            MazeProcessor().process(dungeon, configuration, randomizer)
            SparsenessProcessor().process(dungeon, configuration, randomizer)
            DeadEndsProcessor().process(dungeon, configuration, randomizer)

            preProcessors?.forEach {
                it.process(dungeon, configuration, randomizer)
            }

            dungeon = createRefinedDungeon(dungeon)

            RoomProcessor().process(dungeon, configuration, randomizer)
            DoorProcessor().process(dungeon, configuration, randomizer)
            WallsProcessor().process(dungeon, configuration, randomizer)

            postProcessors?.forEach {
                it.process(dungeon, configuration, randomizer)
            }

            return dungeon
        }

        /**
         * Inflate the dungeon to double its size and coerce the maze into being more usable to us as a dungeon.
         */
        private fun createRefinedDungeon(dungeon: Dungeon): Dungeon {
            val newDungeon = Dungeon(dungeon.width * 2 + 1, dungeon.height * 2 + 1)

            dungeon.allCells.filter { it.isOpen }.forEach { oldCell ->
                val newCell = newDungeon.getCell(oldCell.row * 2 + 1, oldCell.column * 2 + 1)
                newCell.terrainType = TerrainType.FLOOR
                oldCell.passableSides.filter { it.value }.forEach {
                    newDungeon.getAdjacentCell(newCell, it.key)?.let {
                        it.terrainType = TerrainType.FLOOR
                    }
                }
            }

            return newDungeon
        }
    }
}
