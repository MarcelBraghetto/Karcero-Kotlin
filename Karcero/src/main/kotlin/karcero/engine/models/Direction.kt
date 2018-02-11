package karcero.engine.models

enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    fun opposite(): Direction {
        return when (this) {
            Direction.EAST -> Direction.WEST
            Direction.NORTH -> Direction.SOUTH
            Direction.SOUTH -> Direction.NORTH
            Direction.WEST -> Direction.EAST
        }
    }

    fun rotate(clockwise: Boolean): Direction {
        return when (this) {
            Direction.EAST -> if (clockwise) Direction.SOUTH else Direction.NORTH
            Direction.NORTH -> if (clockwise) Direction.EAST else Direction.WEST
            Direction.SOUTH -> if (clockwise) Direction.WEST else Direction.EAST
            Direction.WEST -> if (clockwise) Direction.NORTH else Direction.SOUTH
        }
    }
}