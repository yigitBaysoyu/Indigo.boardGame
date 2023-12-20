package entity

import kotlinx.serialization.Serializable

/**
 * At the start of the game there are six treasure tiles on the outer edges of the board. They each hold a gem.
 */
@Serializable
class TreasureTile (
    override val connections: Map<Int, Int>,
    override  val rotationOffset: Int,
    override val xCoordinate: Int,
    override  val yCoordinate: Int,
    val gemPositions: MutableList<GemType>
): Tile()
