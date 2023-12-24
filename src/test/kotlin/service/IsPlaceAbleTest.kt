package service

import entity.*
import kotlin.test.*

/**
 * This test class verifies the functionality of the `isPlaceAble` method in the game service.
 * It includes tests for both valid and invalid tile placements according to game rules.
 */
class IsPlaceAbleTest {

    /**
     * Tests the `isPlaceAble` method for various scenarios where tile placement should be invalid.
     * Ensures the method correctly identifies and prohibits illegal placements.
     */
    @Test
    fun isPlaceAbleInvalidTest(){
        val game = RootService()
        val player = mutableListOf(
            Player("q",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
            , Player("q1",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame( player , false,1.0,false)
        val tile = PathTile(mutableMapOf(0 to 1 , 2 to 3 , 4 to 5 ),0,0,
            0, mutableListOf())

        val tile1 = PathTile(mutableMapOf(1 to 2 , 3 to 4 , 5 to 0),0,0,
            0, mutableListOf())


        //Check if a tile can be placed on centerTile or a treasurerTile
        assertFalse(game.gameService.isPlaceAble(0,0, tile))
        assertFalse(game.gameService.isPlaceAble(0,4, tile))
        assertFalse(game.gameService.isPlaceAble(0,-4, tile))
        assertFalse(game.gameService.isPlaceAble(-4,0, tile))
        assertFalse(game.gameService.isPlaceAble(4,-4, tile))
        assertFalse(game.gameService.isPlaceAble( 4,0,tile))
        assertFalse(game.gameService.isPlaceAble( -4,4,tile))

        //check if a tile that have connections from 4 to 5 or form 5 to 4 and on the positions
        assertFalse(game.gameService.isPlaceAble(-1,-3,tile))
        assertFalse(game.gameService.isPlaceAble(-2,-2,tile))
        assertFalse(game.gameService.isPlaceAble(-3,-1,tile))


        //check if a tile that have connections from 1 to 2 or form 2 to 1 and on the positions
        assertFalse(game.gameService.isPlaceAble(1,3,tile1))
        assertFalse(game.gameService.isPlaceAble(2,2,tile1))
        assertFalse(game.gameService.isPlaceAble(3,1,tile1))

        //check if a tile that have connections from 2 to 3 or form 3 to 2 and x = 4
        assertFalse(game.gameService.isPlaceAble(4,-1,tile))

        //check if a tile that have connections from 3 to 4 or form 4 to 3 and y = -4
        assertFalse(game.gameService.isPlaceAble(1,-4,tile1))

        //check if a tile that have connections from 5 to 0 or form 0 to 5 and x = -4
        assertFalse(game.gameService.isPlaceAble(-4,1,tile1))

        //check if a tile that have connections from 1 to 0 or form 0 to 1 and y = 4
        assertFalse(game.gameService.isPlaceAble(-1,4,tile))

    }

    /**
    * Tests the `isPlaceAble` method for scenarios where tile placement should be valid.
    * Checks that the method correctly allows legal tile placements.
    */
    @Test
    fun isPlaceAbleValidTest(){
        val game = RootService()
        val player = mutableListOf(
            Player("q",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
            , Player("q1",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame( player , false,1.0,false)
        val tile = PathTile(mutableMapOf(0 to 1 , 2 to 3 , 4 to 5 ),0,0,
            0, mutableListOf())

        val tile1 = PathTile(mutableMapOf(1 to 2 , 3 to 4 , 5 to 0),0,0,
            0, mutableListOf())


        //check if a tile that do not have connections from 4 to 5 or form 5 to 4 and on the positions
        assertTrue(game.gameService.isPlaceAble(-1,-3,tile1))
        assertTrue(game.gameService.isPlaceAble(-2,-2,tile1))
        assertTrue(game.gameService.isPlaceAble(-3,-1,tile1))


        //check if a tile that do not have connections from 1 to 2 or form 2 to 1 and on the positions
        assertTrue(game.gameService.isPlaceAble(1,3,tile))
        assertTrue(game.gameService.isPlaceAble(2,2,tile))
        assertTrue(game.gameService.isPlaceAble(3,1,tile))

        //check if a tile that do not have connections from 2 to 3 or form 3 to 2 and x = 4
        assertTrue(game.gameService.isPlaceAble(4,-1,tile1))

        //check if a tile that do not have connections from 3 to 4 or form 4 to 3 and y = -4
        assertTrue(game.gameService.isPlaceAble(1,-4,tile))

        //check if a tile that do not have connections from 5 to 0 or form 0 to 5 and x = -4
        assertTrue(game.gameService.isPlaceAble(-4,1,tile))

        //check if a tile that do not have connections from 1 to 0 or form 0 to 1 and y = 4
        assertTrue(game.gameService.isPlaceAble(-1,4,tile1))

    }

    /**
     * Tests the `isPlaceAble` method for various scenarios where tile placement should be invalid.
     * Ensures the method correctly identifies and prohibits illegal placements.
     */
    @Test
    fun isPlaceAbleOutsideTest(){
        val game = RootService()
        val player = mutableListOf(
            Player("q",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
            , Player("q1",0, PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame( player , false,1.0,false)
        val tile = PathTile(mutableMapOf(0 to 1 , 2 to 3 , 4 to 5 ),0,0,
            0, mutableListOf())

        //Check if a tile can be placed outside the allowed space
        assertFalse(game.gameService.isPlaceAble(5,1,tile))
        assertFalse(game.gameService.isPlaceAble(-5,1,tile))
        assertFalse(game.gameService.isPlaceAble(1,-5,tile))
        assertFalse(game.gameService.isPlaceAble(1,5,tile))

    }

    /**
     * Tests the `isPlaceAble` method for scenarios where tile placement should be valid.
     * Checks that the method correctly allows legal tile placements.
     */
    @Test
    fun isPlaceAbleInsideTest() {
        val game = RootService()
        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        game.gameService.startNewGame(player, false, 1.0, false)


        val tile1 = PathTile(
            mutableMapOf(1 to 2, 3 to 4, 5 to 0), 0, 0,
            0, mutableListOf()
        )


        //Check if other positions are valid.
        assertTrue(game.gameService.isPlaceAble(2, 1, tile1))
        assertTrue(game.gameService.isPlaceAble(1, 2, tile1))
        assertTrue(game.gameService.isPlaceAble(-2, 1, tile1))
        assertTrue(game.gameService.isPlaceAble(1, -2, tile1))

    }
}