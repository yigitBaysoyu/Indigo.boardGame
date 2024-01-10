package entity

/**
 * At the start of the game there are six treasure tiles on the outer edges of the board. They each hold a gem.
 */
class TreasureTile (
    connections: Map<Int, Int> = mutableMapOf(),
    rotationOffset: Int = 0,
    xCoordinate: Int = 0,
    yCoordinate: Int = 0,
    val gemPositions: MutableList<GemType> = mutableListOf()
): Tile(
    connections,
    rotationOffset,
    xCoordinate,
    yCoordinate
)