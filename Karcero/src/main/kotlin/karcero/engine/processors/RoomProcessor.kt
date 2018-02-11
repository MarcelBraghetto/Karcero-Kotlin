package karcero.engine.processors

import karcero.engine.contracts.DungeonProcessor
import karcero.engine.helpers.Randomizer
import karcero.engine.models.*

class RoomProcessor : DungeonProcessor {
    override fun process(dungeon: Dungeon, configuration: DungeonConfiguration, randomizer: Randomizer) {
        val validSizes = getAllPossibleRoomSizes(configuration)

        for (i in 0 until configuration.roomCount) {
            val room = createRoom(i, randomizer, validSizes) ?: break

            var roomPlaced = false
            val visitedCells = mutableSetOf<DungeonCell>()
            val unvisitedCells = mutableSetOf<DungeonCell>().apply {
                dungeon.allCells.forEach { add(it) }
            }

            while (visitedCells.size < dungeon.height * dungeon.width) {
                val cell = randomizer.getRandomItem(unvisitedCells)
                visitedCells.add(cell)
                unvisitedCells.remove(cell)

                room.row = cell.row
                room.column = cell.column

                // Make sure the room is placed completely inside the dungeon.
                if (room.column <= 0
                        || room.right >= dungeon.width
                        || room.row <= 0
                        || room.bottom >= dungeon.height) {

                    continue
                }

                val cells = dungeon.getRoomCells(room)

                // Don't place room where it is overlapping another room
                if (cells.any { it.isInRoom }) {
                    continue
                }

                // Don't place room where it is adjacent to another room
                if (getCellsAdjacentToRoom(dungeon, room).any { it.isInRoom }) {
                    continue
                }

                // Corners are rock
                if (!areAllCornerCellsRocks(dungeon, room)) {
                    continue
                }

                // All corridors leading into room can become doors (are isolated)
                if (!canAllCorridorsLeadingToRoomBeDoors(dungeon, room)) {
                    continue
                }

                placeRoom(dungeon, room)
                roomPlaced = true
                break
            }

            if (!roomPlaced) {
                validSizes.remove(room.size)
            }
        }

        resolveIsolatedRooms(dungeon)
    }

    private companion object {
        fun resolveIsolatedRooms(dungeon: Dungeon) {
            // An isolated room is 'orphaned' - all of their adjacent cells are rock, therefore they
            // cannot be reached by any part of the map. We can attempt to 'connect' these rooms to
            // something else in the map that isn't rock.
            val isolatedRooms = dungeon.rooms.filter { room ->
                getCellsAdjacentToRoom(dungeon, room).all { it.terrainType == TerrainType.ROCK }
            }.toMutableList()

            // Walk through each isolated room and attempt to connect it, upon a successful connection
            // the room will no longer be isolated.
            val iterator = isolatedRooms.listIterator()
            while (iterator.hasNext()) {
                val room = iterator.next()
                if (connectRoom(dungeon, room)) {
                    iterator.remove()
                }
            }

            // If we still have isolated rooms left over, we should remove them from the map entirely.
            isolatedRooms.forEach { isolatedRoom ->
                dungeon.getRoomCells(isolatedRoom).forEach { it.reset() }
                dungeon.rooms.remove(isolatedRoom)
            }
        }

        fun connectRoom(dungeon: Dungeon, room: DungeonRoom): Boolean {
            // Find all the cells around the edges of the room and shuffle them.
            val adjacentCells = getCellsAdjacentToRoom(dungeon, room).shuffled()

            // For each adjacent cell determine which direction it is in then attempt to 'tunnel'
            // in that direction until we hit either a non-rock other cell not belonging in another room,
            // or the edge of the map. The edge of the map would be an invalid direction.
            adjacentCells.forEach { cell ->
                val tunnel = mutableListOf<DungeonCell>()
                val direction = determineAdjacentCellDirection(cell, room)
                var currentCell: DungeonCell? = cell

                while (currentCell != null) {
                    tunnel.add(currentCell)

                    // We found something that isn't rock, and isn't in a room so we can connect to it.
                    if (currentCell.terrainType != TerrainType.ROCK && !currentCell.isInRoom) {
                        createTunnel(tunnel, direction)
                        return true
                    }

                    // Step along the current direction to the next cell.
                    currentCell = dungeon.getAdjacentCell(currentCell, direction)
                }
            }

            // Nope, after trying all the adjacent cells, we failed to find one that can tunnel
            // to another valid cell in the dungeon.
            return false
        }

        fun createTunnel(cells: Iterable<DungeonCell>, direction: Direction) {
            cells.forEach { cell ->
                cell.isOpen = true
                cell.passableSides[direction] = false
                cell.passableSides[direction.opposite()] = false
                cell.terrainType = TerrainType.FLOOR
            }
        }

        fun determineAdjacentCellDirection(cell: DungeonCell, room: DungeonRoom): Direction {
            return when {
                cell.row < room.row -> Direction.NORTH
                cell.row > room.bottom -> Direction.SOUTH
                cell.column < room.column -> Direction.WEST
                else -> Direction.EAST
            }
        }

        fun getAllPossibleRoomSizes(configuration: DungeonConfiguration): MutableSet<Size> {
            return mutableSetOf<Size>().apply {
                for (i in configuration.minRoomHeight..configuration.maxRoomHeight) {
                    for (j in configuration.minRoomWidth..configuration.maxRoomWidth) {
                        add(Size(j, i))
                    }
                }
            }
        }

        fun createRoom(roomId: Int, randomizer: Randomizer, validSizes: Set<Size>): DungeonRoom? {
            if (validSizes.isEmpty()) {
                return null
            }

            return DungeonRoom(roomId, randomizer.getRandomItem(validSizes))
        }

        fun placeRoom(dungeon: Dungeon, room: DungeonRoom) {
            dungeon.rooms.add(room)
            dungeon.getRoomCells(room).forEach {
                it.terrainType = TerrainType.FLOOR
                it.isInRoom = true
                it.isOpen = true
            }
        }

        fun areAllCornerCellsRocks(dungeon: Dungeon, room: DungeonRoom): Boolean {
            room.run {
                if (isCellNotRock(dungeon, row - 1, column - 1)) return false
                if (isCellNotRock(dungeon, row - 1, right)) return false
                if (isCellNotRock(dungeon, bottom, column - 1)) return false
                if (isCellNotRock(dungeon, bottom, right)) return false
            }

            return true
        }

        fun isCellNotRock(dungeon: Dungeon, row: Int, column: Int): Boolean {
            val cell = dungeon.tryGetCell(row, column) ?: return true
            return cell.terrainType != TerrainType.ROCK
        }

        fun isCellIsolatedOnSides(dungeon: Dungeon, cell: DungeonCell, directions: Array<Direction>): Boolean {
            return directions.all { direction ->
                val adjacentCell = dungeon.getAdjacentCell(cell, direction)
                return adjacentCell == null || adjacentCell.terrainType == TerrainType.ROCK
            }
        }

        fun shouldRejectRoomCell(dungeon: Dungeon, row: Int, column: Int, direction: Direction, isolatedDirections: Array<Direction>): Boolean {
            dungeon.getAdjacentCell(dungeon.getCell(row, column), direction)?.let {
                if (it.terrainType == TerrainType.FLOOR && !isCellIsolatedOnSides(dungeon, it, isolatedDirections)) {
                    return true
                }
            }

            return false
        }

        fun canAllCorridorsLeadingToRoomBeDoors(dungeon: Dungeon, room: DungeonRoom): Boolean {
            for (column in room.column until room.right) {
                if (shouldRejectRoomCell(dungeon, room.row, column, Direction.NORTH, arrayOf(Direction.EAST, Direction.WEST))) {
                    return false
                }

                if (shouldRejectRoomCell(dungeon, room.bottom, column, Direction.SOUTH, arrayOf(Direction.EAST, Direction.WEST))) {
                    return false
                }
            }

            for (row in room.row until room.bottom) {
                if (shouldRejectRoomCell(dungeon, room.row, room.right, Direction.EAST, arrayOf(Direction.NORTH, Direction.SOUTH))) {
                    return false
                }

                if (shouldRejectRoomCell(dungeon, room.row, room.column, Direction.WEST, arrayOf(Direction.NORTH, Direction.SOUTH))) {
                    return false
                }
            }

            return true
        }

        fun getCellsAdjacentToRoom(dungeon: Dungeon, room: DungeonRoom, distance: Int = 1): Iterable<DungeonCell> {
            val cells = mutableListOf<DungeonCell>()

            for (column in room.column until Math.min(room.right, dungeon.width)) {
                if (room.row >= distance) {
                    dungeon.tryGetCell(room.row, column)?.let { cell ->
                        dungeon.getAdjacentCell(cell, Direction.NORTH, distance)?.let { cells.add(it) }
                    }
                }

                if (room.bottom <= dungeon.height - distance) {
                    dungeon.tryGetCell(room.bottom - 1, column)?.let { cell ->
                        dungeon.getAdjacentCell(cell, Direction.SOUTH, distance)?.let { cells.add(it) }
                    }
                }
            }

            for (row in room.row until Math.min(room.bottom, dungeon.height)) {
                if (room.column >= distance) {
                    dungeon.tryGetCell(row, room.column)?.let { cell ->
                        dungeon.getAdjacentCell(cell, Direction.WEST, distance)?.let { cells.add(it) }
                    }
                }

                if (room.right <= dungeon.width - distance) {
                    dungeon.tryGetCell(row, room.right - distance)?.let { cell ->
                        dungeon.getAdjacentCell(cell, Direction.EAST, distance)?.let { cells.add(it) }
                    }

                    dungeon.getAdjacentCell(dungeon.getCell(row, room.right - 1), Direction.EAST, distance)
                            ?.let { cells.add(it) }
                }
            }

            return cells.toList()
        }
    }
}
