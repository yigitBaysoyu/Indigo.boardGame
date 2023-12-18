package entity

/**
 * Represents a player participating in a game of Indigo.
 *
 * @property playHand holds the tiles which the player currently has on his hand.
 * @property gateList stores the gates that belong to the player. If a gem enters one of these the player gets points.
 */
data class Player(
    val name: String = "",
    val color: Int = 0,
    val playerType: PlayerType = PlayerType.LOCALPLAYER,
    var score: Int = 0,
    val playHand: PathTile,
    val gateList: MutableList<GateTile> = mutableListOf()
)
