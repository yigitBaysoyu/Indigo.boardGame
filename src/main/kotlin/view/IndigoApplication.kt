package view

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

/**
 * Implementation of the Indigo game using BoardGameWork.
 */
class IndigoApplication: BoardGameApplication(windowTitle = "Indigo", windowMode = WindowMode.FULLSCREEN), Refreshable {

    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

     private val mainMenuScene = MainMenuScene(rootService)
     private val gameScene = GameScene(rootService)
     private val startGameScene = StartGameScene(rootService)
     private val loadGameScene = LoadGameScene(rootService)
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
            loadGameScene,
            hostGameScene,
            joinGameScene,
            gameEndedScene
        )

        // Bind buttons from MainMenuScene
        mainMenuScene.quitButton.onMouseClicked = { exit() }
        mainMenuScene.newGameButton.onMouseClicked = { showMenuScene(startGameScene) }
        mainMenuScene.loadGameButton.onMouseClicked = { showMenuScene(loadGameScene) }
        mainMenuScene.hostGameButton.onMouseClicked = {
            // host game logic
            showMenuScene(hostGameScene)
        }
        mainMenuScene.joinGameButton.onMouseClicked = {
            // join game logic
            showMenuScene(joinGameScene)
        }
        gameEndedScene.quitButton.onMouseClicked = { exit() }
        gameEndedScene.newGameButton.onMouseClicked = { showMenuScene(startGameScene) }
        gameScene.quitGameButton.onMouseClicked = { exit() }

        showGameScene(gameScene)
        showMenuScene(mainMenuScene, 0)
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
    }

    override fun refreshAfterEndGame() {
        this.showMenuScene(gameEndedScene)
    }
}

