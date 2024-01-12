package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class TilePlacedMessage(
    val rotation: Int, 
    val qCoordinate: Int, 
    val rCoordinate: Int
): GameAction() {
    init {
        require(rotation in 0..5)
        require(qCoordinate in -4..4)
        require(rCoordinate in -4..4)
    }
}
