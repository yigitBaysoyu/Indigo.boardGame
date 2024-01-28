package service
import entity.*
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertTrue
import kotlin.collections.last as last

/**
 * Test class for the undo method of the PlayerService class.
 */
class UndoTest {
    private lateinit var gameService: GameService
    private lateinit var rootService: RootService
    private lateinit var playerService: PlayerService

    /**
     * setUp provides the necessary services.
     */
    @BeforeEach
    fun setUp() {
        rootService = RootService()
        gameService = rootService.gameService
        playerService = rootService.playerService

    }

    /**
     * Tests the behavior of Undo when the Undo stack is empty.
     */
    @Test
    fun testUndoEmptyStack() {

        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)
        val game = rootService.currentGame
        checkNotNull(game)
        assertTrue(game.undoStack.isEmpty())
        rootService.playerService.undo()
        assertTrue(game.undoStack.isEmpty())
    }

    /**
     * Tests Undo behavior when there is a score change.
     */
    @Test
    fun testUndoScoreChange(){
        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)

        val tile = PathTile(
            mapOf(0 to 1, 2 to 3, 4 to 5), 0, 1, 1,
            mutableListOf()
        )

        game.playerList[0].score = 7
        game.playerList[1].score = 5

        val turn = Turn(0, mutableListOf(1,2),tile, mutableListOf() )
        game.undoStack.add(turn)

        playerService.undo()

        assert(game.playerList[0].score == 6)
        assert(game.playerList[1].score == 3)

    }

    /**
     * Tests Undo behavior for gem movement if the startTile is PathTile.
     */
    @Test
    fun testUndoGemMovementPathTile(){

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        val tile = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf(GemType.NONE, GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE)
        )

        val tile1 = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf()
        )

        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(1,-1)

        game.playerList[game.activePlayerID].playHand[0] = tile1
        playerService.placeTile(2,-2)

        val turn = game.undoStack.last()
        val movement = turn.gemMovements.last()

        playerService.undo()


        assert(movement.positionOnStartTile == 0)
        assert(movement.positionOnEndTile == 0)
        assert(movement.gemType == GemType.EMERALD)
        assert(movement.startTile.xCoordinate == 1)
        assert(movement.startTile.yCoordinate == -1)
        assert(movement.startTile.rotationOffset == 0)
        assert(movement.startTile.connections == tile.connections)
        assert(movement.endTile.xCoordinate == 2)
        assert(movement.endTile.yCoordinate == -2)
        assert(movement.endTile.connections== tile1.connections)
        assert(movement.endTile.rotationOffset == 0)
        assert (!movement.didCollide)

    }

    /**
     * Tests Undo behavior for a general turn .
     */
    @Test
    fun testUndoTurnPath() {

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        val tile = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5, 3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )

        val tile1 = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5, 3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf()
        )

        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(1, -1)

        game.playerList[game.activePlayerID].playHand[0] = tile1
        playerService.placeTile(2, -2)

        val turn = game.undoStack.last()

        playerService.undo()
        assert(turn.placedTile.rotationOffset == tile.rotationOffset)
        assert(turn.placedTile.type == tile.type)
        assert(turn.placedTile.gemPositions == tile.gemPositions)
        assert(turn.placedTile.connections == tile.connections)
        assert(turn.playerID == 1)
    }

    /**
     * Tests Undo behavior for gem movement if the startTile is centerTile.
     */
    @Test
    fun testUndoGemMovementCenterTile(){

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)

        var centerTile = CenterTile(mapOf(),0,0,0, ArrayDeque())
        for (row in game.gameLayout)
        {
            for (tile in row){
                if (tile is CenterTile)
                {
                    centerTile = tile
                }
            }
        }
        val tile = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf(GemType.NONE, GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE)
        )


        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(1,-1)


        val turn = game.undoStack.last()
        val movement = turn.gemMovements.last()

        playerService.undo()


        assert(movement.positionOnStartTile == 0)
        assert(movement.positionOnEndTile == 0)
        assert(movement.gemType == GemType.EMERALD)
        assert(movement.startTile.xCoordinate == 0)
        assert(movement.startTile.yCoordinate == 0)
        assert(movement.startTile.rotationOffset == 0)
        assert(movement.startTile.connections == centerTile.connections)
        assert(movement.endTile.xCoordinate == 1)
        assert(movement.endTile.yCoordinate == -1)
        assert(movement.endTile.connections== tile.connections)
        assert(movement.endTile.rotationOffset == 0)
        assert (!movement.didCollide)

    }

    /**
     * Tests Undo behavior for gem movement if the startTile is TreasureTile.
     */
    @Test
    fun testUndoGemMovementTreasureTile(){

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        val tile = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf(GemType.NONE, GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE)
        )


        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(3,0)


        val turn = game.undoStack.last()
        val movement = turn.gemMovements.last()

        playerService.undo()


        assert(movement.positionOnStartTile == 4)
        assert(movement.positionOnEndTile == 4)
        assert(movement.gemType == GemType.AMBER)
        assert(movement.startTile.xCoordinate == 4)
        assert(movement.startTile.yCoordinate == 0)
        assert(movement.startTile.rotationOffset == 0)
        assert(movement.startTile.connections == mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)))
        assert(movement.endTile.xCoordinate == 3)
        assert(movement.endTile.yCoordinate == 0)
        assert(movement.endTile.connections== tile.connections)
        assert(movement.endTile.rotationOffset == 0)
        assert (!movement.didCollide)

    }

    /**
     * Tests Undo behavior for gem movement when a gem has not benn moved.
     */
    @Test
    fun testUndoGemMovement(){

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)

        val tile = PathTile(
            mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2), 0, 0, 0,
            mutableListOf(GemType.NONE, GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE,GemType.NONE)
        )

        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(2,0)

        assert(game.undoStack.last().gemMovements.isEmpty())

        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(1,0)

        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.placeTile(3,0)

        val movement = game.undoStack.last().gemMovements.last()

        assert(movement.didCollide)

    }

    /**
     * Tests Undo behavior for gem movement if the endTile is GateTile.
     */
    @Test
    fun testUndoGemMovementEndGateTile(){

        val players = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q2", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)


        val tile = PathTile(
            mapOf(0 to 2, 1 to 3, 4 to 5, 2 to 0, 3 to 1, 5 to 4), 0, 0, 0,
            mutableListOf()
        )
        val tile1 = PathTile(
            mapOf(0 to 3, 1 to 2, 4 to 5, 3 to 0, 2 to 1, 5 to 4), 0, 0, 0,
            mutableListOf()
        )


        game.playerList[game.activePlayerID].playHand[0] = tile
        playerService.rotateTile()
        playerService.rotateTile()

        playerService.placeTile(3,0)

        game.playerList[game.activePlayerID].playHand[0] = tile1
        playerService.placeTile(4,-1)

        val turn = game.undoStack.last()
        val movement = turn.gemMovements.last()

        playerService.undo()


        assert(movement.positionOnStartTile == 0)
        assert(movement.positionOnEndTile == 3)
        assert(movement.gemType == GemType.AMBER)
        assert(movement.startTile.xCoordinate == 3)
        assert(movement.startTile.yCoordinate == 0)
        assert(movement.startTile.rotationOffset == 2)
        assert(movement.startTile.connections == tile.connections)
        assert(movement.endTile.xCoordinate == 5)
        assert(movement.endTile.yCoordinate == -2)
        assert(movement.endTile.connections== mapOf(0 to 3, 1 to 4, 2 to 5,3 to 0, 4 to 1, 5 to 2),)
        assert(movement.endTile.rotationOffset == 0)
        assert (!movement.didCollide)

    }
}
