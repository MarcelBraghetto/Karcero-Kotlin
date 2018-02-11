package karcero.engine.helpers

import karcero.engine.models.Direction
import karcero.engine.models.Dungeon
import karcero.engine.models.DungeonCell
import java.util.*

class Randomizer {
    private val random = Random()

    fun getRandomDirection(excluded: Iterable<Direction>?): Direction {
        val availableDirections = Direction.values().filter { excluded?.contains(it) == false }
        return availableDirections[random.nextInt(availableDirections.size)]
    }

    fun getRandomCell(dungeon: Dungeon): DungeonCell {
        return dungeon.getCell(random.nextInt(dungeon.width), random.nextInt(dungeon.height))
    }

    fun <T> getRandomItem(items: Iterable<T>): T {
        return items.elementAt(random.nextInt(items.count()))
    }

    fun getRandomDouble(): Double {
        return random.nextDouble()
    }
}