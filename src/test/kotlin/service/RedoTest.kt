package service
import entity.*
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
/**
 * Testklasse für die Redo Methode der PlayerService-Klasse.
 */
class RedoTest {


    private lateinit var gameService: GameService
    private lateinit var rootService: RootService

    @BeforeEach
    fun setUp() {
        rootService = RootService()
        gameService = GameService(rootService)

    }

    /**
     * Testet das Verhalten des Redo, wenn kein aktives Spiel vorhanden ist.
     */
    @Test
    fun testRedoWhenNoActiveGame() {

        val exception = assertFailsWith<IllegalStateException> {
            rootService.playerService.redo()
        }
        assertEquals("no active game", exception.message)
    }
    /**
     * Testet das Verhalten des Redo, wenn der Redo-Stack leer ist.
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
     * Testet das allgemeine Wiederholen einer Aktion im Spiel.
     */
    @Test
    fun testRedo() {
        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)
        val game = rootService.currentGame

        val initialCoords = Pair(0, 0)
        rootService.playerService.placeTile(initialCoords.first, initialCoords.second)

        val rotationOffset = 2
        for (i in 0 until rotationOffset) {
            rootService.playerService.rotateTile()
        }

        rootService.playerService.redo()
        val expectedRotationOffset = rotationOffset
        val actualRotationOffset = game!!.playerList[game.activePlayerID].playHand.first().rotationOffset
        assertTrue(game.redoStack.isEmpty())
        assertEquals(expectedRotationOffset, actualRotationOffset)
    }




}
