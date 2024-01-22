package service

import kotlin.test.*
import entity.*

/**
 * This test class verifies the functionality of the `moveGems` method in the Game service.
 */

class MoveGemTest {

    /**
     * Testing [GameService.moveGems] and all methods used by it to check
     * whether gems are moved correctly across the board after a tile is placed
     *
     * Testing collisions on all tile types, movement of gems from all tile types,
     * scoring movements and creation of [Turn.gemMovements]
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

        //Creating tiles
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

        gameService.moveGems(turn)

        //Check for Gem in correct position
        assert(placedTile.gemPositions[3] ==GemType.AMBER)
        assert(turn.gemMovements[0].didCollide && turn.gemMovements[1].didCollide) //Check if collision report is made
        //Check that the score is updated
        assert(player.score == 2)

        //Test gem movement's
        for(move in turn.gemMovements){
            assert(move.startTile != placedTile)
        }

        val collisionMove1 = turn.gemMovements[0]
        val collisionMove2 = turn.gemMovements[1]
        assert(collisionMove1.didCollide && collisionMove2.didCollide)
        assert(collisionMove1.endTile == collisionMove2.endTile)

        val scoringMove = turn.gemMovements[2]
        assert(scoringMove.endTile is GateTile)
        assert(!scoringMove.didCollide)

        val gemMove = turn.gemMovements[3]
        assert(placedTile.gemPositions[gemMove.positionOnEndTile] == gemMove.gemType)
        assert(!gemMove.didCollide)

        //Check for the gem in the correct gate
        assert(gate.gemsCollected.first() == GemType.EMERALD)

        //Test interaction of a placed Tile next to a treasureTile
        val placedConnections2 = mutableMapOf<Int,Int>()
        placedConnections2[0] = 2
        placedConnections2[2] = 0
        placedConnections2[4] = 5
        placedConnections2[5] = 4

        val placedTile2 = PathTile(placedConnections2, xCoordinate = -3, yCoordinate = 4)

        val treasureTile2Connections = mutableMapOf<Int, Int>()
        treasureTile2Connections[0] = 0
        treasureTile2Connections[1] = 5
        treasureTile2Connections[5] = 1

        val treasureTile2 = TreasureTile(treasureTile2Connections, xCoordinate = -4, yCoordinate = 4)

        val c8 = mutableMapOf<Int, Int>()
        c8[1] = 3
        c8[3] = 1

        val tile8 = PathTile(c8, xCoordinate = -2, yCoordinate = 3)

        val c9 = mutableMapOf<Int, Int>()
        c9[2] = 3
        c9[3] = 2

        val tile9 = PathTile(c9, xCoordinate = -3, yCoordinate = 3)

        for(i in 0..5){
            placedTile2.gemPositions.add(GemType.NONE)
            tile8.gemPositions.add(GemType.NONE)
            tile9.gemPositions.add(GemType.NONE)
            treasureTile2.gemPositions.add(GemType.NONE)
        }

        //Add distinct gems
        tile8.gemPositions[3] = GemType.EMERALD
        tile9.gemPositions[2] = GemType.AMBER

        gameService.setTileFromAxialCoordinates(placedTile2.xCoordinate, placedTile2.yCoordinate, placedTile2)
        gameService.setTileFromAxialCoordinates(tile8.xCoordinate, tile8.yCoordinate, tile8)
        gameService.setTileFromAxialCoordinates(tile9.xCoordinate, tile9.yCoordinate, tile9)
        gameService.setTileFromAxialCoordinates(treasureTile2.xCoordinate, treasureTile2.yCoordinate, treasureTile2)

        //Create player and turn
        val player2 = game.playerList[1]
        val turn2 = Turn(1,mutableListOf(0,0,0), placedTile2)

        //Get gate tile and add to player
        val gate2 = gameService.getTileFromAxialCoordinates(-3, 5)
        if(gate2 !is GateTile){
            throw Error("Error getting the gate assigned to the player")
        }
        player2.gateList.add(gate2)

        gameService.moveGems(turn2,)

        //Check for gems at correct positions
        assert(treasureTile2.gemPositions[5] == GemType.AMBER)
        assert(gate2.gemsCollected.first() == GemType.EMERALD)

        //assert scoring move happened
        assert(player2.score == 2)

        //asserting turn validity
        assert(turn2.gemMovements[0].endTile == gate2)
        assert(!turn2.gemMovements[0].didCollide)
        assert(turn2.gemMovements[0].startTile == tile8)
        assert(turn2.scoreChanges[1] == 2)

        //Testing for collision of a gem coming from a treasure tile and center tile
        val placedTile3Connections = mutableMapOf<Int, Int>()
        placedTile3Connections[0] = 2
        placedTile3Connections[2] = 0

        val placedTile3 = PathTile(placedTile3Connections, xCoordinate = 0, yCoordinate = 3)

        val treasureTile3Connections = mutableMapOf<Int, Int>()
        treasureTile3Connections[0] = 4
        treasureTile3Connections[4] = 0
        treasureTile3Connections[5] = 5

        val treasureTile3 = TreasureTile(treasureTile3Connections, xCoordinate = 0, yCoordinate = 4)

        val c10 = mutableMapOf<Int, Int>()
        c10[2] = 3
        c10[3] = 2

        val tile10 = PathTile(c10, xCoordinate = 1, yCoordinate = 2)

        for (i in 0..5){
            placedTile3.gemPositions.add(GemType.NONE)
            treasureTile3.gemPositions.add(GemType.NONE)
            tile10.gemPositions.add(GemType.NONE)
        }

        tile10.gemPositions[3] = GemType.SAPPHIRE
        treasureTile3.gemPositions[5] = GemType.AMBER

        gameService.setTileFromAxialCoordinates(placedTile3.xCoordinate, placedTile3.yCoordinate, placedTile3)
        gameService.setTileFromAxialCoordinates(treasureTile3.xCoordinate, treasureTile3.yCoordinate, treasureTile3)
        gameService.setTileFromAxialCoordinates(tile10.xCoordinate, tile10.yCoordinate, tile10)

        val turn3 = Turn(2, mutableListOf(0,0,0), placedTile3)
        gameService.moveGems(turn3)

        //Check validity of collision report
        assert(turn3.gemMovements[0].didCollide && turn3.gemMovements[1].didCollide)

        assert(turn3.gemMovements[1].startTile == treasureTile3)
        assert(turn3.gemMovements[0].startTile == tile10)
        assert(turn3.gemMovements[0].endTile == turn3.gemMovements[1].endTile)

        //Turn 4 test: Moving gems starting at treasure tile
        val placedTile4Connections = mutableMapOf<Int, Int>()
        placedTile4Connections[0] = 2
        placedTile4Connections[2] = 0

        val placedTile4 = PathTile(placedTile4Connections, xCoordinate = -4, yCoordinate = 3)
        for(i in 0..5){
            placedTile4.gemPositions.add(GemType.NONE)
        }

        gameService.setTileFromAxialCoordinates(-4, 3, placedTile4)

        val turn4 = Turn(2, mutableListOf(0,0,0), placedTile4)
        gameService.moveGems(turn4)

        assert(placedTile4.gemPositions[0] == GemType.AMBER)
        assert(turn4.gemMovements[0].endTile == placedTile4)
        assert(!turn4.gemMovements[0].didCollide)

        //Turn 5 test
        val c11 = mutableMapOf<Int, Int>()
        c11[0] = 3
        c11[3] = 0

        val tile11 = PathTile(c11, xCoordinate = 0, yCoordinate = -1)

        val placedTile5Connections = mutableMapOf<Int,Int>()
        placedTile5Connections[0] = 1
        placedTile5Connections[1] = 0
        val placedTile5 = PathTile(placedTile5Connections, xCoordinate = -1, yCoordinate = 0)

        for(i in 0..5){
            placedTile5.gemPositions.add(GemType.NONE)
            tile11.gemPositions.add(GemType.NONE)
        }
        tile11.gemPositions[3] = GemType.EMERALD

        gameService.setTileFromAxialCoordinates(-1, 0, placedTile5)
        gameService.setTileFromAxialCoordinates(0, -1, tile11)

        val turn5 = Turn(2, mutableListOf(0,0,0), placedTile5)

        gameService.moveGems(turn5)
        assert(turn5.gemMovements[0].didCollide && turn5.gemMovements[1].didCollide)
        assert(!(GemType.EMERALD in placedTile5.gemPositions || GemType.EMERALD in tile11.gemPositions))
    }
}