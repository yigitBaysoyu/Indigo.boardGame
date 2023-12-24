package entity

/**
 * Used as placeholder in the data structure for tile locations that don't exist in the game.
 */
class InvisibleTile(
    override var connections: Map<Int, Int> = mapOf(),
    override var rotationOffset: Int = 0,
    override val xCoordinate: Int = 0,
    override val yCoordinate: Int = 0,
): Tile()