package service
import entity.*
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertTrue
/**
 * Testklasse für die Undo Methode der PlayerService-Klasse.
 */
class UndoTest {


    private lateinit var gameService: GameService
    private lateinit var rootService: RootService

    @BeforeEach
    fun setUp() {
        rootService = RootService()
        gameService = rootService.gameService

    }
    /**
     * Testet das Verhalten des Undo, wenn der Undo-Stack leer ist.
     */
    @Test
    fun testUndoEmptyStack() {

        val players = mutableListOf(Player("A"), Player("B"))
        gameService.startNewGame(players, false, 1.0, false)
        val game = rootService.currentGame
        assertTrue(game!!.undoStack.isEmpty())
        rootService.playerService.undo()
        assertTrue(game!!.undoStack.isEmpty())
    }

   //  Sie Können noch einige zusätzliche Tests hinzufügen

}
