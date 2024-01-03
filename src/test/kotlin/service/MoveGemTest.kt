package service

import kotlin.test.*
import entity.*

class MoveGemTest {

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

        val player = game.getActivePlayer()
        val gate = gameService.getTileFromAxialCoordinates(5, -2)
        if(gate !is GateTile){
            throw Error("Error getting the gate assigned to the player")
        }
        player.gateList.add(gate)

        val c1 = mutableMapOf<Int,Int>()
        c1[0] = 2
        c1[2] = 0

        val c2 = mutableMapOf<Int,Int>()
        c2[3] = 5
        c2[5] = 3

        val c3 = mutableMapOf<Int,Int>()
        c3[1] = 4
        c3[4] = 1

        val c4 = mutableMapOf<Int,Int>()
        c4[1] = 3
        c4[3] = 1

        val c5 = mutableMapOf<Int,Int>()
        c5[0] = 4
        c5[4] = 0

        val c6 = mutableMapOf<Int, Int>()
        c6[1] = 3
        c6[3] = 1

        val c7 = mutableMapOf<Int, Int>()
        c7[1] = 4
        c7[4] = 1

        val gateConnections = mutableMapOf<Int,Int>()
        gateConnections[0] = 4
        gateConnections[4] = 0

        val placedTileConnections = mutableMapOf<Int, Int>()
        placedTileConnections[0] = 4
        placedTileConnections[4] = 0
        placedTileConnections[1] = 3
        placedTileConnections[3] = 1
        placedTileConnections[2] = 5
        placedTileConnections[5] = 2

        val treasureTileConn = mutableMapOf<Int, Int>()
        treasureTileConn[4] = 4

        val centerConnections = mutableMapOf<Int, Int>()

        val centerTileGems = ArrayDeque<GemType>()
        centerTileGems.add(GemType.SAPPHIRE)
        for (i in 0..4) centerTileGems.add(GemType.EMERALD)

        //Creating 5 tiles
        val centerTile = CenterTile(centerConnections, 0, 0, 0, centerTileGems)
        val placedTile = PathTile(placedTileConnections, 0, 1, 0, mutableListOf<GemType>())
        val treasureTile = TreasureTile(treasureTileConn, 0, 4, 0, mutableListOf<GemType>())

        val tile1 = PathTile(c1, xCoordinate = 1, yCoordinate = -1)
        val tile2 = PathTile(c2, xCoordinate = 1, yCoordinate = 1)
        val tile3 = PathTile(c3, xCoordinate = 2, yCoordinate = 0)
        val tile4 = PathTile(c4, xCoordinate = 2, yCoordinate = -1)
        val tile5 = PathTile(c5, xCoordinate = 3, yCoordinate = -1)
        val tile6 = PathTile(c6, xCoordinate = 4, yCoordinate = -2)
        val tile7 = PathTile(c7, xCoordinate = 3, yCoordinate = 0)

        //Filling gem list of each tile
        for(i in 0..5){
            placedTile.gemPositions.add(GemType.NONE)
            treasureTile.gemPositions.add(GemType.NONE)
            tile1.gemPositions.add(GemType.NONE)
            tile2.gemPositions.add(GemType.NONE)
            tile3.gemPositions.add(GemType.NONE)
            tile4.gemPositions.add(GemType.NONE)
            tile5.gemPositions.add(GemType.NONE)
            tile6.gemPositions.add(GemType.NONE)
            tile7.gemPositions.add(GemType.NONE)
        }
        //Adding the stone that needs to be moved
        tile3.gemPositions[4] = GemType.AMBER
        tile1.gemPositions[2] =GemType.AMBER
        tile2.gemPositions[5] = GemType.EMERALD

        gameService.setTileFromAxialCoordinates(0,0,centerTile)
        gameService.setTileFromAxialCoordinates(1, 0, placedTile)
        gameService.setTileFromAxialCoordinates(4,0,treasureTile)

        gameService.setTileFromAxialCoordinates(1,-1, tile1)
        gameService.setTileFromAxialCoordinates(1,1, tile2)
        gameService.setTileFromAxialCoordinates(2,0, tile3)
        gameService.setTileFromAxialCoordinates(2,-1, tile4)
        gameService.setTileFromAxialCoordinates(3,-1, tile5)
        gameService.setTileFromAxialCoordinates(4,-2, tile6)
        gameService.setTileFromAxialCoordinates(3,0, tile7)

        val turn = Turn(0, mutableListOf(0,0,0), placedTile)

        gameService.moveGems(turn, placedTile)

        //Check for Gem in correct position
        assert(placedTile.gemPositions[3] ==GemType.AMBER)
        assert(turn.gemMovements[0].didCollide && turn.gemMovements[1].didCollide) //Check if collision report is made
        //Check that the score is updated
        assert(player.score == 2)
        //Check for the gem in the correct gate
        assert(gate.gemsCollected.first() == GemType.EMERALD)
    }
}