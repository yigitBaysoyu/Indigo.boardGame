package entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

/**
 * Gate Tiles are placed on an extra outer layer around the board.
 * When a Gem enters a gate Tile its score is awarded two the Player who owns the Gate.
 *
 * @property gemsCollected holds the gems that have been sent to this Gate.
 */
@Serializable
class GateTile(
     override var connections: Map<Int, Int>,
   override var rotationOffset: Int,
    override val xCoordinate: Int,
    override val yCoordinate: Int,
    val gemsCollected: MutableList<GemType>
): Tile(
)