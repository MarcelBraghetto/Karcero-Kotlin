package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.Direction
import karcero.engine.models.Dungeon
import karcero.engine.models.DungeonCell
import karcero.engine.models.DungeonConfiguration

class DeadEndsProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        // 'Dead end cells' are those that have only 1 side that is passable, meaning they are surrounded on
        // three sides by 'walls'.
        val deadEndCells = dungeon.allCells.filter { it.numPassableSides == 1 }

        for (cell in deadEndCells) {
            // Randomly determine whether we care about this particular dead end.
            if (randomizer.getRandomDouble() > configuration.chanceToRemoveDeadEnds) {
                continue
            }

            // Find the direction in which we need to travel to remove the dead end.
            val adjacentDirection = cell.firstPassableDirection ?: continue

            // Walk to the 'previous' cell, which is the cell in the adjacent direction.
            var previousCell = dungeon.getAdjacentCell(cell, adjacentDirection) ?: continue

            var currentCell = cell
            var connected = false

            while (!connected) {
                getRandomValidDirection(dungeon, currentCell, previousCell, randomizer)?.let {
                    dungeon.getAdjacentCell(currentCell, it)?.let { adjacentCell ->
                        connected = adjacentCell.isOpen
                        adjacentCell.isOpen = true
                        currentCell.passableSides[it] = true
                        adjacentCell.passableSides[it.opposite()] = true

                        previousCell = currentCell
                        currentCell = adjacentCell
                    }
                }
            }
        }
    }

    private companion object {
        fun getRandomValidDirection(dungeon: Dungeon, currentCell: DungeonCell, previousCell: DungeonCell, randomizer: Randomizer): Direction? {
            val invalidDirections = mutableListOf<Direction>()
            val squareDirections = mutableListOf<Direction>()

            while (invalidDirections.size + squareDirections.size < Direction.values().size) {
                val direction = randomizer.getRandomDirection(invalidDirections.union(squareDirections))
                val nextCell = dungeon.getAdjacentCell(currentCell, direction)

                if (isDirectionValid(dungeon, currentCell, direction, previousCell) && nextCell != null) {
                    val clockwiseSides = nextCell.passableSides[direction.rotate(clockwise = true)] == true
                            && currentCell.passableSides[direction.rotate(clockwise = true)] == true

                    val antiClockwiseSides = nextCell.passableSides[direction.rotate(clockwise = false)] == true
                            && currentCell.passableSides[direction.rotate(clockwise = false)] == true

                    if (nextCell.isOpen && (clockwiseSides || antiClockwiseSides)) {
                        squareDirections.add(direction)
                    } else {
                        return direction
                    }
                } else {
                    invalidDirections.add(direction)
                }
            }

            return if (squareDirections.isNotEmpty()) randomizer.getRandomItem(squareDirections) else null
        }

        fun isDirectionValid(dungeon: Dungeon, cell: DungeonCell, direction: Direction, previousCell: DungeonCell): Boolean {
            dungeon.getAdjacentCell(cell, direction)?.let { adjacentCell ->
                return adjacentCell !== previousCell
            }

            return false
        }
    }
}
