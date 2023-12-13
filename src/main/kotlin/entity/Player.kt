package entity

import java.awt.Color

data class Player(
    val name: String,
    var color: Int,
    var playerType: PlayerType,

) {
    var score = 0
    var playHand: Int? = null
}
