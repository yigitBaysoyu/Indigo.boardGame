package view

import entity.IndigoGame
import entity.Player
import entity.PlayerType
import service.Constants
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
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

    private val playerWonIcon = Label(
        width = 75, height = 75,
        posX = halfWidth - 500 / 2 + offsetX + 450, posY = offsetY,
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
            Player("Nick", 0, PlayerType.LOCALPLAYER, 10, 5, mutableListOf(), mutableListOf())
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
        }


    }

    fun resetAllComponents(){
        for(i in 0 until 4) {
            playerNameInputList[i].isVisible = false
            playerNameInputList[i].text = ""
            playerPosInputList[i].isVisible = false
            playerPosInputList[i].text = "1"
        }

        playerWonIcon.visual = ImageVisual(Constants.wonIcon)
    }

    private fun assignGemsToPlayer(player: Player) {
        for(gate in player.gateList) {
            for(gem in gate.gemsCollected) {
                player.score += gem.toInt()
                player.amountOfGems ++
            }
        }
    }
}