package view

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * Displays configuration for an online game.
 */
class HostGameScene(private val rootService: RootService) : MenuScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private  val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2
    private val offsetY = 250
    private val offsetX = 50

    // 0 = Local, 1 = RandomAI, 2 = SmartAI, 3 = NetworkPlayer
    private val selectedModes = mutableListOf(0, 0, 0, 0)
    private val selectedColors = mutableListOf(0, 1, 2, 3)

    var hostName = ""
    private var lobbyFull = false

    private val modeImageList = listOf(
        ImageVisual(Constants.modeIconPlayer),
        ImageVisual(Constants.modeIconRandom),
        ImageVisual(Constants.modeIconAI),
    )

    private val colorImageList = mutableListOf(
        ImageVisual(Constants.whiteGate),
        ImageVisual(Constants.redGate),
        ImageVisual(Constants.blueGate),
        ImageVisual(Constants.purpleGate)
    )

    private val cornersBackground = Button(
        posX = 0, posY = 0,
        width = sceneWidth, height = sceneHeight,
        visual = ImageVisual(Constants.cornersBackground)
    ).apply {
        isDisabled = true
    }

    private val sessionIDLabel = Label(
        width = 750, height = 100,
        posX = halfWidth - 750/2, posY = 800,
        text = "Session ID: ",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val headerLabel = Label(
        width = 500, height = 100,
        posX = halfWidth - 500 / 2, posY = 75,
        text = "Online Game",
        font = Font(size = 65, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerNameInputList = mutableListOf<Label>().apply {
        for(i in 0 until 4) {
            val playerNameInput: Label = Label(
                width = 500, height = 75,
                posX = halfWidth - 500 / 2 + offsetX, posY = 150*i + offsetY,
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
                visual = ImageVisual(Constants.modeIconNetwork)
            ).apply {
                isDisabled = true
            }

            pane.add(playerModeIconBackground)
            pane.add(playerModeIcon)
            add(pane)

            playerModeIconBackground.onMouseClicked = {
                // The host should only change its own PlayerMode
                if(i == 0) {
                    selectedModes[i] = (selectedModes[i] + 1) % 3
                    playerModeIcon.visual = modeImageList[selectedModes[i]]
                }
            }
        }
    }

    private val playerColorIconList = mutableListOf<Pane<Button>>().apply {

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
                componentStyle = "-fx-background-color: #ffffff; -fx-background-radius: 25px;"
            }

            val playerColorIcon = Button(
                width = 60, height = 60,
                posX = 8, posY = 8,
                visual = colorImageList[i]
            ).apply {
                isDisabled = true
            }
            pane.add(playerColorIconBackground)
            pane.add(playerColorIcon)
            add(pane)

            playerColorIconBackground.onMouseClicked = {
                selectedColors[i] = (selectedColors[i] + 1) % 4
                playerColorIcon.visual = colorImageList[selectedColors[i]]
                setStartButtonState()
            }
        }
    }

    private val startButton = Button(
        width = 350, height = 75,
        posX = halfWidth + 15, posY = 900,
        text = "Start",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { handleStartClick() }
    }

    val backButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 - 15, posY = 900,
        text = "Back",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val randomOrderCheckbox = CheckBox(
        posX = halfWidth + 275 + offsetX + 150, posY = offsetY,
        width = 300, height = 75,
        text = "Randomize Player Order",
        alignment = Alignment.CENTER_LEFT,
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    ).apply {
        isIndeterminateAllowed = false
    }

    private val threePlayerVariantCheckBox = CheckBox(
        posX = halfWidth + 275 + offsetX + 150, posY = 2*150 + offsetY,
        width = 300, height = 75,
        text = "Share Gates",
        alignment = Alignment.CENTER_LEFT,
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    ).apply {
        isIndeterminateAllowed = false
        isVisible = false
    }

    init {
        background = Constants.sceneBackgroundColorVisual

        addComponents(
            cornersBackground,
            headerLabel,
            sessionIDLabel,
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
            backButton,
            startButton,
            randomOrderCheckbox,
            threePlayerVariantCheckBox,
        )
    }
    private fun checkForSamePlayerColors(): Boolean {
        for(i in 0 until 4) {
            playerColorIconList[i].components[0].componentStyle = "-fx-background-color: #ffffff; -fx-background-radius: 25px;"
        }

        val copiedSelectColors = mutableListOf<Int>()
        for(i in 0 until 4) {
            if(!playerNameInputList[i].isVisible) break
            copiedSelectColors.add(selectedColors[i])
        }

        var returnValue = false
        for(i in 0 until copiedSelectColors.size) {
            for(j in 0 until copiedSelectColors.size) {
                if(copiedSelectColors[i] == copiedSelectColors[j] && i != j) {
                    playerColorIconList[i].components[0].componentStyle = "-fx-background-color: #dd3344; -fx-background-radius: 25px;"
                    returnValue = true
                }
            }
        }
        return returnValue
    }

    private fun setStartButtonState() {
        val validColor = !checkForSamePlayerColors()
        if(!validColor) {
            startButton.isDisabled = true
            return
        }
        if(lobbyFull) startButton.isDisabled = false
    }

    private fun handleStartClick() {
        val selectedType = when(selectedModes[0]) {
            0 -> PlayerType.LOCALPLAYER
            1 -> PlayerType.RANDOMAI
            else -> PlayerType.SMARTAI
        }
        rootService.networkService.startNewHostedGame(selectedColors, selectedType, randomOrderCheckbox.isChecked)

        resetAllComponents()
    }

    override fun refreshAfterLastPlayerJoined(){
        lobbyFull = true
        setStartButtonState()
    }

    /**
     *  loads the Players Names, Colors and Player-modes
     */
    fun resetAllComponents() {
        lobbyFull = false

        for(i in 0 until 4) {
            selectedColors[i] = i
            selectedModes[i] = if (i == 0) 0 else 3
        }

        sessionIDLabel.text = "Session ID: "

        // host specific configuration
        playerNameInputList[0].isVisible = true
        playerNameInputList[0].text = hostName
        playerModeIconList[0].components[1].visual = ImageVisual(Constants.modeIconPlayer)
        playerColorIconList[0].components[1].visual = colorImageList[selectedColors[0]]

        // other network player
        for(i in 1 until 4) {
            playerNameInputList[i].isVisible = false
            playerModeIconList[i].isVisible = false
            playerColorIconList[i].isVisible = false

            playerNameInputList[i].text = ""
            playerModeIconList[i].components[1].visual = ImageVisual(Constants.modeIconNetwork)
            playerColorIconList[i].components[1].visual = colorImageList[selectedColors[i]]
        }


        randomOrderCheckbox.isChecked = false
        threePlayerVariantCheckBox.isChecked = false
        threePlayerVariantCheckBox.isVisible = false

        startButton.isDisabled = true
    }

    override fun refreshAfterPlayerJoined(name: String) {
        for(i in 1 until 4) {
            if(playerNameInputList[i].text == "") {
                playerNameInputList[i].text = name
                playerNameInputList[i].isVisible = true
                playerModeIconList[i].isVisible = true
                playerColorIconList[i].isVisible = true


                playerModeIconList[i].components[1].visual = ImageVisual(Constants.modeIconNetwork)
                playerColorIconList[i].components[1].visual = colorImageList[selectedColors[i]]
                return
            }
        }
        // game is already full

    }

    override fun refreshAfterPlayerLeft(name: String){

        val listIndex = playerNameInputList.indexOfFirst { label ->
            label.text == name
        }

        for(i in 1 until 4) {
            if(selectedColors[i] == listIndex) {
                playerNameInputList[i].isVisible = false
                playerModeIconList[i].isVisible = false
                playerColorIconList[i].isVisible = false
                playerNameInputList[i].text = ""

                if(i in 1..2) {

                    for(j in i ..2) {
                        if(playerNameInputList[j+1].text == "") return

                        // move player at index i+1 to index i
                        playerNameInputList[j].text = playerNameInputList[j+1].text

                        playerNameInputList[j].isVisible = true
                        playerModeIconList[j].isVisible = true
                        playerColorIconList[j].isVisible = true

                        playerNameInputList[j+1].isVisible = false
                        playerModeIconList[j+1].isVisible = false
                        playerColorIconList[j+1].isVisible = false
                        playerNameInputList[j+1].text = ""
                    }
                }
                return
            }
        }
        // game is already full
    }

    override fun refreshAfterSessionIDReceived(sessionID: String) {
        sessionIDLabel.text = "Session ID: $sessionID"
    }
}