package view

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

class IndigoApplication: BoardGameApplication("Indigo", 1920, 1080, WindowMode.FULLSCREEN), Refreshable {

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

