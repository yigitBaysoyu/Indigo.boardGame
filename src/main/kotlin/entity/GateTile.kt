package entity

/**
 * Gate Tiles are placed on an extra outer layer around the board.
 * When a Gem enters a gate Tile its score is awarded two the Player who owns the Gate.
 *
 * @property gemsCollected holds the gems that have been sent to this Gate.
 */

class GateTile(
    connections: Map<Int, Int>,
    rotationOffset: Int,
    xCoordinate: Int,
    yCoordinate: Int,
    val gemsCollected: MutableList<GemType>
): Tile(
    connections,
    rotationOffset,
    xCoordinate,
    yCoordinate
)