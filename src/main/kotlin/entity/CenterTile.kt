package entity

import kotlinx.serialization.Serializable


/**
 * Represents the Tile in the center of the board.
 *
 * @property availableGems holds the up to 6 Gems that are on the Tile at the start of the game.
 */
@Serializable
class CenterTile(
    override var connections: Map<Int, Int>,
    override var rotationOffset: Int,
    override val xCoordinate: Int,
    override val yCoordinate: Int,
    @Serializable(with = ArrayDequeGemTypeSerializer::class)
    val availableGems: ArrayDeque<GemType>
): Tile()