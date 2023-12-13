package entity

/**
 * At the start of the game there are six treasure tiles on the outer edges of the board. They each hold a gem.
 */
class TreasureTile (
    connections: Map<Int, Int>,
    rotationOffset: Int,
    xCoordinate: Int,
    yCoordinate: Int,
    val gemPositions: MutableList<GemType>
): Tile(
    connections,
    rotationOffset,
    xCoordinate,
    yCoordinate
)
