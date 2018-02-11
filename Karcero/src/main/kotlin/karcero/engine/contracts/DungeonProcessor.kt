package karcero.engine.contracts

import karcero.engine.helpers.Randomizer
import karcero.engine.models.Dungeon
import karcero.engine.models.DungeonConfiguration

interface DungeonProcessor {
    fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer)
}
