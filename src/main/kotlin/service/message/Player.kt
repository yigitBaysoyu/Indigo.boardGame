package service.message
import entity.*

/**
 * implements a Player Object which represents a client inside the game.
 * @constructor returns a Player Object with a chosen name and [PlayerColor]
 * @param name The displayed unique name of the Player
 * @param color the chosen Color which is relevant to the assigned Gates.
 */
data class Player (val name : String,
              val color : PlayerColor)
