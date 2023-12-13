package entity

/**
 * Stores everything that happens during a turn. Is used to undo and redo.
 */
data class Turn (
    val playerID: Int,
    val scoreChanges: MutableList<Int>,
    val placedTile: PathTile,
    val gemMovements: MutableList<GemMovement>
)