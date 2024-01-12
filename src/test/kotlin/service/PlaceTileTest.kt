package service
import entity.*
import kotlin.test.*
import kotlin.test.assertEquals
/**
 * Testet die Platzierung eines Tiles im Spiel.
 */
class PlaceTileTest {

    /**
     * Überprüft die korrekte Platzierung eines Tiles an gültigen Koordinaten.
     */
    @Test
    fun testPlaceTile() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val player1 = Player("A", score = 4)
        val player2 = Player("B", score = 6)
        val players = mutableListOf( player1, player2)
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        assertNotNull(game)
        val originalActivePlayerID = game.activePlayerID
        val DrawPileSize = game.drawPile.size
        val PlayerHandSize = game.playerList[game.activePlayerID].playHand.size

        rootService.playerService.placeTile(0,1)
        assertNotEquals(originalActivePlayerID, game.activePlayerID)
        assertEquals(DrawPileSize - 1, game.drawPile.size)
        assertNotEquals(PlayerHandSize - 1, game.playerList[originalActivePlayerID].playHand.size)
        }



    }
    /**
     * Überprüft das Verhalten bei ungültigen Koordinaten für die Platzierung eines Tiles.
     */
    @Test
    fun testPlaceTileInvalidCoordinates() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val player1 = Player("A", score = 4)
        val player2 = Player("B", score = 6)
        val players = mutableListOf( player1, player2)
        gameService.startNewGame(players, false, 1.0, false)


        val game = rootService.currentGame
        assertNotNull(game)
        val originalActivePlayerID = game.activePlayerID
        val DrawPileSize = game.drawPile.size
        val PlayerHandSize = game.playerList[game.activePlayerID].playHand.size

        rootService.playerService.placeTile(-4,-9)
        assertEquals(originalActivePlayerID, game.activePlayerID)
        assertNotEquals(DrawPileSize - 1, game.drawPile.size)
        assertNotEquals(PlayerHandSize - 1, game.playerList[originalActivePlayerID].playHand.size)
    }

