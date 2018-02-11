package karcero.engine.models

class DungeonRoom(val roomId: Int, val size: Size) {
    var row: Int = 0
    var column: Int = 0

    val bottom: Int
        get() = row + size.height

    val right: Int
        get() = column + size.width
}
