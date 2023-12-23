package service

import kotlin.test.*
import entity.*

class GameServiceTest {

    @Test
    fun testSetGate(){
        val game = RootService()
        val player = mutableListOf(Player("q",0,PlayerType.LOCALPLAYER,0, mutableListOf(), mutableListOf())
        ,Player("q1",0,PlayerType.LOCALPLAYER,0, mutableListOf(), mutableListOf()))
        game.gameService.startNewGame( player , false,1.0,false)
        //game.gameService.setGates(false)
        val player1 = mutableListOf(Player("q",0,PlayerType.LOCALPLAYER,0, mutableListOf(), mutableListOf())
            ,Player("q1",0,PlayerType.LOCALPLAYER,0, mutableListOf(), mutableListOf())
            ,Player("q2",0,PlayerType.LOCALPLAYER,0, mutableListOf(), mutableListOf()))
        game.gameService.startNewGame( player1 , true,1.0,false)
        //game.gameService.setGates(true)
    }

    /**
     * Currently only testing if search for end of path succeeds
     * on a simple test domain containing 5 tiles with one connection
     * each
     *
     * Tile2 is "placed" and the gem should be moved from tile1 connection 3
     * to tile5 connection 1
     */
    @Test
    fun testMoveGems(){
        val rootService = RootService()
        val gameService = rootService.gameService

        val players = mutableListOf(Player("p"), Player("s"), Player("ss"))
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)

        //Creating five connection maps for the five tiles
        val c1 = mutableMapOf<Int,Int>()
        c1[0] = 3
        c1[3] = 0

        val c2 = mutableMapOf<Int,Int>()
        c2[0] = 4
        c2[4] = 0

        val c3 = mutableMapOf<Int,Int>()
        c3[1] = 3
        c3[3] = 1

        val c4 = mutableMapOf<Int,Int>()
        c4[0] = 1
        c4[1] = 0

        val c5 = mutableMapOf<Int,Int>()
        c5[1] = 4
        c5[4] = 1

        //Creating 5 tiles
        val tile1 = PathTile(c1, xCoordinate = 1, yCoordinate = 1)
        val tile2 = PathTile(c2, xCoordinate = 0, yCoordinate = 2)
        val tile3 = PathTile(c3, xCoordinate = -1, yCoordinate = 2)
        val tile4 = PathTile(c4, xCoordinate = -2, yCoordinate = 3)
        val tile5 = PathTile(c5, xCoordinate = -1, yCoordinate = 3)

        //Filling gem list of each tile
        for(i in 0..5){
            tile1.gemPositions.add(GemType.NONE)
            tile2.gemPositions.add(GemType.NONE)
            tile3.gemPositions.add(GemType.NONE)
            tile4.gemPositions.add(GemType.NONE)
            tile5.gemPositions.add(GemType.NONE)
        }
        //Adding the stone that needs to be moved
        tile1.gemPositions.removeAt(3)
        tile1.gemPositions.add(3, GemType.SAPPHIRE)

        gameService.setTileFromAxialCoordinates(1,1, tile1)
        gameService.setTileFromAxialCoordinates(0,2, tile2)
        gameService.setTileFromAxialCoordinates(-1,2, tile3)
        gameService.setTileFromAxialCoordinates(-2,3, tile4)
        gameService.setTileFromAxialCoordinates(-1,3, tile5)

        val turn = Turn(1, mutableListOf(0,0,0), tile2)
        //Tile two is the tile that gets "placed"
        gameService.moveGems(turn, tile2)

        assert(tile1.gemPositions[3] == GemType.NONE)
        assert(tile5.gemPositions[1] == GemType.SAPPHIRE)
    }
}