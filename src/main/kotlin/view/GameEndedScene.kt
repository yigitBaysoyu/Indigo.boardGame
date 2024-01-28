package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * Is displayed after a game has ended. Shows scores and winner of the game.
 */
class GameEndedScene(private val rootService: RootService) : MenuScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT),
    Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2
    private val listOffset = halfWidth - 75
    private val offsetY = 250
    private val offsetX = 50
    private val gemSize = 25

    private val colorImageList = mutableListOf(
        ImageVisual(Constants.whiteGate),
        ImageVisual(Constants.redGate),
        ImageVisual(Constants.blueGate),
        ImageVisual(Constants.purpleGate)
    )

    private val modeImageList = listOf(
        ImageVisual(Constants.modeIconPlayer),
        ImageVisual(Constants.modeIconRandom),
        ImageVisual(Constants.modeIconAI),
        ImageVisual(Constants.modeIconNetwork)
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
        text = "Game Over",
        font = Font(size = 75, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerNameInputList = mutableListOf<Label>().apply {
        for(i in 0 until 4) {
            val playerNameInput: Label = Label(
                width = 400, height = 75,
                posX = listOffset - 500 / 2 + offsetX, posY = 150*i + offsetY,
                font = Font(size = 35, Color(0, 0, 0)),
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"

                text = "Player ${i + 1}"
            }
            add(playerNameInput)
        }
    }

    private val playerPosInputList = mutableListOf<Label>().apply {
        for(i in 0 until 4) {
            val playerPosInput: Label = Label(
                width = 75, height = 75,
                posX = listOffset - 500 / 2 + offsetX, posY = 150*i + offsetY,
                font = Font(size = 35, Color(0, 0, 0)),
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
            }
            add(playerPosInput)
        }
    }

    private val playerPointsInputList = mutableListOf<Label>().apply {
        for(i in 0 until 4) {
            val playerPosInput: Label = Label(
                width = 75, height = 75,
                posX = listOffset - 500 / 2 + offsetX + 510, posY = 150*i + offsetY,
                font = Font(size = 35, Color(0, 0, 0)),
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
            }
            add(playerPosInput)
        }
    }

    private val playerColorIconList = mutableListOf<Pane<Button>>().apply {

        for (i in 0 until 4) {
            val pane = Pane<Button>(
                posX = listOffset - 500 / 2 - offsetX, posY = 150*i + offsetY,
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
        }
    }

    private val playerModeIconList = mutableListOf<Pane<Button>>().apply {
        for (i in 0 until 4) {
            val pane = Pane<Button>(
                posX = listOffset - 500 / 2 - offsetX - 100, posY = 150*i + offsetY,
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

    private val playerGemLayoutListList = mutableListOf<MutableList<Button>>().apply {
        for (i in 0 until 4) {
            val gems= mutableListOf<Button>()
            for(j in 0 until 12) {
                val gem = Button(
                    posX = listOffset - 500 / 2 + offsetX + 600 + j*25, posY = 150*i + offsetY +25,
                    width = gemSize, height = gemSize,
                    visual = Visual.EMPTY
                )
                gems.add(gem)
            }
            add(gems)
        }
    }

    private val playerWonIcon = Label(
        width = 60, height = 60,
        posX = listOffset - 500 / 2 + offsetX + 425, posY = offsetY + 5,
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    val newGameButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 375, posY = halfWidth - 50,
        text = "New Game",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    val quitButton = Button(
        width = 350, height = 75,
        posX = halfWidth + 25, posY = halfWidth - 50,
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
            headerLabel,
            playerNameInputList[0],
            playerNameInputList[1],
            playerNameInputList[2],
            playerNameInputList[3],
            playerPosInputList[0],
            playerPosInputList[1],
            playerPosInputList[2],
            playerPosInputList[3],
            playerPointsInputList[0],
            playerPointsInputList[1],
            playerPointsInputList[2],
            playerPointsInputList[3],
            playerColorIconList[0],
            playerColorIconList[1],
            playerColorIconList[2],
            playerColorIconList[3],
            playerWonIcon,
            quitButton,
            newGameButton,
            playerModeIconList[0],
            playerModeIconList[1],
            playerModeIconList[2],
            playerModeIconList[3],
        )
        playerGemLayoutListList.forEach {
            for(button in it) {
                addComponents(button)
            }
        }
    }

    override fun refreshAfterEndGame() {
        resetAllComponents()

        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        val players = game.playerList

        renderCollectedGemsLists()

        //sort players with score / amount of gems
        players.sortWith(compareByDescending<Player> { it.score }.thenByDescending { it.amountOfGems })


        players.forEachIndexed { i, player ->
            playerNameInputList[i].text = player.name
            playerNameInputList[i].isVisible = true
            playerPosInputList[i].text = "${i + 1}"
            playerPosInputList[i].isVisible = true
            playerPointsInputList[i].text = "${player.score}"
            playerPointsInputList[i].isVisible = true
            playerColorIconList[i].visual = colorImageList[player.color]
            playerColorIconList[i].isVisible = true

            val playerTypeAsInt = when(player.playerType) {
                PlayerType.LOCALPLAYER -> 0
                PlayerType.RANDOMAI -> 1
                PlayerType.SMARTAI -> 2
                PlayerType.NETWORKPLAYER -> 3
            }
            playerModeIconList[i].visual = modeImageList[playerTypeAsInt]
            playerModeIconList[i].isVisible = true
        }
    }

    private fun resetAllComponents(){
        for(i in 0 until 4) {
            playerColorIconList[i].isVisible = false
            playerNameInputList[i].isVisible = false
            playerNameInputList[i].text = ""
            playerPosInputList[i].isVisible = false
            playerPosInputList[i].text = "1"
            playerPointsInputList[i].isVisible = false
            playerPointsInputList[i].text = "0"
            playerColorIconList[i].isVisible = false
            playerModeIconList[i].isVisible = false
            for(j in 0 until 12) {
                playerGemLayoutListList[i][j].visual = Visual.EMPTY
                playerGemLayoutListList[i][j].isVisible = false
            }
        }

        playerWonIcon.visual = ImageVisual(Constants.wonIcon)
    }

    private fun renderCollectedGemsLists() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        game.playerList.forEachIndexed {index, player ->
            renderCollectedGemsForPlayer(player, index)
        }
    }

    private fun renderCollectedGemsForPlayer(player: Player, playerIndex: Int) {
        val playersGemList = mutableListOf<GemType>()

        for(gate in player.gateList) {
            playersGemList.addAll(gate.gemsCollected)
        }

        for((i, gem) in playersGemList.withIndex()) {
            val gemVisual = when(gem) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }

            playerGemLayoutListList[playerIndex][i].visual = gemVisual
            playerGemLayoutListList[playerIndex][i].isVisible = true
        }
    }
}