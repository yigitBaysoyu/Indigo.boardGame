package view

import entity.*
import service.Constants
import service.RootService
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

/**
 * Displays the actual gameplay.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    private val sceneWidth = Constants.SCENE_WIDTH
    private val halfWidth = sceneWidth / 2

    private val hexagonWidth = (360 / 3 / 1.05).toInt()
    private val hexagonHeight = (416 / 3 / 1.05).toInt()
    private val hexagonSize = hexagonHeight / 2
    private val gemSize = 25

    private val playerColorSize = 45
    private val playerListOffsetX = 125
    private val playerListOffsetY = 65

    private val menuAreaMargin = 50
    private var simulationSpeedBinary = ""


	private val tileMap: BidirectionalMap<Tile, Area<TokenView>> = BidirectionalMap()

    private val outerArea = Pane<Area<TokenView>>(
        width = hexagonWidth * 9, height = hexagonHeight * 5 + (hexagonHeight / 2) * 4,
        posX = halfWidth - hexagonWidth * 9 / 2, posY = 85,
        visual = Visual.EMPTY
    )

    private val gateColorsBackground = TokenView(
        posX = halfWidth - (1077 / 1.07 / 2), posY = 23,
        width = 1077 / 1.07, height = 1106 / 1.07,
        visual = ImageVisual(Constants.gates)
    )

    private val cornersBackground = TokenView(
        posX = 0, posY = 0,
        width = 1920, height = 1080,
        visual = ImageVisual(Constants.cornersBackground)
    )

    private val playerOneLabel = Label(
        width = 300, height = 50,
        posX = playerListOffsetX, posY = playerListOffsetY,
        text = "Player1",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerTwoLabel = Label(
        width = 300, height = 50,
        posX = playerListOffsetX, posY = 250 + playerListOffsetY,
        text = "Player2",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerThreeLabel = Label(
        width = 300, height = 50,
        posX = playerListOffsetX, posY = 500 + playerListOffsetY,
        text = "Player3",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )

    private val playerFourLabel = Label(
        width = 300, height = 50,
        posX = playerListOffsetX, posY = 750 + playerListOffsetY,
        text = "Player4",
        font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
    )


    private val playerOneColor = TokenView(
        width = playerColorSize, height = playerColorSize,
        posX = 20 + playerListOffsetX, posY = 5 + playerListOffsetY,
        visual = ImageVisual(Constants.redGate)
    )

    private val playerTwoColor = TokenView(
        width = playerColorSize, height = playerColorSize,
        posX = 20 + playerListOffsetX, posY = 255 + playerListOffsetY,
        visual = ImageVisual(Constants.redGate)
    )

    private val playerThreeColor = TokenView(
        width = playerColorSize, height = playerColorSize,
        posX = 20 + playerListOffsetX, posY = 505 + playerListOffsetY,
        visual = ImageVisual(Constants.redGate)
    )

    private val playerFourColor = TokenView(
        width = playerColorSize, height = playerColorSize,
        posX = 20 + playerListOffsetX, posY = 755 + playerListOffsetY,
        visual = ImageVisual(Constants.redGate)
    )

    private val playerOneAIIcon = TokenView(
        width = 28 * 1.5, height = 25 * 1.5,
        posX = 230 + playerListOffsetX, posY = 5 + playerListOffsetY,
        visual = ImageVisual(Constants.aiIcon)
    )

    private val playerTwoAIIcon = TokenView(
        width = 28 * 1.5, height = 25 * 1.5,
        posX = 230 + playerListOffsetX, posY = 255 + playerListOffsetY,
        visual = ImageVisual(Constants.aiIcon)
    )

    private val playerThreeAIIcon = TokenView(
        width = 28 * 1.5, height = 25 * 1.5,
        posX = 230 + playerListOffsetX, posY = 505 + playerListOffsetY,
        visual = ImageVisual(Constants.aiIcon)
    )

    private val playerFourAIIcon = TokenView(
        width = 28 * 1.5, height = 25 * 1.5,
        posX = 230 + playerListOffsetX, posY = 755 + playerListOffsetY,
        visual = ImageVisual(Constants.aiIcon)
    )

    private val playerOneHand = TokenView(
        width = hexagonWidth,  height = hexagonHeight,
        posX = 70 + playerListOffsetX, posY = 90 + playerListOffsetY,
        visual = ImageVisual(Constants.pathTileImageList[0])
    )

    private val playerTwoHand = TokenView(
        width = hexagonWidth,  height = hexagonHeight,
        posX = 70 + playerListOffsetX, posY = 340 + playerListOffsetY,
        visual = ImageVisual(Constants.pathTileImageList[0])
    )

    private val playerThreeHand = TokenView(
        width = hexagonWidth,  height = hexagonHeight,
        posX = 70 + playerListOffsetX, posY = 590 + playerListOffsetY,
        visual = ImageVisual(Constants.pathTileImageList[0])
    )

    private val playerFourHand = TokenView(
        width = hexagonWidth,  height = hexagonHeight,
        posX = 70 + playerListOffsetX, posY = 840 + playerListOffsetY,
        visual = ImageVisual(Constants.pathTileImageList[0])
    )

    private val rotateButton = Button(
        width = 35 * 1.5, height = 32 * 1.5,
        posX = 227 + playerListOffsetX, posY = 840 + playerListOffsetY + hexagonHeight / 2 - 35 * 1.5 / 2,
        visual = ImageVisual(Constants.rotateIcon)
    ).apply {
        componentStyle = "-fx-background-radius: 25px;"
    }







    private val menuArea = Pane<ComponentView>(
        width = 400, height = Constants.SCENE_HEIGHT,
        posX = sceneWidth - 75, posY = 0,
        visual = ColorVisual(0, 0, 0, 60)
    ).apply {
        onMouseEntered = {
            playAnimation(
                MovementAnimation(
                    componentView = this,
                    toX = sceneWidth - 400,
                    duration = 100
                ).apply {
                    onFinished = {
                        posX = sceneWidth - 400.0
                    }
                }
            )

        }

        onMouseExited = {
            playAnimation(
                MovementAnimation(
                    componentView = this,
                    toX = sceneWidth - 75,
                    duration = 100
                ).apply {
                    onFinished = {
                        posX = sceneWidth - 75.0
                    }
                }
            )

        }
    }

    private val simulationSpeedLabel = Label(
        posX = menuAreaMargin, posY = 475,
        width = 300, height = 50,
        text = "Simulationspeed: 10",
        font = Font(size = 29, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        alignment = Alignment.CENTER
    )

    private val zeroButton = Button(
        width = 90, height = 50,
        posX = menuAreaMargin, posY = 550,
        text = "0",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = {simulationSpeedBinary = "0$simulationSpeedBinary"}
    }

    private val oneButton = Button(
        width = 90, height = 50,
        posX = 100 + menuAreaMargin, posY = 550,
        text = "1",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = {simulationSpeedBinary = "1$simulationSpeedBinary" }
    }

    private val setButton = Button(
        width = 90, height = 50,
        posX = 200 + menuAreaMargin, posY = 550,
        text = "Set",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = {
            val newSpeed = Integer.parseInt(simulationSpeedBinary, 2)
            simulationSpeedLabel.text = "Simulationspeed: $newSpeed"
            // rootService.setSimulationsSpeed(newSpeed)
            simulationSpeedBinary = ""
        }
    }


    private val undoButton = Button(
        width = 145, height = 50,
        posX = menuAreaMargin, posY = 625,
        text = "Undo",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val redoButton = Button(
        width = 145, height = 50,
        posX = menuAreaMargin + 155, posY = 625,
        text = "Redo",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val saveGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 700,
        text = "Save Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val loadGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 775,
        text = "Load Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val quitGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 850,
        text = "Quit Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    init {
        background = Constants.sceneBackgroundColorVisual
        addComponents(
            cornersBackground,
            gateColorsBackground,
            outerArea,
            playerOneLabel,
            playerTwoLabel,
            playerThreeLabel,
            playerFourLabel,
            playerOneColor,
            playerTwoColor,
            playerThreeColor,
            playerFourColor,
            playerOneAIIcon,
            playerTwoAIIcon,
            playerThreeAIIcon,
            playerFourAIIcon,
            playerOneHand,
            playerTwoHand,
            playerThreeHand,
            playerFourHand,
            rotateButton,
            menuArea
        )

        menuArea.addAll(
            simulationSpeedLabel,
            zeroButton,
            oneButton,
            setButton,
            undoButton,
            redoButton,
            saveGameButton,
            loadGameButton,
            quitGameButton
        )
    }

    override fun refreshAfterStartNewGame() {
        rootService.gameService.setTileFromAxialCoordinates(1, 1, PathTile(
            // mutableMapOf(0 to 3, 1 to 4, 5 to 2, 3 to 0, 4 to 1, 2 to 5),
            // mutableMapOf(0 to 5, 1 to 2, 3 to 4, 4 to 3, 2 to 1, 5 to 0),
            mutableMapOf(0 to 4, 1 to 5, 2 to 3, 3 to 2, 5 to 1, 4 to 0),
            3,
            1,
            1,
            gemPositions = mutableListOf(GemType.SAPPHIRE, GemType.EMERALD, GemType.AMBER, GemType.SAPPHIRE, GemType.AMBER, GemType.EMERALD),
            3
        ))

        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        renderGateAssignments()

        for (x in -5..5) {
            for (y in -5..5) {
                if (!rootService.gameService.checkIfValidAxialCoordinates(x, y)) continue
                val tile = rootService.gameService.getTileFromAxialCoordinates(x, y)
                if(tile is GateTile || tile is InvisibleTile) continue

                var image: BufferedImage = Constants.emptyTileImage

                if (tile is CenterTile) image = Constants.centerTileImage
                if (tile is EmptyTile) image = Constants.emptyTileImage
                if (tile is TreasureTile) image = Constants.treasureTileImage
                if (tile is PathTile) image = Constants.pathTileImageList[tile.type]

                val tileVisual: Visual = ImageVisual(image)


                val offsetX = hexagonWidth * 9 / 2
                val offsetY = (hexagonHeight * 5 + (hexagonHeight / 2) * 4) / 2

                val areaX = hexagonSize * (sqrt(3.0) * x + sqrt(3.0)/2 * y)
                val areaY = hexagonSize * (3.0/2.0 * y)

                val area = Area<TokenView>(
                    posX = areaX + offsetX - hexagonWidth/2, posY = areaY + offsetY - hexagonHeight / 2,
                    width = hexagonWidth, height = hexagonHeight,
                    visual = tileVisual
                )
                area.onMouseClicked = {
                    val clickedTile = tileMap.backward(area)
                    println("${clickedTile.xCoordinate}, ${clickedTile.yCoordinate}")
                }
                area.rotate(tile.rotationOffset * 60)

                if(tile is PathTile || tile is TreasureTile) renderGemsForPathOrTreasureTile(tile, area)
                if(tile is CenterTile) renderGemsForCenterTile(tile, area)

                outerArea.add(area)
                tileMap.add(tile, area)
            }
        }
    }

    private fun rotateListBackwards(list: MutableList<GemType>, offset: Int) {
        for(i in 0 until offset) {
            list.add(list.removeFirst())
        }
    }

    private fun renderGemsForPathOrTreasureTile(tile: Tile, area: Area<TokenView>) {
        var gemPositions: MutableList<GemType> = mutableListOf()
        if(tile is PathTile) gemPositions = tile.gemPositions
        if(tile is TreasureTile) gemPositions = tile.gemPositions

        rotateListBackwards(gemPositions, tile.rotationOffset)

        for(i in 0 .. 5) {
            val gemVisual = when(gemPositions[i]) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }

            var gemX = 0
            if(i == 0 || i == 2) gemX = hexagonWidth / 4 - gemSize / 2
            if(i == 3 || i == 5) gemX = -hexagonWidth / 4 + gemSize / 2
            if(i == 1) gemX = (hexagonWidth / 2 - gemSize * (0.75)).toInt()
            if(i == 4) gemX = (-hexagonWidth / 2 + gemSize * (0.75)).toInt()

            var gemY = 0
            if(i == 0 || i == 5) gemY = (-hexagonHeight * (3.0/8.0) + gemSize / (3.0/2.0)).toInt()
            if(i == 2 || i == 3) gemY = (hexagonHeight * (3.0/8.0) - gemSize / (3.0/2.0)).toInt()

            gemX += hexagonWidth / 2
            gemY += hexagonHeight / 2

            // This gem is the one that starts on the Treasure tile
            val correctedGemIndex = (i + tile.rotationOffset) % 6
            if(tile is TreasureTile && tile.connections[correctedGemIndex] == correctedGemIndex) {
                // this moves the gems coordinates closer to the center of the tile
                val centerX = hexagonWidth / 2
                val centerY = hexagonHeight / 2
                gemX = (centerX * 0.35 + gemX * 0.65).toInt()
                gemY = (centerY * 0.35 + gemY * 0.65).toInt()
            }

            //SAPPHIRE, EMERALD, AMBER, NONE, AMBER, NONE
            val gemView = TokenView(gemX - gemSize / 2, gemY - gemSize / 2, gemSize, gemSize, gemVisual)
            area.add(gemView)
        }
    }

    private fun renderGemsForCenterTile(tile: CenterTile, area: Area<TokenView>) {
        val offSets = mutableListOf(
            Pair(0, -hexagonHeight / 4),
            Pair(hexagonWidth / 4, -hexagonHeight / 8),
            Pair(-hexagonWidth / 4, -hexagonHeight / 8),
            Pair(hexagonWidth / 4, hexagonHeight / 8),
            Pair(-hexagonWidth / 4, hexagonHeight / 8)
        )

        for(i in 0 until tile.availableGems.size - 1) {
            var gemX = hexagonWidth / 2
            var gemY = hexagonHeight / 2

            gemX += offSets[i].first
            gemY += offSets[i].second
            val gemView = TokenView(
                gemX - gemSize / 2, gemY - gemSize / 2,
                gemSize, gemSize,
                ImageVisual(Constants.emeraldImage)
            )
            area.add(gemView)
        }

        if(tile.availableGems.size >= 1) {
            val gemX = hexagonWidth / 2
            val gemY = hexagonHeight / 2
            val gemView = TokenView(
                gemX - gemSize / 2, gemY - gemSize / 2,
                gemSize, gemSize,
                ImageVisual(Constants.sapphireImage)
            )
            area.add(gemView)
        }
    }

    private fun visualFromColorInt(number: Int): ImageVisual {
        return when(number) {
            0 -> ImageVisual(Constants.redGate)
            1 -> ImageVisual(Constants.whiteGate)
            2 -> ImageVisual(Constants.blueGate)
            else -> ImageVisual(Constants.purpleGate)
        }
    }

    private fun renderGateAssignments() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        val playerList = game.playerList

        // will hold the colors of the 6 gates
        val gateAssignments: MutableList<MutableList<Int>> = MutableList(6){ mutableListOf()}

        // Iterate over all Players
        for(playerIndex in 0 until playerList.size) {
            // Iterate over their GateTiles
            for(gateTile in playerList[playerIndex].gateList) {
                var gateNumber = 0
                // Iterate over the List of all Gate tiles to find which Gate the tile belongs to
                for(i in 0 until game.gateList.size) {
                    if(game.gateList[i].contains(gateTile)) {
                        gateNumber = i
                        break
                    }
                }
                gateAssignments[gateNumber].add(playerList[playerIndex].color)
            }
        }

        // Remove duplicates from the gateAssignments
        val filteredGateAssignments: List<MutableList<Int>> = gateAssignments.map { it.distinct().toMutableList() }
        for(list in filteredGateAssignments) {
            if(list.size == 1) list.add(list[0])
        }

        val gateVisuals: List<List<ImageVisual>> = filteredGateAssignments.map { n -> n.map { visualFromColorInt(it) } }

        val positions: MutableList<MutableList<Pair<Int, Int>>> = mutableListOf(
            mutableListOf(Pair(5, -3), Pair(5, -2)),
            mutableListOf(Pair(3, 2), Pair(2, 3)),
            mutableListOf(Pair(-2, 5), Pair(-3, 5)),
            mutableListOf(Pair(-5, 3), Pair(-5, 2)),
            mutableListOf(Pair(-3, -2), Pair(-2, -3)),
            mutableListOf(Pair(2, -5), Pair(3, -5)),
        )

        val tokenSize = 61

        for(i in 0 until 6) {
            for(j in 0 until 2) {
                val x = positions[i][j].first
                val y = positions[i][j].second
                val offsetX = hexagonWidth * 9 / 2
                val offsetY = (hexagonHeight * 5 + (hexagonHeight / 2) * 4) / 2

                val tokenX = hexagonSize * (sqrt(3.0) * x + sqrt(3.0)/2 * y)
                val tokenY = hexagonSize * (3.0/2.0 * y)

                val calculatedX = tokenX + offsetX
                val calculatedY = tokenY + offsetY

                val nudgedX = calculatedX * 0.97 + offsetX * 0.03
                val nudgedY = calculatedY * 0.97 + offsetY * 0.03

                val tokenArea = Area<TokenView>(
                    posX = nudgedX - tokenSize / 2, posY = nudgedY - tokenSize / 2,
                    width = tokenSize, height = tokenSize,
                    visual = gateVisuals[i][j]
                )
                outerArea.add(tokenArea)
            }
        }
    }
}