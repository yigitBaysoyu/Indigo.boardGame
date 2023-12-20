package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
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

/**
 * Displays the actual gameplay.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {

    private val sceneWidth = 1920
    private val halfWidth = sceneWidth / 2

    private val hexagonWidth = 360 / 3
    private val hexagonHeight = 416 / 3

    private val tileMap: BidirectionalMap<Tile, HexagonView> = BidirectionalMap()

    private val hexagonGrid: HexagonGrid<HexagonView> = HexagonGrid(
        posX = halfWidth - 60 + 375, posY = 1080 / 2 - 50,
        width = 800, height = 800,
        coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL
    )

    init {
        background = ColorVisual(44, 70, 127)

        addComponents(
            Label(
                width = 750, height = 75,
                posX = halfWidth - 750 / 2 - 550, posY =  50,
                text = "GameScene",
                font = Font(size = 65, fontWeight = Font.FontWeight.BOLD, color = Color(250, 250, 240)),
            ),
            hexagonGrid
        )
    }

    override fun refreshAfterStartNewGame() {
        // Get all different Tile images
        val pathTileImageList: MutableList<BufferedImage> = mutableListOf()
        for(i in 0 ..4) pathTileImageList.add(ImageIO.read(GameScene::class.java.getResource("/TileType$i.png")))
        val treasureTileImage : BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileTreasure.png"))
        val centerTileImage : BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileCenter.png"))
        val emptyTileImage : BufferedImage = ImageIO.read(GameScene::class.java.getResource("/TileEmpty.png"))

        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        for(x in -5 .. 5 ) {
            for(y in -5 .. 5) {
                if(!rootService.gameService.checkIfValidAxialCoordinates(x, y)) continue
                val tile = rootService.gameService.getTileFromAxialCoordinates(x, y)

                var image: BufferedImage = treasureTileImage.getSubimage(0, 0, 1, 1)
                val className = tile.javaClass.name

                if (className == "entity.CenterTile") image = centerTileImage
                if (className == "entity.EmptyTile") image = emptyTileImage
                if (className == "entity.TreasureTile") image = treasureTileImage
                if (className == "entity.PathTile") {
                    image = if (tile.connections[0] == 5) {
                                if (tile.connections[1] == 4) pathTileImageList[2]
                                else if (tile.connections[1] == 3) pathTileImageList[3]
                                else pathTileImageList[4]
                            } else if (tile.connections[0] == 2) pathTileImageList[0]
                            else pathTileImageList[1]
                }

                var visual: Visual = ImageVisual(image)
                //if(className == "entity.GateTile") visual = ColorVisual(Color.RED)
                val hexagonView = HexagonView(size = hexagonHeight / 2, visual = visual)

                hexagonView.rotate(60 * tile.rotationOffset)
                hexagonView.onMouseClicked = {
                    println("TODO: IMPLEMENT PLACE TILE")
                    val clickedTile = tileMap.backward(hexagonView)
                    println("${clickedTile.xCoordinate}, ${clickedTile.yCoordinate}")
                }

                tileMap.add(tile, hexagonView)
                if ( className != "entity.InvisibleTile") hexagonGrid[x, y] = hexagonView
            }
        }
    }
}