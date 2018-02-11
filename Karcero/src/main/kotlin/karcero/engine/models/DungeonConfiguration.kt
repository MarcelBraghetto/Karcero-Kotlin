package karcero.engine.models

class DungeonConfiguration(
        // Dimensions of the dungeon.
        var height: Int = 1, var width: Int = 1,

        // How wriggly corridors will be during the maze generation.
        var randomness: Double = 1.0,

        // How densely populated the dungeon will be.
        var sparseness: Double = 0.0,

        // How often dead end corridors will be filled.
        var chanceToRemoveDeadEnds: Double = 0.0,

        // How often a door will be placed at the intersection between a room and a corridor.
        var chanceToPlaceRoomDoor: Double = 1.0,

        // Min / max room width when generating rooms.
        var minRoomWidth: Int = 0, var maxRoomWidth: Int = 0,

        // Min / max room height when generating rooms.
        var minRoomHeight: Int = 0, var maxRoomHeight: Int = 0,

        // How many rooms to attempt to populate into the dungeon.
        var roomCount: Int = 0
) {
    companion object {
        /**
         * Generate a configuration with sensible defaults for a nicely sized
         * and populated dungeon.
         */
        fun createDefault(): DungeonConfiguration {
            return DungeonConfiguration(
                    width = 25,
                    height = 25,
                    randomness = 0.5,
                    sparseness = 0.8,
                    chanceToRemoveDeadEnds = 0.9,
                    chanceToPlaceRoomDoor = 1.0,
                    minRoomWidth = 4,
                    maxRoomWidth = 14,
                    minRoomHeight = 4,
                    maxRoomHeight = 9,
                    roomCount = 14
            )
        }
    }
}
