package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.DungeonCell
import karcero.engine.models.Dungeon
import karcero.engine.models.DungeonConfiguration

class SparsenessProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        // How many cells to attempt to remove from the dungeon to make it more 'sparse'
        var cellsToRemove = (dungeon.width * dungeon.height * configuration.sparseness).toInt()

        while (cellsToRemove != 0) {
            val changedCells = mutableSetOf<DungeonCell>()
            val deadEndCells = dungeon.allCells.filter { it.numPassableSides == 1 }

            if (deadEndCells.isEmpty()) {
                break
            }

            for (deadEndCell in deadEndCells) {
                deadEndCell.isOpen = false

                val openDirection = deadEndCell.firstPassableDirection ?: continue

                deadEndCell.passableSides[openDirection] = false
                changedCells.add(deadEndCell)

                dungeon.getAdjacentCell(deadEndCell, openDirection)?.let { oppositeCell ->
                    oppositeCell.passableSides[openDirection.opposite()] = false
                    changedCells.add(oppositeCell)
                }

                cellsToRemove--

                if (cellsToRemove == 0) {
                    break
                }
            }
        }
    }
}
