package service

import entity.PathTile
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * This class performs tests for game start scenarios.
 * It uses `RootService` for game logic and `TestRefreshable` for verifying the update logic.
 */
class StartNewGameTest {

    /**
     * Tests the game start behavior with two players.
     * Verifies the initialization of the game components and the correct distribution of gates.
     */
    @Test
    fun testStartNewGameTowPlayers() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )
        //check if the assertion are working
        assertThrows<IllegalStateException> { game.gameService.checkIfGameEnded() }
        assertThrows<IllegalStateException> { game.gameService.isPlaceAble(0, 0, tile1) }

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        //check if the initials of the game components are correct
        assertEquals(6, newGame.gateList.size)
        assertEquals(24, newGame.gateList.flatten().size)
        assertEquals(12, newGame.playerList[0].gateList.size)
        assertEquals(12, newGame.playerList[1].gateList.size)
        assertEquals(0, newGame.redoStack.size)
        assertEquals(0, newGame.undoStack.size)
        assertEquals(54, newGame.drawPile.size)

        //check if the players shares gates with each other
        assertNotEquals(newGame.playerList[0].gateList, newGame.playerList[1].gateList)

        //check if the call of Refreshs are correct
        assertTrue(test.refreshAfterStartNewGameCalled)


        val player1 = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )

        //check if the assertion is correct, when threePlayerVariant == true and the number of player == 2
        assertThrows<IllegalArgumentException> {
            game.gameService.startNewGame(player1, true, 1.0, false)
        }

        val player2 = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        //check if the assertion is correct, true and the number of player < 2 or > 4
        assertThrows<IllegalArgumentException> {
            game.gameService.startNewGame(player2, false, 1.0, false)
        }

        val player3 = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
        )

        assertThrows<IllegalArgumentException> {
            game.gameService.startNewGame(player3, false, 1.0, false)
        }

    }

    /**
     * Tests the game start behavior with three players.
     * When the threePlayerVariant is false.
     * Verifies the initialization of the game components and the correct distribution of gates.
     */
    @Test
    fun testStartNewGameThreePlayersFalse() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)
        val newGame = game.currentGame
        checkNotNull(newGame)
        //check if the initials of the game components are correct
        assertEquals(6, newGame.gateList.size)
        assertEquals(24, newGame.gateList.flatten().size)
        assertEquals(8, newGame.playerList[0].gateList.size)
        assertEquals(8, newGame.playerList[1].gateList.size)
        assertEquals(8, newGame.playerList[2].gateList.size)
        assertEquals(0, newGame.redoStack.size)
        assertEquals(0, newGame.undoStack.size)
        assertEquals(54, newGame.drawPile.size)

        //check if the players shares gates with each other
        assertNotEquals(newGame.playerList[0].gateList, newGame.playerList[1].gateList)
        assertNotEquals(newGame.playerList[1].gateList, newGame.playerList[2].gateList)
        assertNotEquals(newGame.playerList[0].gateList, newGame.playerList[2].gateList)

        //check if the call of Refreshs are correct
        assertTrue(test.refreshAfterStartNewGameCalled)


    }

    /**
     * Tests the game start behavior with three players.
     * When the threePlayerVariant is true.
     * Verifies the initialization of the game components and the correct distribution of gates.
     */
    @Test
    fun testStartNewGameThreePlayersTrue() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, true, 1.0, false)
        val newGame = game.currentGame
        checkNotNull(newGame)

        //check if the initials of the game components are correct
        assertEquals(6, newGame.gateList.size)
        assertEquals(24, newGame.gateList.flatten().size)
        assertEquals(12, newGame.playerList[0].gateList.size)
        assertEquals(12, newGame.playerList[1].gateList.size)
        assertEquals(12, newGame.playerList[2].gateList.size)
        assertEquals(0, newGame.redoStack.size)
        assertEquals(0, newGame.undoStack.size)
        assertEquals(54, newGame.drawPile.size)

        //check if player1 shares gates with player 2
        var shared = 0
        newGame.playerList[0].gateList.forEach { gateTile ->
            newGame.playerList[1].gateList.forEach { gateTile1 ->
                if (gateTile == gateTile1) {
                    shared++
                }
            }
        }
        assertEquals(4, shared)

        //check if player 1 shares gates with player 3
        var shared1 = 0
        newGame.playerList[0].gateList.forEach { gateTile ->
            newGame.playerList[2].gateList.forEach { gateTile1 ->
                if (gateTile == gateTile1) {
                    shared1++
                }
            }
        }
        assertEquals(4, shared1)

        //check if player 3 shares gates with player 2
        var shared2 = 0
        newGame.playerList[1].gateList.forEach { gateTile ->
            newGame.playerList[2].gateList.forEach { gateTile1 ->
                if (gateTile == gateTile1) {
                    shared2++
                }
            }
        }
        assertEquals(4, shared2)

        //check if the call of Refreshs are correct
        assertTrue(test.refreshAfterStartNewGameCalled)


    }


    /**
     * Tests the game start behavior with four players.
     * Verifies the initialization of the game components and the correct distribution of gates.
     */
    @Test
    fun testStartNewGameFourPlayers() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)
        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q3", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        //check if the initials of the game components are correct
        assertEquals(6, newGame.gateList.size)
        assertEquals(24, newGame.gateList.flatten().size)
        assertEquals(12, newGame.playerList[0].gateList.size)
        assertEquals(12, newGame.playerList[1].gateList.size)
        assertEquals(12, newGame.playerList[2].gateList.size)
        assertEquals(12, newGame.playerList[3].gateList.size)
        assertEquals(0, newGame.redoStack.size)
        assertEquals(0, newGame.undoStack.size)
        assertEquals(54, newGame.drawPile.size)


        //check if the call of Refreshs are correct
        assertTrue(test.refreshAfterStartNewGameCalled)

    }

    /**
     * Tests the game start behavior with four players.
     * Verifies the initialization of the game components and the correct distribution of gates.
     */
    @Test
    fun testStartNewGameFourPlayersGates() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)
        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q3", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)
        //check if player1 shares gates with player 2
        assertEquals(4 , countSharedGates(newGame.playerList[0] , newGame.playerList[1]))

        //check if player 1 shares gates with player 3
        assertEquals(4 , countSharedGates(newGame.playerList[0] , newGame.playerList[2]))

        //check if player 3 shares gates with player 2
        assertEquals(4 , countSharedGates(newGame.playerList[2] , newGame.playerList[1]))

        //check if player1 shares gates with player 4
        assertEquals(4 , countSharedGates(newGame.playerList[0] , newGame.playerList[3]))

        //check if player 2  shares gates with player 4
        assertEquals(4 , countSharedGates(newGame.playerList[3] , newGame.playerList[1]))

        //check if player 3  shares gates with player 4
        assertEquals(4 , countSharedGates(newGame.playerList[2] , newGame.playerList[3]))

    }

    /**
     * Private function helps to counting the shared gates
     */
    private fun countSharedGates(player1 : Player , player2: Player) : Int{

        var shared = 0
        player1.gateList.forEach { gateTile ->
            player2.gateList.forEach { gateTile1 ->
                if (gateTile == gateTile1) {
                    shared++
                }
            }
        }
        return shared
    }
}




