package entity

import kotlinx.serialization.Serializable

/**
 * At the start of the game there are six treasure tiles on the outer edges of the board. They each hold a gem.
 */
@Serializable
class TreasureTile (
    override var connections: Map<Int, Int> = mutableMapOf(),
    override  var rotationOffset: Int = 0,
    override val xCoordinate: Int = 0,
    override  val yCoordinate: Int = 0,
    val gemPositions: MutableList<GemType> = mutableListOf()
): Tile()