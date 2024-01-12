package entity

import kotlinx.serialization.Serializable

/**
 * Main Entity. Holds all the data used during a Game.
 *
 * @property activePlayerID index in the playerList of the player whose turn it currently is.
 * @property playerList holds the players in the order that they take turns in.
 * @property gameLayout 2d List which holds all the tiles on the board.
 */
@Serializable
data class IndigoGame(
    var activePlayerID: Int = 0,
    var simulationSpeed: Double = 1.0,
    val isNetworkGame: Boolean = false,
    @Serializable(with = ArrayDequeSerializer::class)
    val undoStack: ArrayDeque<Turn> = ArrayDeque(),
    @Serializable(with = ArrayDequeSerializer::class)
    val redoStack: ArrayDeque<Turn> = ArrayDeque(),
    // palyerList wieder zu val machen
    var playerList: MutableList<Player> = mutableListOf(),
    val gateList: MutableList<MutableList<GateTile>> = MutableList(6){ mutableListOf()},
    val drawPile: MutableList<PathTile> = mutableListOf(),
    val gameLayout: MutableList<MutableList<Tile>> = mutableListOf()
) {
    /**
     * returns the player object whose turn it currently is.
     */
    fun getActivePlayer(): Player {
        return playerList[activePlayerID]
    }
}