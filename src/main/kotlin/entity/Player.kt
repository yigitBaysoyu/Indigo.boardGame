package entity

import kotlinx.serialization.Serializable

/**
 * Represents a player participating in a game of Indigo.
 *
 * @property playHand holds the tiles which the player currently has on his hand.
 * @property gateList stores the gates that belong to the player. If a gem enters one of these the player gets points.
 */
@Serializable
data class Player(
    val name: String = "",
    var color: Int = 0,
    val playerType: PlayerType = PlayerType.LOCALPLAYER,
    var score: Int = 0,
    var amountOfGems: Int = 0,
    val playHand: MutableList<PathTile> = mutableListOf(),
    val gateList: MutableList<GateTile> = mutableListOf()
){

    /**
     *  creates a deep copy of the player object for the AIService to simulate possible game states.
     */
    fun deepCopy(): Player {
        return Player(
            name = this.name,
            color = this.color,
            playerType = this.playerType,
            score = this.score,
            amountOfGems = this.amountOfGems,
            playHand = this.playHand.map { it.copy() }.toMutableList(),
            gateList = this.gateList.map { it.copy() }.toMutableList()
        )
    }
}