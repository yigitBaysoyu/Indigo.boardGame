package entity

/**
 * Main Entity. Holds all the data used during a Game.
 *
 * @property activePlayerID index in the playerList of the player whose turn it currently is.
 * @property playerList holds the players in the order that they take turns in.
 * @property gameLayout 2d List which holds all the tiles on the board.
 */
data class IndigoGame (
    var activePlayerID: Int,
    var simulationSpeed: Double,
    val isNetworkGame: Boolean,
    val undoStack: ArrayDeque<Turn>,
    val redoStack: ArrayDeque<Turn>,
    val playerList: MutableList<Player>,
    val gateList: MutableList<GateTile>,
    val drawPile: MutableList<PathTile>,
    val gameLayout: MutableList<MutableList<Tile>>
) {
    /**
     * returns the player object whose turn it currently is.
     */
    fun getActivePlayer(): Player {
        return playerList[activePlayerID]
    }
}