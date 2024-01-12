package entity

import kotlinx.serialization.Serializable

/**
 * Holds the Data necessary to trace the movement of one Gem.
 * Is used to undo and redo the gem movements.
 */
@Serializable
data class GemMovement (
    val gemType: GemType,
    val startTile: Tile,
    val positionOnStartTile: Int,
    val endTile: Tile,
    val positionOnEndTile: Int,
    val didCollide: Boolean
)