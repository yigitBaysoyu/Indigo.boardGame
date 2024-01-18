package entity

import kotlinx.serialization.Serializable

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
){
    /**
     *  This function assists the deepCopy function in IndigoGame to create a deep copy of the game state.
     */
    override fun copy(): GateTile {
        return GateTile(
            connections = this.connections.toMap(),
            rotationOffset = this.rotationOffset,
            xCoordinate = this.xCoordinate,
            yCoordinate = this.yCoordinate,
            gemsCollected = this.gemsCollected.toMutableList()
        )
    }
}