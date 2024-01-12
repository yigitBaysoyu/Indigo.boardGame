package service.message

import entity.*
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class GameInitMessage(
    val players: List<Player> , 
    val gameMode: GameMode, 
    val tileList: List<TileType>
): GameAction() {
    init {
        require(players.size in 2..4)
        require(tileList.size == 54)
        require(tileList.filter { it == TileType.TYPE_0 }.size == 14)
        require(tileList.filter { it == TileType.TYPE_1 }.size == 6)
        require(tileList.filter { it == TileType.TYPE_2 }.size == 14)
        require(tileList.filter { it == TileType.TYPE_3 }.size == 14)
        require(tileList.filter { it == TileType.TYPE_4 }.size == 6)
    }
}
