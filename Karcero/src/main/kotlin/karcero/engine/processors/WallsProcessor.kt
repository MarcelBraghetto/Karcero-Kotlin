package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.*

class WallsProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        dungeon.allCells.forEach { cell ->
            processWallType(dungeon, cell, Direction.NORTH)
            processWallType(dungeon, cell, Direction.EAST)
            processWallType(dungeon, cell, Direction.SOUTH)
            processWallType(dungeon, cell, Direction.WEST)
        }
    }

    private companion object {
        fun processWallType(dungeon: Dungeon, cell: DungeonCell, direction: Direction) {
            val adjacentCell = dungeon.getAdjacentCell(cell, direction)
            cell.passableSides[direction] = false

            // There is no adjacent cell in the given direction, so it is out of bounds.
            if (adjacentCell == null) {
                cell.walls[direction] = WallType.SOLID
                return
            }

            when (adjacentCell.terrainType) {
                TerrainType.ROCK -> {
                    cell.walls[direction] = WallType.SOLID
                }

                TerrainType.DOOR_NORTH_SOUTH -> {
                    cell.walls[direction] = WallType.DOOR_NORTH_SOUTH
                    cell.passableSides[direction] = true
                }

                TerrainType.DOOR_EAST_WEST -> {
                    cell.walls[direction] = WallType.DOOR_EAST_WEST
                    cell.passableSides[direction] = true
                }

                else -> {
                    cell.walls[direction] = WallType.NONE
                    cell.passableSides[direction] = true
                }
            }
        }
    }
}