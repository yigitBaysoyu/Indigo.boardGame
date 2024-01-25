package view

import entity.*
import kotlinx.coroutines.runBlocking
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.event.MouseEvent
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt
import service.ConnectionState
import tools.aqua.bgw.event.KeyCode
import kotlin.system.measureTimeMillis

/**
 * Displays the actual gameplay.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT), Refreshable {

    // Constants / Measurements
    private val sceneWidth = Constants.SCENE_WIDTH
    private val sceneHeight = Constants.SCENE_HEIGHT
    private val halfWidth = sceneWidth / 2

    private val hexagonWidth = (360 / 3 / 1.05).toInt()
    private val hexagonHeight = (416 / 3 / 1.05).toInt()
    private val hexagonSize = hexagonHeight / 2
    private val gemSize = 25

    private val playerListOffsetX = 125
    private val playerListOffsetY = 65

    private val menuAreaMargin = 64
    private val menuAreaOffsetY = -200
    private var simulationSpeedBinary = ""

    // Maps
    private val tileMap: BidirectionalMap<Pair<Int, Int>, Area<TokenView>> = BidirectionalMap()
    private val gemMap: BidirectionalMap<Area<TokenView>, MutableList<TokenView>> = BidirectionalMap()

    // Components
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
        width = sceneWidth, height = sceneHeight,
        visual = ImageVisual(Constants.cornersBackground)
    )

    private val playerLabelList = mutableListOf<Label>().apply {
        for (i in 0 until 4) {
            val playerLabel = Label(
                width = 300, height = 50,
                posX = playerListOffsetX, posY = playerListOffsetY + i * 250,
                text = "Player" + (i + 1),
                font = Font(size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
            )
            add(playerLabel)
        }
    }

    private val playerColorList = mutableListOf<TokenView>().apply {
        for (i in 0 until 4) {
            val playerColor = TokenView(
                width = 45, height = 45,
                posX = 10 + playerListOffsetX, posY = 5 + playerListOffsetY + i * 250,
                visual = ImageVisual(Constants.redGate)
            )
            add(playerColor)
        }
    }

    private val playerScoreList = mutableListOf<Label>().apply {
        for (i in 0 until 4) {
            val scoreLabel = Label(
                width = 100, height = 50,
                posX = -25 + playerListOffsetX, posY = 90 + playerListOffsetY + i * 250 + hexagonHeight / 2 - 25,
                text = "0 pts.",
                font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240))
            )
            add(scoreLabel)
        }
    }

    private val playerAIIconList = mutableListOf<TokenView>().apply {
        for (i in 0 until 4) {
            val playerAIIcon = TokenView(
                width = 28 * 1.5, height = 25 * 1.5,
                posX = 230 + playerListOffsetX, posY = 5 + playerListOffsetY + i * 250,
                visual = ImageVisual(Constants.aiIcon)
            )
            add(playerAIIcon)
        }
    }

    private val playerHandList = mutableListOf<TokenView>().apply {
        for (i in 0 until 4) {
            val playerHand = TokenView(
                width = hexagonWidth, height = hexagonHeight,
                posX = 90 + playerListOffsetX, posY = 90 + playerListOffsetY + i * 250,
                visual = ImageVisual(Constants.pathTileImageList[0])
            )
            add(playerHand)
        }
    }

    private val playerGemLayoutList = mutableListOf<LinearLayout<TokenView>>().apply {
        for (i in 0 until 4) {
            val playerGemLayout = LinearLayout<TokenView>(
                posX = 0 + playerListOffsetX, posY = 48 + playerListOffsetY + i * 250,
                width = 300, height = 35,
                visual = Visual.EMPTY,
                alignment = Alignment.CENTER_LEFT
            )
            add(playerGemLayout)
        }
    }

    private val rotateButton = Button(
        width = 35 * 1.5, height = 32 * 1.5,
        posX = 227 + playerListOffsetX, posY = 840 + playerListOffsetY + hexagonHeight / 2 - 35 * 1.5 / 2,
        visual = ImageVisual(Constants.rotateIcon)
    ).apply {
        componentStyle = "-fx-background-radius: 25px;"
        onMouseClicked = { rootService.playerService.rotateTile() }
    }



    private val menuArea = Pane<ComponentView>(
        width = 425, height = sceneHeight,
        posX = sceneWidth - 75, posY = 0,
        visual = ColorVisual(0, 0, 0, 60)
    ).apply {
        onMouseEntered = {
            playAnimation(
                MovementAnimation(
                    componentView = this,
                    toX = sceneWidth - 425,
                    duration = 100
                ).apply {
                    onFinished = {
                        posX = sceneWidth - 425.0
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
        posX = menuAreaMargin, posY = 275 + menuAreaOffsetY,
        width = 300, height = 50,
        text = "Simulationspeed: 10",
        font = Font(size = 29, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        alignment = Alignment.CENTER
    )

    private val zeroButton = Button(
        width = 90, height = 50,
        posX = menuAreaMargin, posY = 350 + menuAreaOffsetY,
        text = "0",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { simulationSpeedBinary = "0$simulationSpeedBinary" }
    }

    private val oneButton = Button(
        width = 90, height = 50,
        posX = 100 + menuAreaMargin, posY = 350 + menuAreaOffsetY,
        text = "1",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { simulationSpeedBinary = "1$simulationSpeedBinary" }
    }

    private val setButton = Button(
        width = 90, height = 50,
        posX = 200 + menuAreaMargin, posY = 350 + menuAreaOffsetY,
        text = "Set",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = {
            val newSpeed = Integer.parseInt(simulationSpeedBinary, 2)
            rootService.gameService.setSimulationSpeed(newSpeed.toDouble())
            simulationSpeedBinary = ""
        }
    }


    private val undoButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 525 + menuAreaOffsetY,
        text = "Undo",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { rootService.playerService.undo() }
    }

    private val redoButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 600 + menuAreaOffsetY,
        text = "Redo",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { rootService.playerService.redo() }
    }

    private val saveGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 800 + menuAreaOffsetY,
        text = "Save Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { rootService.gameService.saveGame() }
    }

    private val loadGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 875 + menuAreaOffsetY,
        text = "Load Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseClicked = { rootService.gameService.loadGame() }
    }

    private val returnToMenuWarning = Label(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 1020 + menuAreaOffsetY,
        text = "Progress will be lost!",
        font = Font(size = 20, fontWeight = Font.FontWeight.BOLD, color = Color(255, 60, 79)),
        visual = Visual.EMPTY
    ).apply {
        isVisible = false
    }

    private val returnToMenuButtonBackground = Button(
        width = 300 + 50, height = 65 + 30,
        posX = menuAreaMargin - 25, posY = 1070 + menuAreaOffsetY - 15,
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = { returnToMenuWarning.isVisible = true }
        onMouseExited = { returnToMenuWarning.isVisible = false }
    }

    val returnToMenuButton = Button(
        width = 300, height = 65,
        posX = menuAreaMargin, posY = 1070 + menuAreaOffsetY,
        text = "Return to Menu",
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
        onMouseEntered = { returnToMenuWarning.isVisible = true }
        onMouseExited = { returnToMenuWarning.isVisible = false }
    }

    val quitGameButton = Button(
        width = 300, height = 50,
        posX = menuAreaMargin, posY = 1150 + menuAreaOffsetY,
        text = "Quit Game",
        font = Font(size = 35, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
        visual = Visual.EMPTY
    ).apply {
        componentStyle = "-fx-background-color: ${Constants.buttonBackgroundColor}; -fx-background-radius: 25px;"
    }

    private val menuAreaArrow = TokenView(
        width = 96, height = sceneHeight,
        posX = -57, posY = 0,
        visual = ImageVisual(Constants.menuAreaArrow)
    )

    init {
        background = Constants.sceneBackgroundColorVisual
        addComponents(
            cornersBackground,
            gateColorsBackground,
            outerArea,
            playerLabelList[0],
            playerLabelList[1],
            playerLabelList[2],
            playerLabelList[3],
            playerColorList[0],
            playerColorList[1],
            playerColorList[2],
            playerColorList[3],
            playerScoreList[0],
            playerScoreList[1],
            playerScoreList[2],
            playerScoreList[3],
            playerAIIconList[0],
            playerAIIconList[1],
            playerAIIconList[2],
            playerAIIconList[3],
            playerHandList[0],
            playerHandList[1],
            playerHandList[2],
            playerHandList[3],
            playerGemLayoutList[0],
            playerGemLayoutList[1],
            playerGemLayoutList[2],
            playerGemLayoutList[3],
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
            returnToMenuWarning,
            returnToMenuButtonBackground,
            returnToMenuButton,
            quitGameButton,
            menuAreaArrow
        )

        this.onKeyPressed = keyHandler@{
            if(it.keyCode != KeyCode.R) return@keyHandler

            val game = rootService.currentGame
            checkNotNull(game) { "game is null" }

            if(game.getActivePlayer().playerType != PlayerType.LOCALPLAYER) return@keyHandler
            rootService.playerService.rotateTile()
        }
    }

    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        resetAllComponents()

        renderGateAssignments()

        renderTiles()

        renderCollectedGemsLists()

        renderPlayerConfiguration()

        setRotateButtonHeight()

        renderPlayerHands()

        updatePlayerScores()

        refreshAfterSimulationSpeedChange(game.simulationSpeed)

        setButtonsIfNetworkGame()

        handleUndoRedoButton()

        handleAIPlayers()
    }

    private fun handleAIPlayers() {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame) { "game is null" }

        when(currentGame.playerList[0].playerType){
            PlayerType.RANDOMAI -> {
                rootService.aiService.randomNextTurn()
            }
            PlayerType.SMARTAI -> {
                val timeTaken = measureTimeMillis {
                    //Blocking current Thread until coroutine in calculateNextTurn() is finished
                    runBlocking {
                        rootService.aiService.calculateNextTurn()
                    }
                }
                println("Took : ${timeTaken/1000} sec")
            }
            else -> return
        }
    }

    private fun setButtonsIfNetworkGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        if (game.isNetworkGame) {
            undoButton.isVisible = false
            undoButton.isDisabled = true
            redoButton.isVisible = false
            redoButton.isDisabled = true
            saveGameButton.isVisible = false
            saveGameButton.isDisabled = true
            loadGameButton.isVisible = false
            loadGameButton.isDisabled = true
        } else {
            undoButton.isVisible = true
            undoButton.isDisabled = false
            redoButton.isVisible = true
            redoButton.isDisabled = false
            saveGameButton.isVisible = true
            saveGameButton.isDisabled = false
            loadGameButton.isVisible = true
            loadGameButton.isDisabled = false
        }
    }

    private fun rotateListBackwards(list: MutableList<GemType>, offset: Int): MutableList<GemType> {
        val copiedList = mutableListOf<GemType>()
        for (gem in list) copiedList.add(gem)

        for (i in 0 until offset) {
            copiedList.add(copiedList.removeFirst())
        }

        return copiedList
    }

    private fun renderGemsForPathOrTreasureTile(tile: Tile, area: Area<TokenView>) {
        var unRotatedGemPositions: MutableList<GemType> = mutableListOf()
        if (tile is PathTile) unRotatedGemPositions = tile.gemPositions
        if (tile is TreasureTile) unRotatedGemPositions = tile.gemPositions

        val gemPositions = rotateListBackwards(unRotatedGemPositions, tile.rotationOffset)
        val gemList: MutableList<TokenView> = MutableList(6) { TokenView(visual = Visual.EMPTY) }

        for (i in 0..5) {
            val gemVisual = when (gemPositions[i]) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }

            var gemX = 0
            if (i == 0 || i == 2) gemX = hexagonWidth / 4 - gemSize / 2
            if (i == 3 || i == 5) gemX = -hexagonWidth / 4 + gemSize / 2
            if (i == 1) gemX = (hexagonWidth / 2 - gemSize * (0.75)).toInt()
            if (i == 4) gemX = (-hexagonWidth / 2 + gemSize * (0.75)).toInt()

            var gemY = 0
            if (i == 0 || i == 5) gemY = (-hexagonHeight * (3.0 / 8.0) + gemSize / (3.0 / 2.0)).toInt()
            if (i == 2 || i == 3) gemY = (hexagonHeight * (3.0 / 8.0) - gemSize / (3.0 / 2.0)).toInt()

            gemX += hexagonWidth / 2
            gemY += hexagonHeight / 2

            // This gem is the one that starts on the Treasure tile
            val correctedGemIndex = (i + tile.rotationOffset) % 6
            if (tile is TreasureTile && tile.connections[correctedGemIndex] == correctedGemIndex) {
                // this moves the gems coordinates closer to the center of the tile
                val centerX = hexagonWidth / 2
                val centerY = hexagonHeight / 2
                gemX = (centerX * 0.35 + gemX * 0.65).toInt()
                gemY = (centerY * 0.35 + gemY * 0.65).toInt()
            }

            val gemView = TokenView(gemX - gemSize / 2, gemY - gemSize / 2, gemSize, gemSize, gemVisual)
            area.add(gemView)
            gemList[correctedGemIndex] = gemView
        }

        gemMap.add(area, gemList)
    }

    private fun renderGemsForCenterTile(tile: CenterTile, area: Area<TokenView>) {
        val offSets = mutableListOf(
            Pair(0, -hexagonHeight / 4),
            Pair(hexagonWidth / 4, -hexagonHeight / 8),
            Pair(-hexagonWidth / 4, -hexagonHeight / 8),
            Pair(hexagonWidth / 4, hexagonHeight / 8),
            Pair(-hexagonWidth / 4, hexagonHeight / 8)
        )

        val gemList = mutableListOf<TokenView>()

        for (i in 0 until tile.availableGems.size - 1) {
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
            gemList.add(gemView)
        }

        if (tile.availableGems.size >= 1) {
            val gemX = hexagonWidth / 2
            val gemY = hexagonHeight / 2
            val gemView = TokenView(
                gemX - gemSize / 2, gemY - gemSize / 2,
                gemSize, gemSize,
                ImageVisual(Constants.sapphireImage)
            )
            area.add(gemView)
            gemList.add(0, gemView)
        }

        gemMap.add(area, gemList)
    }

    private fun visualFromColorInt(number: Int): ImageVisual {
        return when (number) {
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
        val gateAssignments: MutableList<MutableList<Int>> = MutableList(6) { mutableListOf() }

        // Iterate over all Players
        for (playerIndex in 0 until playerList.size) {
            // Iterate over their GateTiles
            for (gateTile in playerList[playerIndex].gateList) {
                var gateNumber = 0
                // Iterate over the List of all Gate tiles to find which Gate the tile belongs to
                for (i in 0 until game.gateList.size) {
                    if (game.gateList[i].contains(gateTile)) {
                        gateNumber = i
                        break
                    }
                }
                gateAssignments[gateNumber].add(playerList[playerIndex].color)
            }
        }

        // Remove duplicates from the gateAssignments
        val filteredGateAssignments: List<MutableList<Int>> = gateAssignments.map { it.distinct().toMutableList() }
        for (list in filteredGateAssignments) {
            if (list.size == 1) list.add(list[0])
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

        for (i in 0 until 6) {
            for (j in 0 until 2) {
                val x = positions[i][j].first
                val y = positions[i][j].second
                val offsetX = hexagonWidth * 9 / 2
                val offsetY = (hexagonHeight * 5 + (hexagonHeight / 2) * 4) / 2

                val tokenX = hexagonSize * (sqrt(3.0) * x + sqrt(3.0) / 2 * y)
                val tokenY = hexagonSize * (3.0 / 2.0 * y)

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

    private fun renderCollectedGemsLists() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        game.playerList.forEachIndexed { index, player ->
            renderCollectedGemsForPlayer(player, index)
        }
    }

    private fun renderCollectedGemsForPlayer(player: Player, playerIndex: Int) {
        val playersGemList = mutableListOf<GemType>()

        for (gate in player.gateList) {
            playersGemList.addAll(gate.gemsCollected)
        }

        playerGemLayoutList[playerIndex].clear()
        for (gem in playersGemList) {
            val gemVisual = when (gem) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }
            val gemView = TokenView(-gemSize / 2, -gemSize / 2, gemSize, gemSize, gemVisual)
            playerGemLayoutList[playerIndex].add(gemView)
        }
    }

    private fun renderTiles() {
        for (x in -5..5) {
            for (y in -5..5) {
                if (!rootService.gameService.checkIfValidAxialCoordinates(x, y)) continue
                val tile = rootService.gameService.getTileFromAxialCoordinates(x, y)
                if (tile is GateTile || tile is InvisibleTile) continue

                var image: BufferedImage = Constants.emptyTileImage

                if (tile is CenterTile) image = Constants.centerTileImage
                if (tile is EmptyTile) image = Constants.emptyTileImage
                if (tile is TreasureTile) image = Constants.treasureTileImage
                if (tile is PathTile) image = Constants.pathTileImageList[tile.type]

                val tileVisual: Visual = ImageVisual(image)

                val offsetX = hexagonWidth * 9 / 2
                val offsetY = (hexagonHeight * 5 + (hexagonHeight / 2) * 4) / 2

                val areaX = hexagonSize * (sqrt(3.0) * x + sqrt(3.0) / 2 * y)
                val areaY = hexagonSize * (3.0 / 2.0 * y)

                val area = Area<TokenView>(
                    posX = areaX + offsetX - hexagonWidth / 2, posY = areaY + offsetY - hexagonHeight / 2,
                    width = hexagonWidth, height = hexagonHeight,
                    visual = tileVisual
                )

                if (tile is EmptyTile) area.onMouseClicked = { handleTileClick(it, area) }
                if (tile is EmptyTile) {
                    area.onMouseEntered = { handleOnMouseEntered(area, x, y) }
                    area.onMouseExited = { handleOnMouseExited(area, x, y) }
                }

                area.rotate(tile.rotationOffset * 60)

                if (tile is PathTile || tile is TreasureTile) renderGemsForPathOrTreasureTile(tile, area)
                if (tile is CenterTile) renderGemsForCenterTile(tile, area)

                outerArea.add(area)
                tileMap.add(Pair(x, y), area)
            }
        }
    }

    private fun handleOnMouseEntered(area: Area<TokenView>, x: Int, y: Int) {
        if (rootService.gameService.getTileFromAxialCoordinates(x, y) !is EmptyTile) return

        val game = rootService.currentGame
        checkNotNull(game) { "no active game" }

        // If Player cant place a tile because he is not a Local Player, don't show tile shadow
        if(game.playerList[game.activePlayerID].playerType != PlayerType.LOCALPLAYER) {
            return
        }

        val tileInPlayersHand = game.playerList[game.activePlayerID].playHand[0]

        val hoverTileVisual = ImageVisual(Constants.pathTileImageList[tileInPlayersHand.type])
        hoverTileVisual.transparency = 0.63
        val hoverBackgroundImage = ImageVisual(Constants.emptyTileImage)

        if (!rootService.gameService.isPlaceAble(x, y, tileInPlayersHand)) {
            val hoverTintVisual = ImageVisual(Constants.hoverTintImage)
            hoverTileVisual.transparency = 0.45
            area.visual = CompoundVisual(hoverBackgroundImage, hoverTileVisual, hoverTintVisual)
        } else {
            area.visual = CompoundVisual(hoverBackgroundImage, hoverTileVisual)
        }

        area.rotation = tileInPlayersHand.rotationOffset * 60.0
    }

    private fun handleOnMouseExited(area: Area<TokenView>, x: Int, y: Int) {
        if (rootService.gameService.getTileFromAxialCoordinates(x, y) !is EmptyTile) return
        area.visual = ImageVisual(Constants.emptyTileImage)
        area.rotation = 0.0
    }

    private fun handleTileClick(mouseEvent: MouseEvent, area: Area<TokenView>) {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        if (game.isNetworkGame && rootService.networkService.connectionState != ConnectionState.PLAYING_MY_TURN) {
            return
        }
        val mouseX: Double = mouseEvent.posX.toDouble()
        val mouseY: Double = mouseEvent.posY.toDouble()
        val tileCoords = tileMap.backward(area)
        val tileX = tileCoords.first
        val tileY = tileCoords.second

        // Check if player clicked top left or bottom left area that is outside the
        // hexagon but inside the area rectangle
        val maxPossibleWidth: Double = hexagonWidth / 2.0
        val maxPossibleHeight: Double = hexagonHeight / 4.0

        // used for top left corner
        val maxAllowedWidth = (-maxPossibleWidth / maxPossibleHeight * mouseY).toInt() + maxPossibleWidth.toInt()

        // used for bottom left corner
        val distanceFromMaxHeight =
            (-maxPossibleHeight / maxPossibleWidth * mouseX).toInt() + maxPossibleHeight.toInt()
        val minNeededHeight = hexagonHeight / 4 - distanceFromMaxHeight + hexagonHeight * 3 / 4

        // check if top left corner was clicked
        if (mouseX.toInt() in 0..maxAllowedWidth && mouseY.toInt() in 0..hexagonHeight / 4) {
            // tileY -= 1
            return
        }

        // check if bottom left corner was clicked
        if (mouseX.toInt() in 0..hexagonWidth / 2 && mouseY.toInt() in minNeededHeight..hexagonHeight) {
            // tileX -= 1
            // tileY += 1
            return
        }

        rootService.playerService.placeTile(tileX, tileY)
    }

    private fun handleUndoRedoButton() {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }
        undoButton.isDisabled = game.undoStack.isEmpty()
        redoButton.isDisabled = game.redoStack.isEmpty()
    }

    private fun renderPlayerConfiguration() {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        game.playerList.forEachIndexed { index, player ->
            playerLabelList[index].text = player.name
            playerLabelList[index].isVisible = true
            playerColorList[index].visual = visualFromColorInt(player.color)
            playerColorList[index].isVisible = true
            playerAIIconList[index].isVisible = when (player.playerType) {
                PlayerType.RANDOMAI -> true
                PlayerType.SMARTAI -> true
                else -> false
            }
            playerHandList[index].isVisible = true
            playerGemLayoutList[index].isVisible = true
            playerScoreList[index].isVisible = true
        }
    }

    private fun setRotateButtonHeight() {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        rotateButton.posY = 90 + playerListOffsetY + hexagonHeight / 2 - 35 * 1.5 / 2 + (game.activePlayerID * 250)
    }

    private fun updatePlayerScores() {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        game.playerList.forEachIndexed { index, player ->
            playerScoreList[index].text = "${player.score} pts."
        }
    }

    private fun renderPlayerHands(turn: Turn? = null) {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        game.playerList.forEachIndexed { index, player ->
            if (player.playHand.size <= 0) {
                playerHandList[index].visual = Visual.EMPTY
                return@forEachIndexed
            }

            if(turn != null && turn.playerID == index) {
                playerHandList[index].posX -= 500
                val animation = MovementAnimation(
                    componentView = playerHandList[index],
                    byX = 500,
                    duration = 200
                )
                animation.onFinished = {
                    playerHandList[index].posX += 500
                    unlock()
                }
                lock()
                playAnimation(animation)
            }
            val tileType = player.playHand[0].type
            playerHandList[index].visual = ImageVisual(Constants.pathTileImageList[tileType])
            playerHandList[index].rotation = player.playHand[0].rotationOffset * 60.0
        }
    }

    private fun resetAllComponents() {
        outerArea.clear()

        for (label in playerLabelList) label.isVisible = false
        for (color in playerColorList) color.isVisible = false
        for (aiIcon in playerAIIconList) aiIcon.isVisible = false
        for (hand in playerHandList) hand.isVisible = false
        for (gemLayout in playerGemLayoutList) gemLayout.isVisible = false
        for (gemLayout in playerGemLayoutList) gemLayout.clear()
        for (playerScore in playerScoreList) playerScore.isVisible = false
        tileMap.clear()
        gemMap.clear()
    }

    override fun refreshAfterSimulationSpeedChange(speed: Double) {
        simulationSpeedLabel.text = "Simulationspeed: ${speed.toInt()}"
    }

    override fun refreshAfterTileRotated() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        val rotationOffset = game.playerList[game.activePlayerID].playHand[0].rotationOffset
        playerHandList[game.activePlayerID].rotation = rotationOffset * 60.0
    }

    override fun refreshAfterTilePlaced(turn: Turn) {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        val duration = if(game.playerList[turn.playerID].playerType == PlayerType.RANDOMAI) 750 else 0
        val animation = DelayAnimation(duration)

        animation.onFinished = {
            val tile = turn.placedTile
            val x = tile.xCoordinate
            val y = tile.yCoordinate
            val view = tileMap.forward(Pair(x, y))

            val newVisual = ImageVisual(Constants.pathTileImageList[tile.type])

            view.visual = newVisual
            view.rotation = tile.rotationOffset * 60.0

            renderGemsForPathOrTreasureTile(tile, view)
            renderPlayerHands(turn)
            updatePlayerScores()
            renderCollectedGemsLists()
            setRotateButtonHeight()
            handleUndoRedoButton()

            for (gemMovement in turn.gemMovements) {
                refreshAfterGemMoved(gemMovement)
            }

            unlock()

            rootService.gameService.checkIfGameEnded()
            rootService.gameService.switchPlayer()
        }

        lock()
        playAnimation(animation)
    }

    override fun refreshAfterGemMoved(movement: GemMovement) {
        val startX = movement.startTile.xCoordinate
        val startY = movement.startTile.yCoordinate

        val tileView = tileMap.forward(Pair(startX, startY))
        val gemViews = gemMap.forward(tileView)

        val gemView = if (movement.startTile is CenterTile) {
            gemViews[movement.startTile.availableGems.size + 1 - 1]
        } else {
            gemViews[movement.positionOnStartTile]
        }

        gemView.visual = Visual.EMPTY

        if (!movement.didCollide && movement.endTile !is GateTile) {
            val endX = movement.endTile.xCoordinate
            val endY = movement.endTile.yCoordinate

            val endTileView = tileMap.forward(Pair(endX, endY))
            val endGemViews = gemMap.forward(endTileView)
            val endGemView = endGemViews[movement.positionOnEndTile]

            val gemVisual = when (movement.gemType) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }

            endGemView.visual = gemVisual
        }
    }

    override fun refreshAfterUndo(turn: Turn) {
        val game = rootService.currentGame
        checkNotNull(game) { "game is null" }

        // render reverted scores
        updatePlayerScores()

        // render old tile in hand
        val player = game.playerList[turn.playerID]
        if (player.playHand.isNotEmpty()) {
            val tileType = player.playHand[0].type
            playerHandList[turn.playerID].visual = ImageVisual(Constants.pathTileImageList[tileType])
            playerHandList[turn.playerID].rotation = player.playHand[0].rotationOffset * 60.0
        } else {
            playerHandList[turn.playerID].visual = Visual.EMPTY
        }

        // remove placed tile from board
        val tileX = turn.placedTile.xCoordinate
        val tileY = turn.placedTile.yCoordinate
        val tileView = tileMap.forward(Pair(tileX, tileY))
        tileView.visual = ImageVisual(Constants.emptyTileImage)

        // update whose turn it is
        setRotateButtonHeight()

        // update collected gems list under players name
        renderCollectedGemsForPlayer(player, turn.playerID)

        // update gem positions
        for (movement in turn.gemMovements) {

            // put gem back on start tile
            val startX = movement.startTile.xCoordinate
            val startY = movement.startTile.yCoordinate

            val startView = tileMap.forward(Pair(startX, startY))
            val gemViews = gemMap.forward(startView)

            val gemView = if (movement.startTile is CenterTile) {
                gemViews[movement.startTile.availableGems.size - 1]
            } else {
                gemViews[movement.positionOnStartTile]
            }

            val gemVisual = when (movement.gemType) {
                GemType.AMBER -> ImageVisual(Constants.amberImage)
                GemType.EMERALD -> ImageVisual(Constants.emeraldImage)
                GemType.SAPPHIRE -> ImageVisual(Constants.sapphireImage)
                GemType.NONE -> Visual.EMPTY
            }
            gemView.visual = gemVisual

            // remove gem from endTile
            if (!movement.didCollide && movement.endTile !is GateTile) {
                val endX = movement.endTile.xCoordinate
                val endY = movement.endTile.yCoordinate
                val endView = tileMap.forward(Pair(endX, endY))
                val endGemView = gemMap.forward(endView)[movement.positionOnEndTile]
                endGemView.visual = Visual.EMPTY
            }
        }

        // clear area
        tileView.clear()
        // remove entry from gemMap
        gemMap.removeForward(tileView)

        handleUndoRedoButton()
    }

    override fun refreshConnectionState(newState: ConnectionState) {

        if (newState == ConnectionState.WAITING_FOR_OPPONENTS_TURN) {

            rotateButton.isDisabled = true
            rotateButton.isVisible = false



        } else if (newState == ConnectionState.PLAYING_MY_TURN) {

            rotateButton.isDisabled = false
            rotateButton.isVisible = true

            val client = rootService.networkService.client

            if (client != null && client.playerType != PlayerType.NETWORKPLAYER && client.playerType != PlayerType.LOCALPLAYER) {


            } else {



            }

        }

    }
}