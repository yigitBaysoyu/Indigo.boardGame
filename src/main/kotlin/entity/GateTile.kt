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
): Tile() {
    override fun equals(other: Any?): Boolean {
        if(other !is GateTile) return false
        if(other.xCoordinate != xCoordinate) return false
        if(other.yCoordinate != yCoordinate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = connections.hashCode()
        result = 31 * result + rotationOffset
        result = 31 * result + xCoordinate
        result = 31 * result + yCoordinate
        result = 31 * result + gemsCollected.hashCode()
        return result
    }
}