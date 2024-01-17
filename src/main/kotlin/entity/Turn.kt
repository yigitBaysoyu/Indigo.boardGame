package entity

import kotlinx.serialization.Serializable

/**
 * Stores everything that happens during a turn. Is used to undo and redo.
 */
@Serializable
data class Turn (
    val playerID: Int,
    val scoreChanges: MutableList<Int> = mutableListOf(),
    val placedTile: PathTile,
    val gemMovements: MutableList<GemMovement> = mutableListOf()
){
    fun deepCopy(): Turn {
        return Turn(
            playerID = this.playerID,
            scoreChanges = this.scoreChanges.toMutableList(),
            placedTile = this.placedTile.copy(),
            gemMovements = this.gemMovements.map { it.copy() }.toMutableList()
        )
    }
}