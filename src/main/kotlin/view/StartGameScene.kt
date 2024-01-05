package view

import service.Constants
import service.RootService
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * Shows configuration screen for a local game.
 */
class StartGameScene(private val rootService: RootService) : MenuScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private  val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2
    private val offsetY = 250
    private val offsetX = 50

    private val cornersBackground = Button(
        posX = 0, posY = 0,
        width = sceneWidth, height = sceneHeight,
        visual = ImageVisual(Constants.cornersBackground)
    ).apply {
        isDisabled = true
    }

    private val headerLabel = Label(
        width = 500, height = 100,
        posX = halfWidth - 500 / 2, posY = 75,
        text = "Local Game",
        font = Font(size = 65, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerNameInputList = mutableListOf<TextField>().apply {
        for(i in 0 until 4) {
            val playerNameInput: TextField = TextField(
                width = 500, height = 75,
                posX = halfWidth - 500 / 2 + offsetX, posY = 150*i + offsetY,
                prompt = "name ...",
                font = Font(size = 35, Color(0, 0, 0)),
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
            }
            add(playerNameInput)
        }
    }

    private val playerModeIconList = mutableListOf<Pane<Button>>().apply {
        for (i in 0 until 4) {
            val pane = Pane<Button>(
                posX = halfWidth - 450 + offsetX, posY = i*150 + offsetY + 2,
                width = 75, height = 75,
                visual = Visual.EMPTY
            )

            val playerModeIconBackground = Button(
                width = 75, height = 75,
                posX = 0, posY = 0,
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #ffffffff; -fx-background-radius: 25px;"
            }

            val playerModeIcon = Button(
                width = 60, height = 60,
                posX = 7, posY = 7,
                visual = ImageVisual(Constants.modeIconPlayer)
            ).apply {
                isDisabled = true
            }
            pane.add(playerModeIconBackground)
            pane.add(playerModeIcon)
            add(pane)
        }
    }

    private val playerColorIconList = mutableListOf<Pane<Button>>().apply {
        val colorIconList = mutableListOf(
            ImageVisual(Constants.redGate),
            ImageVisual(Constants.blueGate),
            ImageVisual(Constants.purpleGate),
            ImageVisual(Constants.whiteGate)
        )
        for (i in 0 until 4) {
            val pane = Pane<Button>(
                posX = halfWidth - 350 + offsetX, posY = i*150 + offsetY + 2,
                width = 75, height = 75,
                visual = Visual.EMPTY
            )

            val playerColorIconBackground = Button(
                width = 75, height = 75,
                posX = 0, posY = 0,
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #ffffffff; -fx-background-radius: 25px;"
            }

            val playerColorIcon = Button(
                width = 60, height = 60,
                posX = 8, posY = 8,
                visual = colorIconList[i]
            ).apply {
                isDisabled = true
            }
            pane.add(playerColorIconBackground)
            pane.add(playerColorIcon)
            add(pane)
        }
    }

    private val playerRemoveIconList = mutableListOf<Pane<Button>>().apply {
        for (i in 0 until 4) {
            val pane = Pane<Button>(
                posX = halfWidth + 275 + offsetX, posY = i*150 + offsetY + 2,
                width = 75, height = 75,
                visual = Visual.EMPTY
            )

            val playerRemoveIconBackground = Button(
                width = 75, height = 75,
                posX = 0, posY = 0,
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #ff999900; -fx-background-radius: 25px;"
            }

            val playerRemoveIcon = Button(
                width = 60, height = 60,
                posX = 8, posY = 8,
                visual = ImageVisual(Constants.minusIcon)
            ).apply {
                isDisabled = true
            }
            pane.add(playerRemoveIconBackground)
            pane.add(playerRemoveIcon)
            add(pane)
        }
    }



    private val addPlayerButton = Button(
        width = 50, height = 50,
        posX = halfWidth - 50 / 2, posY = 2*150 + offsetY,
        text = "",
        visual = ImageVisual(Constants.plusIcon)
    ).apply {
        componentStyle = "-fx-background-radius: 18px;"
    }

    private val startButton = Button(
        width = 350, height = 75,
        posX = halfWidth + 15, posY = 900,
        text = "Start",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val backButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 - 15, posY = 900,
        text = "Back",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    init {
        background = Constants.sceneBackgroundColorVisual

        addComponents(
            cornersBackground,
            headerLabel,
            playerNameInputList[0],
            playerNameInputList[1],
            playerNameInputList[2],
            playerNameInputList[3],
            playerModeIconList[0],
            playerModeIconList[1],
            playerModeIconList[2],
            playerModeIconList[3],
            playerColorIconList[0],
            playerColorIconList[1],
            playerColorIconList[2],
            playerColorIconList[3],
            playerRemoveIconList[0],
            playerRemoveIconList[1],
            playerRemoveIconList[2],
            playerRemoveIconList[3],
            addPlayerButton,
            backButton,
            startButton
        )
    }
}