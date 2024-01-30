package service

import entity.PathTile
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.Test

/**
 * Test class to ensure that the turns of the players switch correctly
 */
class SwitchPlayerTest {
    private val rootService = RootService()

    /**
     * Function to test the switchPlayer method by
     * calling a sequence where a normal player, a random
     * AI and a smart AI make a turn
     */
    @Test
    fun testSwitchPlayer() {
        val gameService = rootService.gameService
        val playerService = rootService.playerService

        val players = mutableListOf(
            Player("player"),
            Player("random", playerType = PlayerType.RANDOMAI),
            Player("smart", playerType = PlayerType.SMARTAI)
        )

        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        //SmartAi has currently no functionality, so it isn't tested
        //Make turn for normalPlayer
        playerService.placeTile(-1, 0)
        // call switchPlayer because it is only called in view after delay

        assert(game.activePlayerID == 1)
        assert(game.undoStack.size == 1)

        var placedTileIndex = 0
        for (row in game.gameLayout) {
            for (tile in row) {
                if (tile is PathTile) {
                    placedTileIndex++
                }
            }
        }

        assert(placedTileIndex == 1)
    }

    /**
     * Function to test the switchPlayer method by
     * calling a sequence where a normal player, a random
     * AI and a smart AI make a turn
     */
    @Test
    fun testSwitchPlayer1() {
        val gameService = rootService.gameService
        val playerService = rootService.playerService

        val players = mutableListOf(
            Player("player"),
            Player("random", playerType = PlayerType.RANDOMAI),
            Player("smart", playerType = PlayerType.SMARTAI)
        )

        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        //SmartAi has currently no functionality, so it isn't tested
        //Make turn for normalPlayer
        game.activePlayerID = 1
        playerService.placeTile(1, 1)
        // call switchPlayer because it is only called in view after delay
        gameService.makeAIPlayerTurn()

        assert(game.activePlayerID == 2)
        assert(game.undoStack.size == 1)


        var placedTileIndex = 0
        for (row in game.gameLayout) {
            for (tile in row) {
                if (tile is PathTile) {
                    placedTileIndex++
                }
            }
        }
        assert(placedTileIndex == 1)
    }

    /**
     * Function to test the switchPlayer method by
     * calling a sequence where a normal player, a random
     * AI and a smart AI make a turn
     */
    @Test
    fun testSwitchPlayer2() {
        val gameService = rootService.gameService
        val playerService = rootService.playerService

        val players = mutableListOf(
            Player("player"),
            Player("random", playerType = PlayerType.RANDOMAI),
            Player("smart", playerType = PlayerType.SMARTAI)
        )

        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        //SmartAi has currently no functionality, so it isn't tested
        //Make turn for normalPlayer

        game.activePlayerID = 2
        playerService.placeTile(1, 2)

        assert(game.activePlayerID == 0)
        assert(game.undoStack.size == 1)


        var placedTileIndex = 0
        for (row in game.gameLayout) {
            for (tile in row) {
                if (tile is PathTile) {
                    placedTileIndex++
                }
            }
        }

        assert(placedTileIndex == 1)
    }
}

