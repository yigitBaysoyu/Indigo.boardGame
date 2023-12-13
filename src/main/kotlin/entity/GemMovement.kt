package entity

/**
 * Holds the Data necessary to trace the movement of one Gem.
 * Is used to undo and redo the gem movements.
 */
data class GemMovement (
    val gemType: GemType,
    val startTile: Tile,
    val positionOnStartTile: Int,
    val endTile: Tile,
    val positionOnEndTile: Int,
    val didCollide: Boolean
)