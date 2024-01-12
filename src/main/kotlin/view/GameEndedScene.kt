package view

import entity.GemType
import entity.IndigoGame
import entity.Player
import entity.PlayerType
import service.Constants
import service.RootService
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Is displayed after a game has ended. Shows scores and winner of the game.
 */
class GameEndedScene(private val rootService: RootService) : MenuScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2
    private val offsetY = 250
    private val offsetX = 50
    private val gemSize = 25

    private val headerLabel = Label(
        width = 500, height = 100,
        posX = halfWidth - 500 / 2, posY = 75,
        text = "Game Over",
        font = Font(size = 75, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    val testButton = Button(
        width = 350, height = 75,
        posX = 100 - 185, posY = 200,
        text = "test",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"


    }

    private val playerNameInputList = mutableListOf<Label>().apply {
        for(i in 0 until 4) {
            val playerNameInput: Label = Label(
                width = 400, height = 75,
                posX = halfWidth - 500 / 2 + offsetX, posY = 150*i + offsetY,
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
                posX = halfWidth - 500 / 2 + offsetX, posY = 150*i + offsetY,
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
                posX = halfWidth - 500 / 2 + offsetX + 510, posY = 150*i + offsetY,
                font = Font(size = 35, Color(0, 0, 0)),
                visual = Visual.EMPTY
            ).apply {
                componentStyle = "-fx-background-color: #fafaf0; -fx-background-radius: 25px;"
            }
            add(playerPosInput)
        }
    }

    private val playerGemLayoutList = mutableListOf<LinearLayout<TokenView>>().apply {
        for(i in 0 until 4) {
            val playerGemLayout = LinearLayout<TokenView>(
                width = 400, height = 75,
                posX = halfWidth - 500 / 2 + offsetX + 560, posY = 150*i + offsetY,
                visual = Visual.EMPTY,
                alignment = Alignment.CENTER_LEFT
            )
            add(playerGemLayout)
        }
    }



    private val playerWonIcon = Label(
        width = 60, height = 60,
        posX = halfWidth - 500 / 2 + offsetX + 425, posY = offsetY + 5,
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
        //background = ColorVisual(44, 70, 127)
        background = Constants.sceneBackgroundColorVisual

        addComponents(
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
            playerGemLayoutList[0],
            playerGemLayoutList[1],
            playerGemLayoutList[2],
            playerGemLayoutList[3],
            playerWonIcon,
            quitButton,
            newGameButton,
            testButton
        )
    }

    override fun refreshAfterEndGame() {
        resetAllComponents()

        //fake game:
        rootService.currentGame = IndigoGame()

        val player = mutableListOf(
            Player("Alex", 0, PlayerType.LOCALPLAYER, 10, 7, mutableListOf(), mutableListOf()),
            Player("Nick", 0, PlayerType.LOCALPLAYER, 10, 5, mutableListOf(), mutableListOf()),
            Player("Kussay", 0, PlayerType.LOCALPLAYER, 8, 4, mutableListOf(), mutableListOf()),
            Player("Johannes", 0, PlayerType.LOCALPLAYER, 3, 1, mutableListOf(), mutableListOf())
        )
        rootService.currentGame!!.playerList = player
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        val players = game.playerList

        print("players size: " + players.size)

        players.forEach { assignGemsToPlayer(it) }

        //sort players with score / amount of gems
        players.sortWith(compareByDescending<Player> { it.score }.thenByDescending { it.amountOfGems })


        players.forEachIndexed { i, player ->
            playerNameInputList[i].text = player.name
            playerNameInputList[i].isVisible = true
            playerPosInputList[i].text = "${i + 1}"
            playerPosInputList[i].isVisible = true
            playerPointsInputList[i].text = "${player.score}"
            playerPointsInputList[i].isVisible = true
        }


    }

    fun resetAllComponents(){
        for(i in 0 until 4) {
            playerNameInputList[i].isVisible = false
            playerNameInputList[i].text = ""
            playerPosInputList[i].isVisible = false
            playerPosInputList[i].text = "1"
            playerPointsInputList[i].isVisible = false
            playerPointsInputList[i].text = "0"
        }

        playerWonIcon.visual = ImageVisual(Constants.wonIcon)
    }

    private fun assignGemsToPlayer(player: Player) {
        renderCollectedGemsLists()
        for(gate in player.gateList) {
            for(gem in gate.gemsCollected) {
                player.score += gem.toInt()
                player.amountOfGems ++
            }
        }

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

        playerGemLayoutList[playerIndex].clear()
        for(gem in playersGemList) {
            val gemVisual = when(gem) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }
            val gemView = TokenView( -gemSize / 2, -gemSize / 2, gemSize, gemSize, gemVisual)
            playerGemLayoutList[playerIndex].add(gemView)
        }
    }
}