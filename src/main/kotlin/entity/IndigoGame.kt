package entity

import PairDequeSerializer
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
    var simulationSpeed: Double = 50.0,
    val isNetworkGame: Boolean = false,
    @Serializable(with = ArrayDequeSerializer::class)
    val undoStack: ArrayDeque<Turn> = ArrayDeque(),
    @Serializable(with = PairDequeSerializer::class)
    val redoStack: ArrayDeque<Pair<Pair<Int,Int>,Int>> = ArrayDeque(),
    val playerList: MutableList<Player> = mutableListOf(),
    val gateList: MutableList<MutableList<GateTile>> = MutableList(6){ mutableListOf()},
    var drawPile: MutableList<PathTile> = mutableListOf(),
    val gameLayout: MutableList<MutableList<Tile>> = mutableListOf()
) {
    /**
     * returns the player object whose turn it currently is.
     */
    fun getActivePlayer(): Player {
        return playerList[activePlayerID]
    }

    /**
     *  creates a deep copy of the game state for the AIService to simulate possible game states.
     */
    fun deepCopy(): IndigoGame {
        return IndigoGame(
            activePlayerID = this.activePlayerID,
            simulationSpeed = this.simulationSpeed,
            isNetworkGame = this.isNetworkGame,
            undoStack = ArrayDeque(),
            redoStack = ArrayDeque(),
            playerList = this.playerList.map { it.deepCopy() }.toMutableList(),
            gateList = this.gateList.map { innerList -> innerList.map { it.copy() }.toMutableList() }.toMutableList(),
            drawPile = this.drawPile.map { it.copy() }.toMutableList(),
            gameLayout = this.gameLayout.map { innerList -> innerList.map { it.copy() }.toMutableList() }.toMutableList()
        )
    }

}
