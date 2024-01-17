package entity

import kotlinx.serialization.Serializable

/**
 * Represents a space on the Board with no Tile on it.
 */
@Serializable
class EmptyTile (
   override var connections: Map<Int, Int>,
   override var  rotationOffset: Int,
   override val xCoordinate: Int,
   override val yCoordinate: Int
): Tile() {
   override fun copy(): EmptyTile {
      return EmptyTile(
         connections = this.connections.toMap(),
         rotationOffset = this.rotationOffset,
         xCoordinate = this.xCoordinate,
         yCoordinate = this.yCoordinate
      )
   }
}