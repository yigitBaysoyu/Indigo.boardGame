package service
import entity.*
import kotlin.test.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Testklasse für die setSimulationSpeed Methode der GameService-Klasse.
 */
class SetSimulationSpeedTest {


    /**
     * Testet die setSimulationSpeed-Methode mit einer gültigen Geschwindigkeit.
     */
    @Test
    fun setSimulationSpeedValid() {


            val rootService = RootService()
            val gameService = rootService.gameService

            val players = mutableListOf(Player("A"), Player("B"), Player("C"))
            gameService.startNewGame(players, false, 50.0, false)

            val game = rootService.currentGame
            checkNotNull(game)
           gameService.setSimulationSpeed(50.0)

           assertEquals(50.0, game.simulationSpeed)

    }

    /**
     * Testet die setSimulationSpeed Methode mit einer gültigen Geschwindigkeit, die kleiner als 1.0 ist.
     * In diesem Fall sollte die SimulationSpeed auf den Mindestwert 1.0 gesetzt werden.
     */
    @Test
    fun setSimulationSpeedValidNotOne() {


        val rootService = RootService()
        val gameService = rootService.gameService

        val players = mutableListOf(Player("A"), Player("B"), Player("C"))
        gameService.startNewGame(players, false, 1.0, false)

        val game = rootService.currentGame
        checkNotNull(game)
        gameService.setSimulationSpeed(0.5)

        assertEquals(1.0, game.simulationSpeed)

    }
    /**
     * Testet die setSimulationSpeed-Methode mit einer gültigen Geschwindigkeit, die größer als 100.0 ist.
     * In diesem Fall sollte die SimulationSpeed auf den Höchstwert 100.0 gesetzt werden.
     */
    @Test
    fun setSimulationSpeedValidOverMax() {

        val rootService = RootService()
        val gameService = rootService.gameService

        val players = mutableListOf(Player("A"), Player("B"), Player("C"))
        gameService.startNewGame(players, false, 100.0, false)
        val game = rootService.currentGame
        checkNotNull(game)
        gameService.setSimulationSpeed(150.5)
        assertEquals(100.0, game.simulationSpeed)
    }

    /**
     * Testet die setSimulationSpeed-Methode, wenn currentGame null ist.
     * In diesem Fall sollte eine IllegalStateException ausgelöst werden.
     */

    @Test
    fun setSimulationSpeedNulCurrentGame() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        rootService.currentGame = null
        assertFailsWith<IllegalStateException> {
            gameService.setSimulationSpeed(50.0)
        }
    }
}


