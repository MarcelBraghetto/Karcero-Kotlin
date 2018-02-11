package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.DungeonCell
import karcero.engine.models.Direction
import karcero.engine.models.Dungeon
import karcero.engine.models.DungeonConfiguration

class MazeProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        val visitedCells = mutableSetOf<DungeonCell>()
        val visitedValidCells = mutableSetOf<DungeonCell>()
        var previousDirection: Direction? = null

        var currentCell = randomizer.getRandomCell(dungeon)
        currentCell.isOpen = true

        while (visitedCells.size < dungeon.width * dungeon.height) {
            val oldCell = currentCell
            var changed = false
            visitedCells.add(currentCell)
            visitedValidCells.add(currentCell)

            val direction = getRandomValidDirection(dungeon, currentCell, visitedCells,
                    configuration.randomness, previousDirection, randomizer)

            if (direction == null) {
                visitedValidCells.remove(currentCell)

                if (visitedValidCells.isEmpty()) {
                    return
                }

                currentCell = randomizer.getRandomItem(visitedValidCells)
            } else {
                changed = currentCell.passableSides[direction] == false
                currentCell = dungeon.getAdjacentCell(currentCell, direction)!!
                currentCell.passableSides[direction.opposite()] = true
                oldCell.passableSides[direction] = true
                previousDirection = direction
            }

            if (currentCell.isOpen && !changed) {
                continue
            }

            currentCell.isOpen = true
        }
    }

    private companion object {
        fun getRandomValidDirection(dungeon: Dungeon, cell: DungeonCell, visitedCells: Set<DungeonCell>,
                                    randomness: Double, previousDirection: Direction?,
                                    randomizer: Randomizer): Direction? {

            previousDirection?.let {
                if (randomness < 1.0
                        && randomizer.getRandomDouble() > randomness
                        && isDirectionValid(dungeon, cell, it, visitedCells)) {

                    return it
                }
            }

            val invalidDirections = mutableListOf<Direction>()
            while (invalidDirections.size < Direction.values().size) {
                val direction = randomizer.getRandomDirection(invalidDirections)

                if (isDirectionValid(dungeon, cell, direction, visitedCells)) {
                    return direction
                }

                invalidDirections.add(direction)
            }

            return null
        }

        fun isDirectionValid(dungeon: Dungeon, cell: DungeonCell, direction: Direction,
                             visitedCells: Set<DungeonCell>): Boolean {

            dungeon.getAdjacentCell(cell, direction)?.let { adjacentCell ->
                return !visitedCells.contains(adjacentCell)
            }

            return false
        }
    }
}
