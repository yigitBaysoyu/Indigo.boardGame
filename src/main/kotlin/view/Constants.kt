package view

import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Object that holds Constant values which are used in different other classes.
 */
object Constants {
    // COLORS
    private val backgroundColor: Color = Color.decode("#2a3b8e")
    val sceneBackgroundColorVisual = ColorVisual(backgroundColor.red, backgroundColor.green, backgroundColor.blue)
    const val BUTTON_BACKGROUND_COLOR = "#211c4f"

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
    val hoverTintImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/HoverTintImage.png"))
    val turnIndicator: BufferedImage = ImageIO.read(Constants::class.java.getResource("/PlayerTurnIndicator.png"))

    val amberImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Amber.png"))
    val emeraldImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Emerald.png"))
    val sapphireImage: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Sapphire.png"))

    val redGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TokenRed.png"))
    val blueGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TokenBlue.png"))
    val whiteGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TokenWhite.png"))
    val purpleGate: BufferedImage = ImageIO.read(Constants::class.java.getResource("/TokenPurple.png"))

    val gates: BufferedImage = ImageIO.read(Constants::class.java.getResource("/Gates.png"))
    val cornersBackground: BufferedImage = ImageIO.read(Constants::class.java.getResource("/CornersBackground.png"))

    val rotateIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/RotateIcon.png"))
    val aiIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/AIIcon.png"))
    val networkIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/NetworkIcon.png"))
    val randomIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/RandomIcon.png"))
    val menuAreaArrow: BufferedImage = ImageIO.read(Constants::class.java.getResource("/MenuAreaArrow.png"))

    val modeIconPlayer: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconPlayer.png"))
    val modeIconRandom: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconRandom.png"))
    val modeIconAI: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconAI.png"))
    val modeIconNetwork: BufferedImage = ImageIO.read(Constants::class.java.getResource("/ModeIconNetwork.png"))

    val plusIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/PlusIcon.png"))
    val minusIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/MinusIcon.png"))
    val warningIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/WarningIcon.png"))

    val wonIcon: BufferedImage = ImageIO.read(Constants::class.java.getResource("/WonIcon.png"))

    val gameModeIcon2: BufferedImage = ImageIO.read(Constants::class.java.getResource("/gameMode_2_not_shared.png"))
    val gameModeIcon3NotShared: BufferedImage = ImageIO.read(Constants::class.java.getResource("/gameMode_3_not_shared.png"))
    val gameModeIcon3Shared: BufferedImage = ImageIO.read(Constants::class.java.getResource("/gameMode_3_shared.png"))
    val gameModeIcon4: BufferedImage = ImageIO.read(Constants::class.java.getResource("/gameMode_4_shared.png"))
}