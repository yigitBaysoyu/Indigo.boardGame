package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.sqrt

/**
 * Displays the actual gameplay.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2

    private val hexagonWidth = 360 / 3
    private val hexagonHeight = 416 / 3
    private val hexagonSize = hexagonHeight / 2

    private val gemSize = 25

	private val tileMap: BidirectionalMap<Tile, Area<TokenView>> = BidirectionalMap()

    private val outerArea = Pane<Area<TokenView>>(
        width = hexagonWidth * 9, height = hexagonHeight * 5 + (hexagonHeight / 2) * 4,
        posX = halfWidth - hexagonWidth * 9 / 2, posY = 50,
        visual = Visual.EMPTY
    )

    private val gameSceneHeader = Label(
        width = 400, height = 75,
        posX = 25, posY = 25,
        text = "GameScene",
        font = Font(size = 65, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
    )

    init {
        background = ColorVisual(44, 70, 127)
        addComponents(gameSceneHeader, outerArea)
    }

    override fun refreshAfterStartNewGame() {
        // Get all different Tile images
        val pathTileImageList: MutableList<BufferedImage> = mutableListOf()
        for (i in 0..4) pathTileImageList.add(ImageIO.read(GameScene::class.java.getResource("/TileType$i.png")))
        val treasureTileImage: BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileTreasure.png"))
        val centerTileImage: BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileCenter.png"))
        val emptyTileImage: BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileEmpty.png"))

        // Get all different Gem images
        val amberImage = ImageIO.read(GameScene::class.java.getResource("/Amber.png"))
        val emeraldImage = ImageIO.read(GameScene::class.java.getResource("/Emerald.png"))
        val sapphireImage = ImageIO.read(GameScene::class.java.getResource("/Sapphire.png"))

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

        for (x in -5..5) {
            for (y in -5..5) {
                if (!rootService.gameService.checkIfValidAxialCoordinates(x, y)) continue
                val tile = rootService.gameService.getTileFromAxialCoordinates(x, y)
                if(tile is GateTile || tile is InvisibleTile) continue

                var image: BufferedImage = emptyTileImage

                if (tile is CenterTile) image = centerTileImage
                if (tile is EmptyTile) image = emptyTileImage
                if (tile is TreasureTile) image = treasureTileImage
                if (tile is PathTile) image = pathTileImageList[tile.type]

                val tileVisual: Visual = ImageVisual(image)


                val offsetX = hexagonWidth * 9 / 2
                val offsetY = (hexagonHeight * 5 + (hexagonHeight / 2) * 4) / 2

                val areaX = hexagonSize * (sqrt(3.0) * x + sqrt(3.0)/2 * y)
                val areaY = hexagonSize * (3.0/2.0 * y)

                // create gems as token views
                val area = Area<TokenView>(areaX + offsetX - hexagonWidth/2, areaY + offsetY - hexagonHeight / 2, hexagonWidth, hexagonHeight, tileVisual)
                area.onMouseClicked = {
                    val clickedTile = tileMap.backward(area)
                    println("${clickedTile.xCoordinate}, ${clickedTile.yCoordinate}")
                }
                area.rotate(tile.rotationOffset * 60)

                if(tile is PathTile) {
                    for(i in 0 .. 5) {
                        val gemVisual = when(tile.gemPositions[i]) {
                            GemType.AMBER -> ImageVisual(amberImage)
                            GemType.EMERALD -> ImageVisual(emeraldImage)
                            GemType.SAPPHIRE -> ImageVisual(sapphireImage)
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

                        //SAPPHIRE, EMERALD, AMBER, NONE, AMBER, NONE
                        val gemView = TokenView(gemX - gemSize / 2, gemY - gemSize / 2, gemSize, gemSize, gemVisual)
                        area.add(gemView)
                    }
                }

                if(tile is CenterTile) {
                    for(i in 0 until tile.availableGems.size - 1) {
                        val gemView = TokenView(1, 1, gemSize, gemSize, ImageVisual(sapphireImage))
                        var gemX = hexagonWidth / 2
                        var gemY = hexagonHeight / 2

                        val offSets = mutableListOf(
                            Pair(0, -hexagonHeight / 4),
                            Pair(hexagonWidth / 4, -hexagonHeight / 8))

                    }
                }
                outerArea.add(area)
                tileMap.add(tile, area)
            }
        }
    }
}