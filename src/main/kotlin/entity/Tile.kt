package entity

/**
 * Abstract Class which represents a Tile on the Board.
 *
 * @property rotationOffset is always between zero and five. Represents the Tiles Rotation in increments of 60 Degrees.
 * @property connections holds the paths on a tile. Maps two ends of a Tile to one another.
 */
abstract class Tile (
    var connections: Map<Int, Int>,
    var rotationOffset: Int,
    val xCoordinate: Int,
    val yCoordinate: Int
)