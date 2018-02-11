package karcero.engine.models

enum class TerrainType {
    ROCK,
    FLOOR,
    DOOR_NORTH_SOUTH,
    DOOR_EAST_WEST;

    fun isDoor() = this == DOOR_NORTH_SOUTH || this == DOOR_EAST_WEST
}