package entity

import kotlinx.serialization.Serializable

/**
 * Abstract Class which represents a Tile on the Board.
 *
 * @property rotationOffset is always between zero and five. Represents the Tiles Rotation in increments of 60 Degrees.
 * @property connections holds the paths on a tile. Maps two ends of a Tile to one another.
 */
@Serializable
sealed class Tile {
    abstract var connections: Map<Int, Int>
    abstract var rotationOffset: Int
    abstract val xCoordinate: Int
    abstract val yCoordinate: Int

    /**
     *  This function assists the deepCopy function in IndigoGame to create a deep copy of the game state.
     */
    open fun copy(): Tile {
        return when(this) {
            is GateTile -> this.copy()
            is CenterTile -> this.copy()
            is EmptyTile -> this.copy()
            is TreasureTile -> this.copy()
            is PathTile -> this.copy()
            is InvisibleTile -> this.copy()
        }
    }
}