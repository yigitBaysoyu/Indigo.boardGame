package entity

/**
 * Represents the Tile in the center of the board.
 *
 * @property availableGems holds the up to 6 Gems that are on the Tile at the start of the game.
 */
class CenterTile (
    connections: Map<Int, Int>,
    rotationOffset: Int,
    xCoordinate: Int,
    yCoordinate: Int,
    val availableGems: ArrayDeque<GemType>
): Tile(
    connections,
    rotationOffset,
    xCoordinate,
    yCoordinate
)