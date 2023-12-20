package entity

import kotlinx.serialization.Serializable

/**
 * Represents a tile with paths on it. This is the type of tile that players can place.
 *
 * @property gemPositions List with 6 entries. Each represents which type of gem (can also be NONE) is
 * on which side of the tile.
 */
@Serializable
class PathTile(
    override var connections: Map<Int, Int>,
  override  var rotationOffset: Int,
    override val xCoordinate: Int,
     override  val yCoordinate: Int,
   val gemPositions: MutableList<GemType>
) :Tile()