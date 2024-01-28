package view

import edu.udo.cs.sopra.ntf.GameMode
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

/**
 * Implementation of the Indigo game using BoardGameWork.
 */
class IndigoApplication: BoardGameApplication(windowTitle = "Indigo", windowMode = WindowMode.FULLSCREEN), Refreshable {

    // Central service from which all others are created/accessed
    // also holds the currently active game
    val rootService = RootService()

     private val mainMenuScene = MainMenuScene(rootService)
     private val gameScene = GameScene(rootService)
     private val startGameScene = StartGameScene(rootService)
     private val hostGameScene = HostGameScene(rootService)
     private val joinGameScene = JoinGameScene(rootService)
     private val gameEndedScene = GameEndedScene(rootService)

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
        mainMenuScene.hostGameButton.onMouseClicked = { hostGameLogic() }
        mainMenuScene.joinGameButton.onMouseClicked = { joinGameLogic() }
        hostGameScene.backButton.onMouseClicked = {
            rootService.networkService.disconnect()
            showMenuScene(mainMenuScene)
        }
        joinGameScene.backButton.onMouseClicked = {
            rootService.networkService.disconnect()
            showMenuScene(mainMenuScene)
        }
        gameEndedScene.quitButton.onMouseClicked = { exit() }
        gameEndedScene.newGameButton.onMouseClicked = {
            rootService.networkService.disconnect()
            showMenuScene(mainMenuScene)
        }
        gameScene.quitGameButton.onMouseClicked = { exit() }
        gameScene.returnToMenuButton.onMouseClicked = {
            rootService.networkService.disconnect()
            showMenuScene(mainMenuScene)
        }
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
        rootService.networkService.hostGame(sessionID, hostname, gameMode)
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
        rootService.networkService.joinGame(sessionID, name, playerType)
        showMenuScene(joinGameScene)
    }
}

