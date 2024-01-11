package service


import entity.*
import java.io.File
import kotlin.test.*

/**
 * This class contains test cases for the [GameService.saveGame] and [GameService.loadGame]
 * functions.
 * */
class SaveGameTest {
    private lateinit var gameService: GameService
    private lateinit var playerService: PlayerService
    private lateinit var rootService: RootService

    /**
     *
     * Sets up the necessary services and initializes them before each test case.
     **/
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        playerService = PlayerService(rootService)
        gameService = GameService(rootService)
    }

    @AfterTest
    fun tearDown() {
        File("saveGame.ser").delete()
    }

    @Test
    fun saveTest() {
        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(player, false, 1.0, false)
        gameService.saveGame()
        val file = File("saveGame.ser")
        assert(file.exists())
        assert(file.readText().isNotEmpty())
    }
}
