package entity

/**
 * Represents a tile with paths on it. This is the type of tile that players can place.
 *
 * @property gemPositions List with 6 entries. Each represents which type of gem (can also be NONE) is
 * on which side of the tile.
 */
class PathTile(
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