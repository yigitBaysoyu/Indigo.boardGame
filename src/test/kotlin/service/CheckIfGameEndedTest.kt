package service

import entity.GemType
import entity.PathTile
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This test class verifies the functionality of the `CheckIfGameEnded` method in the game service.
 * It includes tests to check if the game should be ended.
 */
class CheckIfGameEndedTest {

    /**
     * Tests if the game ends correctly when all the gems have reached the gates.
     */
    @Test
    fun testCheckIfGameEnded(){
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
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        val gem = GemType.SAPPHIRE
        repeat(12) {
            newGame.gateList[0][1].gemsCollected.add(gem)
        }

        //check if checkIfGameEnded works after adding 12 gems to the gates
        game.gameService.checkIfGameEnded()
        assertTrue(test.refreshAfterEndGameCalled)

    }

    /**
     * Tests whether the game does not end when not all the gems have reached the gates.
     * Then, it checks if the game ends correctly when all the gems have reached the gates.
     */
    @Test
    fun testCheckIfGameEnded1(){
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
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        val gem = GemType.SAPPHIRE

        newGame.gateList[0][1].gemsCollected.add(gem)
        newGame.gateList[0][2].gemsCollected.add(gem)
        newGame.gateList[1][3].gemsCollected.add(gem)
        newGame.gateList[1][3].gemsCollected.add(gem)
        newGame.gateList[5][2].gemsCollected.add(gem)
        newGame.gateList[4][1].gemsCollected.add(gem)
        newGame.gateList[5][3].gemsCollected.add(gem)
        newGame.gateList[4][2].gemsCollected.add(gem)
        newGame.gateList[5][1].gemsCollected.add(gem)
        newGame.gateList[2][1].gemsCollected.add(gem)

        //check if checkIfGameEnded does not work after adding 10 gems to the gates
        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)

        newGame.gateList[3][2].gemsCollected.add(gem)
        newGame.gateList[1][0].gemsCollected.add(gem)

        //check if checkIfGameEnded works after adding 12 gems to the gates
        game.gameService.checkIfGameEnded()
        assertTrue(test.refreshAfterEndGameCalled)

    }

    /**
     * Tests if the game does not end when not all the gems have reached the gates.
     */
    @Test
    fun testCheckIfGameNotEnded(){
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
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        val gem = GemType.SAPPHIRE
        repeat(11) {
            newGame.gateList[0][1].gemsCollected.add(gem)
        }

        //check if checkIfGameEnded do not work after adding 11 gems to the gates
        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)

    }
}