package view

import entity.Player
import entity.PlayerType
import view.Constants
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
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

    private var sessionID = ""
    private var hostName = ""

    private val modeImageList = listOf(
        ImageVisual(Constants.modeIconPlayer),
        ImageVisual(Constants.modeIconRandom),
        ImageVisual(Constants.modeIconAI)
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
                onKeyTyped = {
                    setStartButtonState()
                }
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

    private val playerRemoveIcon = Pane<Button>(
        posX = halfWidth + 275 + offsetX, posY = 1*150 + offsetY,
        width = 75, height = 75,
        visual = Visual.EMPTY
    ).apply {
        val playerRemoveBackground = Button(
            width = 75, height = 75,
            posX = 0, posY = 0,
            visual = Visual.EMPTY
        ).apply {
            componentStyle = "-fx-background-color: #ff999900; -fx-background-radius: 25px;"
            onMouseClicked = { handleRemovePlayerClick() }
        }

        val playerRemoveButton = Button(
            width = 60, height = 60,
            posX = 8, posY = 8,
            visual = ImageVisual(Constants.minusIcon)
        ).apply {
            isDisabled = true
        }

        this.add(playerRemoveBackground)
        this.add(playerRemoveButton)
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

    private val addDummyDataButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 730, posY = 900,
        text = "dummy data",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }


    init {
        background = Constants.sceneBackgroundColorVisual

        resetAllComponents()

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
            playerRemoveIcon,
            backButton,
            startButton,
            randomOrderCheckbox,
            threePlayerVariantCheckBox,
            addDummyDataButton
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

        for(i in 0 until 4) {
            val validLength = isPlayerNameLengthValid(i)
            if(!validLength) {
                startButton.isDisabled = true
                return
            }
        }

        startButton.isDisabled = false
    }

    private fun isPlayerNameLengthValid(index: Int): Boolean {
        if(!playerNameInputList[index].isVisible) return true
        val name = playerNameInputList[index].text
        return (name.length in 3..16)
    }

    private fun handleRemovePlayerClick() {
        var indexToBeDeleted = 2
        if(playerNameInputList[3].isVisible) indexToBeDeleted = 3

        playerNameInputList[indexToBeDeleted].isVisible = false
        playerColorIconList[indexToBeDeleted].isVisible = false
        playerModeIconList[indexToBeDeleted].isVisible = false


        if(indexToBeDeleted == 2) playerRemoveIcon.posY = 2000.0
        else playerRemoveIcon.posY = 2*150.0 + offsetY

        playerNameInputList[indexToBeDeleted].text = "Player ${indexToBeDeleted + 1}"

        if(indexToBeDeleted == 2) threePlayerVariantCheckBox.isVisible = false
        else threePlayerVariantCheckBox.isVisible = true

        setStartButtonState()
    }

    private fun handleStartClick() {
        val playerList = mutableListOf<Player>()
        for(i in 0 until 4) {
            if(!playerNameInputList[i].isVisible) continue

            val player = Player(
                name = playerNameInputList[i].text,
                color = selectedColors[i],
                playerType = PlayerType.values()[selectedModes[i]]
            )
            playerList.add(player)
        }

        if(randomOrderCheckbox.isChecked) playerList.shuffle()

        rootService.gameService.startNewGame(
            players = playerList,
            threePlayerVariant = threePlayerVariantCheckBox.isChecked && playerList.size == 3,
            simulationSpeed = 10.0,
            isNetworkGame = false
        )

        resetAllComponents()
    }

    fun setUp(sessionID: String, name: String) {
        this.sessionID = sessionID
        this.hostName = name
    }
    private fun resetAllComponents() {

        for(i in 0 until 4) {
            selectedColors[i] = i
            selectedModes[i] = if (i == 0) 0 else 3
        }

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

            playerNameInputList[i].text = "Player ${i + 1}"
            playerModeIconList[i].components[1].visual = ImageVisual(Constants.modeIconNetwork)
            playerColorIconList[i].components[1].visual = colorImageList[selectedColors[i]]
        }



        playerRemoveIcon.isVisible = false
        playerRemoveIcon.posY = 150.0 + offsetY

        randomOrderCheckbox.isChecked = false
        threePlayerVariantCheckBox.isChecked = false
        threePlayerVariantCheckBox.isVisible = false

        startButton.isDisabled = true
    }
}