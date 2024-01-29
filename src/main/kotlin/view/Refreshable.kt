package view

import entity.GemMovement
import entity.Turn
import service.ConnectionState

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
     * perform refreshes that are necessary after a new game started.
     */
    fun refreshAfterStartNewGame() {}

    /**
     * perform refreshes that are necessary after a tile rotated.
     */
    fun refreshAfterTileRotated() {}

    /**
     * perform refreshes that are necessary after a tile placed.
     *
     * @param turn The turn that just took place.
     */
    fun refreshAfterTilePlaced(turn: Turn) {}

    /**
     * Performs the necessary refreshes after a gem move.
     *
     * @param movement Contains all the data related to a gem's movement.
     */
    fun refreshAfterGemMoved(movement: GemMovement) {}

    /**
     * perform refreshes that are necessary after a game ended.
     */
    fun refreshAfterEndGame() {}

    /**
     * perform refreshes that are necessary after undo has been called.
     *
     * @param turn Contains all the data related to undo.
     */
    fun refreshAfterUndo(turn: Turn) {}

    /**
     * perform refreshes that are necessary after redo has been called.
     *
     * @param turn Contains all the data related to redo.
     */
    fun refreshAfterRedo(turn: Turn) {}

    /**
     * perform refreshes that are necessary after an old game loaded.
     */
    fun refreshAfterLoadGame() {}

    /**
     * perform refreshes after load game was called and the save file was not found.
     */
    fun refreshAfterFileNotFound() {}

    /**
     * perform refreshes that are necessary after a new game joined.
     */
    fun refreshAfterGameJoined() {}

    /**
     * Performs necessary refreshes after a player joins a game.
     * * @param [name] from the player who has joined
    */
    fun refreshAfterPlayerJoined(name: String) {}

    /**
     * perform refreshes that are necessary after a player lefts a game.
     */
    fun refreshAfterPlayerLeft(name: String) {}

    /**
     * perform refreshes that are necessary after the simulation speed changed.
     */
    fun refreshAfterSimulationSpeedChange(speed: Double) {}

    /**
     * perform refreshes that are necessary after "updateConnectionState",
     * [service.NetworkService]
     * @param newState the new state of the connection
     */
    fun refreshConnectionState(newState: ConnectionState){}

    /**
     * perform refreshes are necessary after the last player joined a hosted game
     */
    fun refreshAfterLastPlayerJoined() {}

    /**
     * performs refreshes after a sessionID is received in a createGame message
     */
    fun refreshAfterSessionIDReceived(sessionID: String) {}

    /**
     * performs refreshes after a game could not be created because sessionID already exists
     */
    fun refreshAfterSessionIDAlreadyExists() {}

    /**
     * performs refreshes after a game could not be joined because sessionID is not valid
     */
    fun refreshAfterSessionIDIsInvalid() {}
}