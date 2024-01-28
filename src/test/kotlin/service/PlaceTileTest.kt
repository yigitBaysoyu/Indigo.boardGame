package service
import entity.*
import kotlin.test.*
import kotlin.test.assertEquals

/**
 * Tests the placement of a tile in the game.
 */
class PlaceTileTest {

    /**
     * Checks the correct placement of a tile at valid coordinates.
     */
    @Test
    fun testPlaceTile() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val test = TestRefreshable()
        test.reset()
        rootService.addRefreshables(test)

        assertFalse(test.refreshAfterTilePlacedCalled)
        val player1 = Player("A", score = 4)
        val player2 = Player("B", score = 6)
        val players = mutableListOf(player1, player2)
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        assertNotNull(game)

        val originalActivePlayerID = game.activePlayerID
        val drawPileSize = game.drawPile.size
        val playerHandSize = game.playerList[game.activePlayerID].playHand.size

        rootService.playerService.placeTile(0, 1)
        assertNotEquals(originalActivePlayerID, game.activePlayerID)
        assertEquals(drawPileSize - 1, game.drawPile.size)
        assertNotEquals(playerHandSize - 1, game.playerList[originalActivePlayerID].playHand.size)
        assertTrue(test.refreshAfterTilePlacedCalled)

        rootService.playerService.placeTile(0,1)
        assert(game.redoStack.size == 0)

        game.drawPile.clear()
        rootService.playerService.placeTile(2,2)
        assert(game.playerList[1].playHand.size == 0)


    }

    /**
     * Checks the behavior when using invalid coordinates for tile placement.
     */
    @Test
    fun testPlaceTileInvalidCoordinates() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val player1 = Player("A", score = 4)
        val player2 = Player("B", score = 6)
        val players = mutableListOf(player1, player2)
        gameService.startNewGame(players, false, 1.0, false)


        val game = rootService.currentGame
        assertNotNull(game)
        val originalActivePlayerID = game.activePlayerID
        val drawPileSize = game.drawPile.size
        val playerHandSize = game.playerList[game.activePlayerID].playHand.size

        rootService.playerService.placeTile(-4, -9)
        assertEquals(originalActivePlayerID, game.activePlayerID)
        assertNotEquals(drawPileSize - 1, game.drawPile.size)
        assertNotEquals(playerHandSize - 1, game.playerList[originalActivePlayerID].playHand.size)

    }
}

