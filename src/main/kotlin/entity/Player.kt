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
    val color: Int = 0,
    val playerType: PlayerType = PlayerType.LOCALPLAYER,
    var score: Int = 0,
    var amountOfGems: Int = 0,
    val playHand: MutableList<PathTile> = mutableListOf(),
    val gateList: MutableList<GateTile> = mutableListOf()
)