package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

/**
 * Is displayed after a game has ended. Shows scores and winner of the game.
 */
class GameEndedScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2
    private val offsetY = -50

    init {
        background = ColorVisual(44, 70, 127)

        addComponents(

        )
    }
}
