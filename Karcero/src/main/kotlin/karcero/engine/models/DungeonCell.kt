package karcero.engine.models

class DungeonCell(val row: Int, val column: Int) {
    val attributes = mutableMapOf<String, Any>()
    var terrainType = TerrainType.ROCK
    var isOpen: Boolean = false
    var isInRoom: Boolean = false
    val passableSides = mutableMapOf(
            Direction.NORTH to false,
            Direction.EAST to false,
            Direction.SOUTH to false,
            Direction.WEST to false
    )

    val walls = mutableMapOf(
            Direction.NORTH to WallType.NONE,
            Direction.EAST to WallType.NONE,
            Direction.SOUTH to WallType.NONE,
            Direction.WEST to WallType.NONE
    )

    val numPassableSides: Int
        get() {
            return passableSides.count { it.value }
        }

    val firstPassableDirection: Direction?
        get() {
            return passableSides.entries.firstOrNull { it.value }?.key
        }

    fun isPassable(direction: Direction): Boolean {
        return passableSides[direction] == true
    }

    fun reset() {
        terrainType = TerrainType.ROCK
        isInRoom = false

        passableSides[Direction.NORTH] = false
        passableSides[Direction.EAST] = false
        passableSides[Direction.SOUTH] = false
        passableSides[Direction.WEST] = false

        walls[Direction.NORTH] = WallType.NONE
        walls[Direction.EAST] = WallType.NONE
        walls[Direction.SOUTH] = WallType.NONE
        walls[Direction.WEST] = WallType.NONE
    }
}
