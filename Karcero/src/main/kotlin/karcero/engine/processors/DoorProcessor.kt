package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.*

class DoorProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        dungeon.rooms.forEach { room ->
            val roomCells = dungeon.getRoomCells(room)

            // North edge of room cells
            processEdgeCells(dungeon, roomCells.filter { it.row == room.row }, Direction.NORTH, configuration, randomizer)

            // East edge of room cells
            processEdgeCells(dungeon, roomCells.filter { it.column == room.right - 1 }, Direction.EAST, configuration, randomizer)

            // South edge of room cells
            processEdgeCells(dungeon, roomCells.filter { it.row == room.bottom - 1 }, Direction.SOUTH, configuration, randomizer)

            // West edge of room cells
            processEdgeCells(dungeon, roomCells.filter { it.column == room.column }, Direction.WEST, configuration, randomizer)
        }
    }

    private companion object {
        fun processEdgeCells(dungeon: Dungeon, edgeCells: Iterable<DungeonCell>, direction: Direction,
                             configuration: DungeonConfiguration, randomizer: Randomizer) {

            for (cell in edgeCells) {
                // Grab the adjacent cell in the direction required to be a corridor.
                val adjacentCell = dungeon.getAdjacentCell(cell, direction) ?: continue

                // The adjacent cell needs to be clear and available.
                if (adjacentCell.terrainType != TerrainType.FLOOR) {
                    continue
                }

                // Check that the adjacent cell also has at least one more cell beyond it.
                val nextAdjacentCell = dungeon.getAdjacentCell(adjacentCell, direction) ?: continue

                // The next adjacent cell also needs to be clear and available.
                if (nextAdjacentCell.terrainType != TerrainType.FLOOR) {
                    continue
                }

                // There must be an adjacent cell to the left of the adjacent cell.
                val leftCell = dungeon.getAdjacentCell(adjacentCell, direction.rotate(clockwise = false)) ?: continue

                // The left cell must be 'rock' else this cannot be a corridor.
                if (leftCell.terrainType != TerrainType.ROCK) {
                    continue
                }

                // There must be an adjacent cell to the right of the adjacent cell.
                val rightCell = dungeon.getAdjacentCell(adjacentCell, direction.rotate(clockwise = true)) ?: continue

                // The right cell must be 'rock' else this cannot be a corridor.
                if (rightCell.terrainType != TerrainType.ROCK) {
                    continue
                }

                // If we reach this point, it means that the current edge cell of the room also has
                // a clear corridor available reaching at least two cells deep for which the first
                // cell along the corridor is flanked on both passableSides by rock. This represents a valid
                // location to place a door. The final step is to randomly decide if we should place
                // a door at this location.
                if (randomizer.getRandomDouble() <= configuration.chanceToPlaceRoomDoor) {
                    adjacentCell.terrainType = when (direction) {
                        Direction.NORTH, Direction.SOUTH -> TerrainType.DOOR_NORTH_SOUTH
                        else -> TerrainType.DOOR_EAST_WEST
                    }
                }
            }
        }
    }
}
