package view

import entity.GemMovement
import entity.PathTile
import entity.Turn

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 */
interface Refreshable {
    /**
     * perform refreshes that are necessary after a new game started
     */
    fun refreshAfterStartNewGame() {}

    fun refreshAfterTileRotated() {}

    fun refreshAfterTilePlaced(tile: PathTile) {}

    fun refreshAfterGemMoved(movement: GemMovement) {}

    fun refreshAfterEndGame() {}

    fun refreshAfterUndo(turn: Turn) {}

    fun refreshAfterRedo(turn: Turn) {}

    fun refreshAfterLoadGame() {}

    fun refreshAfterGameJoined() {}

    fun refreshAfterPlayerJoined() {}

    fun refreshAfterPlayerLeft() {}

    fun refreshAfterSimulationSpeedChange() {}
}