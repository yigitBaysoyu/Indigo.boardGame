package entity

/**
 * Represents a space on the Board with no Tile on it.
 */
class EmptyTile (
    connections: Map<Int, Int>,
    rotationOffset: Int,
    xCoordinate: Int,
    yCoordinate: Int
): Tile(
    connections,
    rotationOffset,
    xCoordinate,
    yCoordinate
)