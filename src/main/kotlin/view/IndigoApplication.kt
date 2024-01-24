package view

import edu.udo.cs.sopra.ntf.GameMode
import edu.udo.cs.sopra.ntf.PlayerColor
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

/**
 * Implementation of the Indigo game using BoardGameWork.
 */
@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class IndigoApplication: BoardGameApplication(windowTitle = "Indigo", windowMode = WindowMode.FULLSCREEN), Refreshable {

    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

     private val mainMenuScene = MainMenuScene(rootService)
     private val gameScene = GameScene(rootService)
     private val startGameScene = StartGameScene(rootService)
     private val hostGameScene = HostGameScene(rootService)
     private val joinGameScene = JoinGameScene(rootService)
     private val gameEndedScene = GameEndedScene(rootService)
     private val NETWORK_SECRET = "game23d"

    init {
        // all scenes and the application itself need to
        // react to changes done in the service layer
        rootService.addRefreshables(
            this,
            mainMenuScene,
            gameScene,
            startGameScene,
            hostGameScene,
            joinGameScene,
            gameEndedScene
        )

        // Bind buttons from Scenes
        mainMenuScene.quitButton.onMouseClicked = { exit() }
        mainMenuScene.newGameButton.onMouseClicked = { showMenuScene(startGameScene) }
        mainMenuScene.loadGameButton.onMouseClicked = {
            rootService.gameService.loadGame()
            //showMenuScene(loadGameScene)
        }
        mainMenuScene.hostGameButton.onMouseClicked = {
            // host game logic
            showMenuScene(hostGameScene)
        }
        mainMenuScene.joinGameButton.onMouseClicked = {
            // join game logic
            showMenuScene(joinGameScene)
        }
        mainMenuScene.hostGameButton.onMouseClicked = { hostGameLogic() }
        mainMenuScene.joinGameButton.onMouseClicked = { joinGameLogic() }
        hostGameScene.backButton.onMouseClicked = { showMenuScene(mainMenuScene)}
        joinGameScene.backButton.onMouseClicked = { showMenuScene(mainMenuScene)}
        gameEndedScene.quitButton.onMouseClicked = { exit() }
        gameEndedScene.newGameButton.onMouseClicked = { showMenuScene(startGameScene) }
        gameScene.quitGameButton.onMouseClicked = { exit() }
        startGameScene.backButton.onMouseClicked = { showMenuScene(mainMenuScene) }

        showGameScene(gameScene)
        showMenuScene(mainMenuScene, 0)
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
    }

    override fun refreshAfterEndGame() {
        this.showMenuScene(gameEndedScene)
    }

    private fun hostGameLogic() {
        val gameMode = when (mainMenuScene.selectedGameMode) {
            0 -> GameMode.TWO_NOT_SHARED_GATEWAYS
            1 -> GameMode.THREE_NOT_SHARED_GATEWAYS
            2 -> GameMode.THREE_SHARED_GATEWAYS
            3 -> GameMode.FOUR_SHARED_GATEWAYS
            else -> { GameMode.TWO_NOT_SHARED_GATEWAYS }
        }
        val sessionID = mainMenuScene.sessionIDInput.text
        val hostname = mainMenuScene.nameInput.text
        rootService.networkService.hostGame(NETWORK_SECRET, sessionID,
            hostname, PlayerColor.WHITE, gameMode)
        hostGameScene.hostName = hostname
        hostGameScene.resetAllComponents()
        showMenuScene(hostGameScene)
    }

    private fun joinGameLogic() {
        val sessionID = mainMenuScene.sessionIDInput.text
        val name = mainMenuScene.nameInput.text
        val playerType = when (mainMenuScene.selectedPlayerType) {
            0 -> PlayerType.LOCALPLAYER
            1 -> PlayerType.RANDOMAI
            2 -> PlayerType.SMARTAI
            else -> PlayerType.LOCALPLAYER
        }
        rootService.networkService.joinGame(NETWORK_SECRET, sessionID, name, playerType)
        showMenuScene(joinGameScene)
    }
}

