package entity

import kotlinx.serialization.Serializable

/**
 * Represents a space on the Board with no Tile on it.
 */
@Serializable
class EmptyTile (
   override val connections: Map<Int, Int>,
   override val  rotationOffset: Int,
   override val xCoordinate: Int,
   override val yCoordinate: Int
): Tile()