package service

import entity.PathTile
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class SwitchPlayerTest {
    private val rootService = RootService()

    /**
     * Function to test the switchPlayer method by
     * calling a sequence where a normal player, a random
     * AI and a smart AI make a turn
     */
    @Test
    fun testSwitchPlayer(){
        val gameService = rootService.gameService
        val playerService = rootService.playerService

        val players = mutableListOf(Player("player"),
                                    Player("random", playerType = PlayerType.RANDOMAI),
                                    Player("smart", playerType = PlayerType.SMARTAI)
        )

        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)

        //SmartAi has currently no functionality so isnt tested
        //Make turn for normalPlayer
        playerService.placeTile(-1, 0)

        assert(game.activePlayerID == 0)
        assert(game.undoStack.size == 2)

        var placedTileIndex = 0
        for(row in game.gameLayout){
            for(tile in row){
                if(tile is PathTile){
                    placedTileIndex++
                }
            }
        }

        assert(placedTileIndex == 2)
    }
}