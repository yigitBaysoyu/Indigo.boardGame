package view

import entity.IndigoGame
import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Is displayed after a game has ended. Shows scores and winner of the game.
 */
class GameEndedScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2
    private val offsetY = -50

    private val gameOverLabel = Label(
        width = 600, height = 75,
        posX = halfWidth - 600 / 2, posY = offsetY + 150,
        text = "GAME OVER",
        font = Font(size = 80, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val firstPlaceNumberLabel = Label(
        width = 75, height = 75,
        posX = halfWidth - 525 / 2, posY = offsetY + 300,
        text = "1.",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val firstPlaceNameLabel = Label(
        width = 400, height = 75,
        posX = halfWidth - 400 / 2, posY = offsetY + 300,
        text = "David",
        font = Font(size = 40, fontWeight = Font.FontWeight.NORMAL, color = Color(250, 250, 240))
    )

    private val firstPlaceWinnerSymbol = Label(
        width = 50, height = 50,
        posX = halfWidth + 180, posY = offsetY + 310,
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val secondPlaceNumberLabel = Label(
        width = 75, height = 75,
        posX = halfWidth - 525 / 2, posY = offsetY + 425,
        text = "2.",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val secondPlaceNameLabel = Label(
        width = 400, height = 75,
        posX = halfWidth - 400 / 2, posY = offsetY + 425,
        text = "Alex",
        font = Font(size = 40, fontWeight = Font.FontWeight.NORMAL, color = Color(250, 250, 240))
    )

    private val thirdPlaceNumberLabel = Label(
        width = 75, height = 75,
        posX = halfWidth - 525 / 2, posY = offsetY + 550,
        text = "3.",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val thirdPlaceNameLabel = Label(
        width = 400, height = 75,
        posX = halfWidth - 400 / 2, posY = offsetY + 550,
        text = "Johannes",
        font = Font(size = 40, fontWeight = Font.FontWeight.NORMAL, color = Color(250, 250, 240))
    )

    private val fourthPlaceNumberLabel = Label(
        width = 75, height = 75,
        posX = halfWidth - 525 / 2, posY = offsetY + 675,
        text = "4.",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val fourthPlaceNameLabel = Label(
        width = 400, height = 75,
        posX = halfWidth - 400 / 2, posY = offsetY + 675,
        text = "Nick",
        font = Font(size = 40, fontWeight = Font.FontWeight.NORMAL, color = Color(250, 250, 240))
    )

    val newGameButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2 - 185, posY = offsetY + 900,
        text = "New Game",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }

    val quitButton = Button(
        width = 350, height = 75,
        posX = halfWidth - 350 / 2 + 185, posY = offsetY + 900,
        text = "Quit",
        font = Font(size = 45, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: #211c4f; -fx-background-radius: 25px;"
    }


    init {
        background = ColorVisual(44, 70, 127)
        val winnerPNG : BufferedImage = ImageIO.read(GameEndedScene::class.java.getResource("/GUIAssets/EndGameScene/winnerSymbol.png"))
        firstPlaceWinnerSymbol.visual = ImageVisual(winnerPNG)
        addComponents(
            gameOverLabel,
            newGameButton,
            quitButton,
            firstPlaceWinnerSymbol,
            firstPlaceNumberLabel,
            firstPlaceNameLabel,
            secondPlaceNumberLabel,
            secondPlaceNameLabel,
            thirdPlaceNumberLabel,
            thirdPlaceNameLabel,
            fourthPlaceNumberLabel,
            fourthPlaceNameLabel
        )
    }

    override fun refreshAfterEndGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        val players = game.playerList

        //sort players with score / amount of gems
        players.sortWith(compareByDescending<Player> { it.score }.thenByDescending { it.score })

        val labels = mutableListOf<Label>(firstPlaceNameLabel, secondPlaceNameLabel,
            thirdPlaceNumberLabel, fourthPlaceNumberLabel)
        var i = 0
        players.forEach { player ->
            labels[i].text = player.name
            i++
        }
    }
}