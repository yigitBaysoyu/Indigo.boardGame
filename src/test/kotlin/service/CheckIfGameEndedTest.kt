package service

import entity.*
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
     * Tests if the game ends correctly when all the gems have removed from the field.
     */
    @Test
    fun testCheckIfGameEnded() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val tile2 = TreasureTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val deque: ArrayDeque<GemType> = ArrayDeque()
        deque.addAll(mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE))

        val tile3 = CenterTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, deque
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



        newGame.gameLayout[0] = mutableListOf(tile1, tile2, tile3)
        for (i in 1 until newGame.gameLayout.size) {
            newGame.gameLayout[i] = mutableListOf()
        }

        game.gameService.checkIfGameEnded()
        assertTrue(test.refreshAfterEndGameCalled)

    }

    /**
     * Tests whether the game does not end when not all the gems have removed from the filed.
     * Then, it checks if the game ends correctly when all the gems have removed from the field.
     */
    @Test
    fun testCheckIfGameEnded1() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )
        //check if the assertion are working
        assertThrows<IllegalStateException> { game.gameService.checkIfGameEnded() }
        assertThrows<IllegalStateException> { game.gameService.isPlaceAble(0, 0, tile) }

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)


        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val tile2 = TreasureTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val deque: ArrayDeque<GemType> = ArrayDeque()
        deque.addAll(mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE))

        val tile3 = CenterTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, deque
        )

        newGame.gameLayout[1] = mutableListOf(tile1)
        newGame.gameLayout[2] = mutableListOf(tile2)
        newGame.gameLayout[0] = mutableListOf(tile3)

        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)

        for (i in 3 until newGame.gameLayout.size) {
            newGame.gameLayout[i] = mutableListOf(tile1)
        }

        game.gameService.checkIfGameEnded()
        assertTrue(test.refreshAfterEndGameCalled)

    }

    /**
     * Tests if the game does not end when not all the gems have removed from the field.
     */
    @Test
    fun testCheckIfGameNotEnded() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )

        assertThrows<IllegalStateException> { game.gameService.checkIfGameEnded() }
        assertThrows<IllegalStateException> { game.gameService.isPlaceAble(0, 0, tile) }

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val tile2 = TreasureTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val deque: ArrayDeque<GemType> = ArrayDeque()
        deque.addAll(mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE))

        val tile3 = CenterTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, deque
        )

        newGame.gameLayout[1] = mutableListOf(tile1)
        newGame.gameLayout[2] = mutableListOf(tile2)
        newGame.gameLayout[0] = mutableListOf(tile3)

        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)

        for (i in 3 until newGame.gameLayout.size - 3) {
            newGame.gameLayout[i] = mutableListOf(tile1)
        }
        val tile4 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.SAPPHIRE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        newGame.gameLayout[newGame.gameLayout.size - 2] = mutableListOf(tile4)

        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)


    }

    /**
     * Tests if the game does not end when not all the tiles have been placed on the field.
     */
    @Test
    fun testCheckIfGameNotEnded1() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )
        //check if the assertion are working
        assertThrows<IllegalStateException> { game.gameService.checkIfGameEnded() }
        assertThrows<IllegalStateException> { game.gameService.isPlaceAble(0, 0, tile) }

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)


        val deque: ArrayDeque<GemType> = ArrayDeque()
        deque.addAll(mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE))




            newGame.gameLayout[newGame.gameLayout.size - 1] = mutableListOf(
                EmptyTile(mutableMapOf(), 0, 9, 0)
            )


        game.gameService.checkIfGameEnded()
        assertFalse(test.refreshAfterEndGameCalled)


    }

    /**
     * Tests if the game does not end when all the tiles have been placed on the field.
     */
    @Test
    fun testCheckIfGameEnded2() {
        val game = RootService()

        val test = TestRefreshable()
        test.reset()
        game.addRefreshables(test)


        val tile = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )
        //check if the assertion are working
        assertThrows<IllegalStateException> { game.gameService.checkIfGameEnded() }
        assertThrows<IllegalStateException> { game.gameService.isPlaceAble(0, 0, tile) }

        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 6, mutableListOf(), mutableListOf())
        )

        game.gameService.startNewGame(player, false, 1.0, false)

        val newGame = game.currentGame
        checkNotNull(newGame)

        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val tile2 = TreasureTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        val deque: ArrayDeque<GemType> = ArrayDeque()
        deque.addAll(mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE))

        val tile3 = CenterTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, deque
        )

        for (i in 0 until newGame.gameLayout.size ) {
            newGame.gameLayout[i] = mutableListOf(tile1 , tile2 , tile3)
        }

            game.gameService.checkIfGameEnded()
            assertTrue(test.refreshAfterEndGameCalled)

        }

    }
