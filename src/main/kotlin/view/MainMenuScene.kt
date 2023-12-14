package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Displays the Main Menu of the Indigo Game
 */
class MainMenuScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2
    private val offsetY = -50;

    private val localLabel = Label(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 200,
        text = "Local",
        font = Font(size = 65, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
    )

    val newGameButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 300,
        text = "New Game",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    val loadGameButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 400,
        text = "Load Game",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    private val onlineLabel = Label(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 550,
        text = "Online",
        font = Font(size = 60, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
    )

    private val sessionIDInput: TextField = TextField(
        width = 350, height = 50,
        posX = halfWidth - 350 / 2, posY = offsetY + 640,
        prompt = "Session ID ...",
        text = "Session ID ...",
        font = Font(size = 35, color = Color(0, 0, 0, 100)),
        visual = Visual.EMPTY
    ).apply {
        onKeyTyped = {
            val sessionID = text

            if(sessionID == "") {
                hostGameButton.isDisabled = true
                joinGameButton.isDisabled = true
            } else {
                hostGameButton.isDisabled = false
                joinGameButton.isDisabled = false
            }
        }

        componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
    }

    val hostGameButton = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth - 350 / 2 + 5, posY = offsetY + 725,
        text = "Host",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
        isDisabled = true
    }

    val hostGameButtonBackground = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth - 350 / 2 + 5, posY = offsetY + 725,
        text = "Host",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240, 120)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f50; -fx-background-radius: 25px;"
    }

    val joinGameButton = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth + 5, posY = offsetY + 725,
        text = "Join",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
        isDisabled = true
    }

    val joinGameButtonBackground = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth + 5, posY = offsetY + 725,
        text = "Join",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240, 120)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f50; -fx-background-radius: 25px;"
    }

    val quitButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 900,
        text = "Quit",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    init {
        background = ColorVisual(44, 70, 127)

        addComponents(
            localLabel,
            newGameButton,
            loadGameButton,
            onlineLabel,
            sessionIDInput,
            hostGameButtonBackground,
            hostGameButton,
            joinGameButtonBackground,
            joinGameButton,
            quitButton
        )
    }
}