package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

/**
 * Displays configuration for an online game.
 */
class HostGameScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2
    private val offsetY = -50

    init {
        background = ColorVisual(44, 70, 127)

        addComponents(

        )
    }
}
