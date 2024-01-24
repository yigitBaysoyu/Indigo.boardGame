package view

import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * Displays the Main Menu of the Indigo Game
 */
class MainMenuScene(private val rootService: RootService) : MenuScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2
    private val offsetY = -90 // Used to Position all Elements vertically

    private val cornersBackground = Button(
        posX = 0, posY = 0,
        width = sceneWidth, height = sceneHeight,
        visual = ImageVisual(Constants.cornersBackground)
    ).apply {
        isDisabled = true
    }

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

    val sessionIDInput: TextField = TextField(
        width = 350, height = 50,
        posX = halfWidth - 350 / 2, posY = offsetY + 640,
        prompt = "Session ID ...",
        font = Font(size = 35, Color(0, 0, 0)),
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

    val nameInput: TextField = TextField(
        width = 350, height = 50,
        posX = halfWidth - 350 / 2, posY = offsetY + 725,
        font = Font(size = 35, Color(0, 0, 0)),
        visual = Visual.EMPTY,
        prompt = "Name ..."
    ).apply {
        onKeyTyped = {
            val name = text

            if(name.length in 3..16) {
                hostGameButton.isDisabled = false
                joinGameButton.isDisabled = false
            } else {
                hostGameButton.isDisabled = true
                joinGameButton.isDisabled = true
            }
        }

        componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
    }

    val hostGameButton = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth - 350 / 2 + 5, posY = offsetY + 815,
        text = "Host",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY,
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
        isDisabled = true
    }

    // Is rendered behind hostGameButton to give it more opacity when it is disabled
    private val hostGameButtonBackground = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth - 350 / 2 + 5, posY = offsetY + 815,
        text = "Host",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240, 120)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f50; -fx-background-radius: 25px;"
    }

    // 0 = TWO_NOT_SHARED_GATEWAYS, 1 = THREE_SHARED_GATEWAYS, 2 = THREE_NOT_SHARED_GATEWAYS
    // 3 = FOUR_SHARED_GATEWAYS
    var selectedGameMode = 0
    private val gameModeImageList = listOf(
        ImageVisual(Constants.gameModeIcon2),
        ImageVisual(Constants.gameModeIcon3NotShared),
        ImageVisual(Constants.gameModeIcon3Shared),
        ImageVisual(Constants.gameModeIcon4),
    )
    private val gameModeIconList = mutableListOf<Pane<Button>>().apply {
        val pane = Pane<Button>(
            posX = halfWidth - 350/2 - 75, posY = offsetY + 815 + 2,
            width = 70, height = 70,
            visual = Visual.EMPTY
        )

        val gameModeIconBackground = Button(
            width = 70, height = 70,
            posX = 0, posY = 0,
            visual = Visual.EMPTY
        ).apply {
            componentStyle = "-fx-background-color: #ffffffff; -fx-background-radius: 25px;"
        }

        val gameModeIcon = Button(
            width = 55, height = 55,
            posX = 7, posY = 7,
            visual = ImageVisual(Constants.gameModeIcon2)
        ).apply {
            isDisabled = true
        }
        pane.add(gameModeIconBackground)
        pane.add(gameModeIcon)
        add(pane)

        gameModeIconBackground.onMouseClicked = {
            selectedGameMode = (selectedGameMode + 1) % 4
            gameModeIcon.visual = gameModeImageList[selectedGameMode]
        }
    }

    // 0 = LOCAL_PLAYER, 1 = RANDOM_AI, 2 = SMART_AI
    var selectedPlayerType = 0
    private val playerTypeImageList = listOf(
        ImageVisual(Constants.modeIconPlayer),
        ImageVisual(Constants.modeIconAI),
        ImageVisual(Constants.modeIconRandom),
    )
    private val playerModeIconList = mutableListOf<Pane<Button>>().apply {
        val pane = Pane<Button>(
            posX = halfWidth + 350/2 + 5, posY = offsetY + 815 + 2,
            width = 70, height = 70,
            visual = Visual.EMPTY
        )

        val playerModeIconBackground = Button(
            width = 70, height = 70,
            posX = 0, posY = 0,
            visual = Visual.EMPTY
        ).apply {
            componentStyle = "-fx-background-color: #ffffffff; -fx-background-radius: 25px;"
        }

        val playerModeIcon = Button(
            width = 55, height = 55,
            posX = 7, posY = 7,
            visual = ImageVisual(Constants.modeIconPlayer)
        ).apply {
            isDisabled = true
        }
        pane.add(playerModeIconBackground)
        pane.add(playerModeIcon)
        add(pane)

        playerModeIconBackground.onMouseClicked = {
            selectedPlayerType = (selectedPlayerType + 1) % 3
            playerModeIcon.visual = playerTypeImageList[selectedPlayerType]
        }
    }

    val joinGameButton = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth + 5, posY = offsetY + 815,
        text = "Join",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
        isDisabled = true
    }

    // Is rendered behind joinGameButton to give it more opacity when it is disabled
    private val joinGameButtonBackground = Button(
        width = 350 / 2 - 10, height = 50,
        posX = halfWidth + 5, posY = offsetY + 815,
        text = "Join",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240, 120)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f50; -fx-background-radius: 25px;"
    }

    val quitButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2, posY = offsetY + 975,
        text = "Quit",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    init {
        background = Constants.sceneBackgroundColorVisual

        addComponents(
            cornersBackground,
            localLabel,
            newGameButton,
            loadGameButton,
            onlineLabel,
            sessionIDInput,
            nameInput,
            hostGameButtonBackground,
            hostGameButton,
            joinGameButtonBackground,
            joinGameButton,
            quitButton,
            playerModeIconList[0],
            gameModeIconList[0]
        )
    }
}