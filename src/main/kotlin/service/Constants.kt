package service

import tools.aqua.bgw.visual.ColorVisual
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Object that holds Constant values which are used in different other classes.
 */
object Constants {
    // COLORS
    val sceneBackgroundColorVisual = ColorVisual(44, 70, 127)
    const val buttonBackgroundColor = "#211c4f"

    // Measurements
    const val SCENE_WIDTH = 1920
    const val SCENE_HEIGHT = 1080

    // Images
    val pathTileImageList: MutableList<BufferedImage> = mutableListOf(
        ImageIO.read(Constants::class.java.getResource("/TileType0.png")),
        ImageIO.read(Constants::class.java.getResource("/TileType1.png")),
        ImageIO.read(Constants::class.java.getResource("/TileType2.png")),
        ImageIO.read(Constants::class.java.getResource("/TileType3.png")),
        ImageIO.read(Constants::class.java.getResource("/TileType4.png")),
    )

    val treasureTileImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TileTreasure.png"))
    val centerTileImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TileCenter.png"))
    val emptyTileImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TileEmpty.png"))

    val amberImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Amber.png"))
    val emeraldImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Emerald.png"))
    val sapphireImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Sapphire.png"))

    val redGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/tokenRed.png"))
    val blueGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/tokenBlue.png"))
    val whiteGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/tokenWhite.png"))
    val purpleGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/tokenPurple.png"))

    val gates: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Gates.png"))
    val cornersBackground: BufferedImage = ImageIO.read(Constants::class.java.getResource("/CornersBackground.png"))

    val undoIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/UndoIcon.png"))
    val redoIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/RedoIcon.png"))
    val rotateIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/RotateIcon.png"))
    val aiIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/AIIcon.png"))
    val menuAreaArrow: BufferedImage = ImageIO.read(Constants::class.java.getResource("/MenuAreaArrow.png"))

    val modeIconPlayer: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconPlayer.png"))
    val modeIconRandom: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconRandom.png"))
    val modeIconAI: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconAI.png"))

    val plusIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/PlusIcon.png"))
}