package entity

/**
 * Main Entity. Holds all the data used during a Game.
 *
 * @property activePlayerID index in the playerList of the player whose turn it currently is.
 * @property playerList holds the players in the order that they take turns in.
 * @property gameLayout 2d List which holds all the tiles on the board.
 */
data class IndigoGame (
    var activePlayerID: Int = 0,
    var simulationSpeed: Double = 1.0,
    val isNetworkGame: Boolean = false,
    val undoStack: ArrayDeque<Turn> = ArrayDeque(),
    val redoStack: ArrayDeque<Turn> = ArrayDeque(),
    val playerList: MutableList<Player> = mutableListOf(),
    val gateList: MutableList<GateTile> = mutableListOf(),
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