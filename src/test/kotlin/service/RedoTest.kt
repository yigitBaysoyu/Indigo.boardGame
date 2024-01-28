package service
import entity.*
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*


/**
 * Test class for the Redo method of the PlayerService class.
 */
class RedoTest {


    private lateinit var gameService: GameService
    private lateinit var rootService: RootService

    /**
     * setUp prepares the required services.
     */
    @BeforeEach
    fun setUp() {
        rootService = RootService()
        gameService = rootService.gameService

    }

    /**
     * Tests the behavior of Redo when there is no active game.
     */
    @Test
    fun testRedoWhenNoActiveGame() {

        val exception = assertFailsWith<IllegalStateException> {
            rootService.playerService.redo()
        }
        assertEquals("no active game", exception.message)
    }

    /**
     * Tests the behavior of Redo when the Redo stack is empty.
     */
    @Test
    fun testRedoWithEmptyRedoStack() {


        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)
        assertTrue(rootService.currentGame!!.redoStack.isEmpty())
        rootService.playerService.redo()
        assertTrue(rootService.currentGame!!.redoStack.isEmpty())
    }

    /**
     * Tests if the redoStack is empty after calling the method.
     */
    @Test
    fun testRedo() {
        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)
        val game = rootService.currentGame
        checkNotNull(game)

        val initialCoords = Pair(1, 1)
        rootService.playerService.placeTile(initialCoords.first, initialCoords.second)

        val rotationOffset = 2
        repeat(2) {
            rootService.playerService.rotateTile()
        }
        rootService.playerService.undo() // Or game!!.redoStack.addAll(listOf(Pair(testCoordinates, testRotationOffset)
        rootService.playerService.redo()


        val actualRotationOffset = game.playerList[game.activePlayerID].playHand.first().rotationOffset
        assertTrue(game.redoStack.isEmpty()) // hier ist redoStack wieder leer
        assertEquals(rotationOffset, actualRotationOffset)
    }

}
