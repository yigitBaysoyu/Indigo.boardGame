package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a tile with paths on it. This is the type of tile that players can place.
 *
 * @property gemPositions List with 6 entries. Each represents which type of gem (can also be NONE) is
 * on which side of the tile.
 */
@Serializable
class PathTile(
    override var connections: Map<Int, Int> = mutableMapOf(),
    override var rotationOffset: Int = 0,
    override val xCoordinate: Int = 0,
    override val yCoordinate: Int = 0,
    override val gemPositions: MutableList<GemType> = mutableListOf(),
    @SerialName("PathTileType")
    val type: Int = 0
): Tile(), TraverseAbleTile {
    override fun copy(): PathTile {
        return PathTile(
            connections = this.connections.toMap(),
            rotationOffset = this.rotationOffset,
            xCoordinate = this.xCoordinate,
            yCoordinate = this.yCoordinate,
            gemPositions = this.gemPositions.toMutableList(),
            type = this.type
        )
    }
}