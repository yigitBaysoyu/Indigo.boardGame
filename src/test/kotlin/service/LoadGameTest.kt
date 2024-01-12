package service


import entity.*
import kotlin.test.*
import java.io.File

class LoadGameTest {
    private lateinit var gameService: GameService
    private lateinit var playerService: PlayerService
    private lateinit var rootService: RootService

    /**
     * Sets up the necessary services and initializes them before each test case.     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        playerService = PlayerService(rootService)
        gameService = GameService(rootService)
    }
    /**
     * Cleans up after tests by deleting the saveGame.ser file.
     */
    @AfterTest
    fun tearDown() {
        File("saveGame.ser").delete()
    }
    /**
     * Tests the loading functionality of the game service.
     *.
     */
    @Test
    fun loadTest() {
        val player = mutableListOf(
            Player("q", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf()),
            Player("q1", 0, PlayerType.LOCALPLAYER, 0, 0, mutableListOf(), mutableListOf())
        )
        gameService.startNewGame(player, false, 1.0, false)
        val sampleState = rootService.currentGame
        checkNotNull(sampleState)
        gameService.saveGame()
        gameService.loadGame()
        val loadedGame = rootService.currentGame
        assertNotNull(loadedGame)
        assertTrue(areGameStatesEqual(sampleState, loadedGame))
    }
}
/**
 * Compares two IndigoGame states to determine if they are equal.
 * Checks various game state attributes such as active player ID, simulation speed, and network game status.
 *
 * @param state1 The first game state to compare.
 * @param state2 The second game state to compare.
 * @return True if the states are equal, False otherwise.
 */
fun areGameStatesEqual(state1: IndigoGame, state2: IndigoGame): Boolean {
    if (state1.activePlayerID != state2.activePlayerID) return false
    if (state1.simulationSpeed != state2.simulationSpeed) return false
    if (state1.isNetworkGame != state2.isNetworkGame) return false
    if (state1.undoStack != state2.undoStack) return false
    if (state1.redoStack != state2.redoStack) return false
    if (!arePlayerListsEqual(state1.playerList, state2.playerList)) return false
    return true
}
/**
 * Compares two lists of Player objects to determine if they are equal.
 * Checks if both lists contain the same players with the same attributes in the same order.
 *
 * @param list1 The first player list to compare.
 * @param list2 The second player list to compare.
 * @return True if the lists are equal, False otherwise.
 */
fun arePlayerListsEqual(list1: List<Player>, list2: List<Player>): Boolean {
    if (list1.size != list2.size) return false
    list1.zip(list2).forEach { (player1, player2) ->
        if (player1.name != player2.name || player1.score != player2.score || player1.playerType != player2.playerType || player1.color != player2.color) {
            return false
        }
    }
    return true
}
