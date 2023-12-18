package service
import entity.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IndexOutOfBoundsException


/**
 * A service responsible for the game logic of a Pyramid card game. It manages the state
 * and progression of the game, including starting new games, ending games, and switching players.
 *
 * @param rootService The root service that holds the current state of the game.
 */
class GameService (private  val rootService: RootService) : AbstractRefreshingService() {


    fun startNewGame(
        players: MutableList<Player>,
        threePlayerVariant: Boolean,
        simulationSpeed: Double,
        isNetworkGame: Boolean
    ) {
        val undostack = ArrayDeque<Turn>()
        val redostack = ArrayDeque<Turn>()
        val gatelist: MutableList<GateTile> = createGateTiles()
        val drawpile: MutableList<PathTile> = mutableListOf()
        val gamelayout: MutableList<MutableList<Tile>> = mutableListOf()

        for(i in 0 ..10) {
            gamelayout.add(mutableListOf())
            for(j in 0..10) gamelayout[i].add(InvisibleTile())
        }

        val game = IndigoGame(
            1, 1.0, false, undostack,
            redostack, players, gatelist, drawpile, gameLayout = gamelayout
        )
        rootService.currentGame = game
        setGates(threePlayerVariant)

        setDefaultGameLayout()
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Only used as helper function in startNewGame. Fills the GameLayout with the correct tiles to start a new Game.
     */
    private fun setDefaultGameLayout() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null. No Game is currently running." }

        for(x in -5 .. 5) {
            for(y in -5 .. 5) {
                if(!checkIfValidAxialCoordinates(x, y)) continue

                val distanceToCenter = (kotlin.math.abs(x) + kotlin.math.abs(x + y) + kotlin.math.abs(y)) / 2
                if(distanceToCenter == 5) {
                    setTileFromAxialCoordinates(x, y, GateTile(
                        connections = mutableMapOf(Pair(0, 3), Pair(1, 4), Pair(2, 5), Pair(3, 0), Pair(4, 1), Pair(5, 2)),
                        rotationOffset = 0,
                        gemsCollected = mutableListOf(),
                        xCoordinate = 0,
                        yCoordinate = 0
                    ))
                } else {
                    setTileFromAxialCoordinates(x, y, EmptyTile(
                        connections = mutableMapOf(),
                        rotationOffset = 0,
                        xCoordinate = 0,
                        yCoordinate = 0
                    ))
                }
            }
        }

        val centerTileGems = ArrayDeque<GemType>()
        centerTileGems.add(GemType.SAPPHIRE)
        for(i in 0 .. 4) centerTileGems.add(GemType.EMERALD)

        val centerTile = CenterTile(
            connections = mutableMapOf(),
            rotationOffset = 0,
            xCoordinate = 0,
            yCoordinate = 0,
            availableGems = centerTileGems
        )
        setTileFromAxialCoordinates(0, 0, centerTile)

        val treasureTile1 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 0,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.AMBER, GemType.NONE)
        )
        setTileFromAxialCoordinates(4, 0, treasureTile1)

        val treasureTile2 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 0,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.AMBER)
        )
        rotateConnections(treasureTile2)
        setTileFromAxialCoordinates(0, 4, treasureTile2)

        val treasureTile3 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 2,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile3)
        setTileFromAxialCoordinates(-4, 4, treasureTile3)

        val treasureTile4 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 3,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile4)
        setTileFromAxialCoordinates(-4, 0, treasureTile4)

        val treasureTile5 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 4,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile5)
        setTileFromAxialCoordinates(0, -4, treasureTile5)

        val treasureTile6 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 5,
            xCoordinate = 0,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.AMBER, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile6)
        setTileFromAxialCoordinates(4, -4, treasureTile6)
    }

    /**
     * Rotates the connections of a treasure tile based on its rotation offset.
     * Only used as helper function for setDefaultGameLayout
     */
    private fun rotateConnections(tile: TreasureTile) {
        for(i in 0 until tile.rotationOffset) {
            val newConnections = mutableMapOf<Int, Int>()
            tile.connections.forEach { (key , value) ->
                val newKey = (key + 1) % 6
                val newValue = (value + 1) % 6

                newConnections[newKey] = newValue
            }

            tile.connections = newConnections
        }
    }

    fun placeRotatedTile(tile: Tile, xCoordinate: Int, yCoordinate: Int) {

    }

    private fun placeTile(tile: Tile) {
        val game = rootService.currentGame
        checkNotNull(game)

    }

    private fun createGateTiles(): MutableList<GateTile> {
        val gateTiles = mutableListOf<GateTile>()

        for (i in 1..6) {
            val connections = mapOf<Int, Int>()
            val rotationOffset = 0
            val xCoordinate = 0
            val yCoordinate = 0
            val gemsCollected = mutableListOf<GemType>()

            gateTiles.add(GateTile(connections, rotationOffset, xCoordinate, yCoordinate, gemsCollected))
        }

        return gateTiles
    }

    /**
     * This method assigns gates to each player.
     */
    fun setGates(threePlayerVariant: Boolean) {

        val game = rootService.currentGame
        checkNotNull(game)

        val gateSize = game.gateList.size

        if (!threePlayerVariant) {
            for (i in 0 until gateSize) {

                val playerIndex = i % 2
                game.playerList[playerIndex].gateList.add(game.gateList[i])
            }
        } else {
            for (i in 0 until gateSize) {

                if (i % 2 == 0) {
                    val playerIndex = i % 3
                    game.playerList[playerIndex].gateList.add(game.gateList[i])
                } else {
                    when (i) {
                        1 -> {
                            game.playerList[0].gateList.add(game.gateList[i])
                            game.playerList[2].gateList.add(game.gateList[i])
                        }

                        3 -> {

                            game.playerList[1].gateList.add(game.gateList[i])
                            game.playerList[0].gateList.add(game.gateList[i])
                        }

                        5 -> {
                            game.playerList[2].gateList.add(game.gateList[i])
                            game.playerList[1].gateList.add(game.gateList[i])
                        }
                    }
                }

            }
        }
    }


    fun endGame() {

        val game = rootService.currentGame
        checkNotNull(game)

        var allGemsInGate = 0

        for (i in 0 until game.gateList.size) {
            allGemsInGate += game.gateList[i].gemsCollected.size
        }

        if (allGemsInGate == 12) {
            //endGame
        }

    }

    /**
     * Function to read the given csv File, which defines the different
     * types of tiles (connections and amount), and create the defined
     * amount of tiles of each type.
     * The csv File is expected in the resources Folder of the project and
     * follow following scheme for the header row:
     *
     * TypeID, Count, Path1Start, Path1End, Path2Start, Path2End, Path3Start, Path3End
     *
     * @return [MutableList]<[PathTile]> containing all created Tiles
     */
    private fun loadTiles(): MutableList<PathTile> {
        val file = GameService::class.java.getResource("/tiles.csv")
        checkNotNull(file) { "No file in defined position" }

        val bufferedReader = BufferedReader(InputStreamReader(file.openStream()))
        val lines = bufferedReader.readLines().toMutableList()

        lines.removeAt(0)   //Remove header line

        val playingTiles: MutableList<PathTile> = mutableListOf()

        for (line in lines) {
            val splitLine = line.split(";")
            val map: MutableMap<Int, Int> = mutableMapOf()

            //Create connections map going both ways
            for (i in 2 until splitLine.size step 2) {
                map[splitLine[i].toInt()] = splitLine[i + 1].toInt()
                map[splitLine[i + 1].toInt()] = splitLine[i].toInt()
            }

            for (i in 0 until splitLine[1].toInt()) {
                playingTiles.add(PathTile(map, 0, 0, 0, mutableListOf<GemType>()))
            }
        }
        return playingTiles
    }

    /**
     * Checks if Axial Coordinates are valid. Coordinates are invalid if they are out of bounds of the gameLayout 2d List,
     * and if they are not inside the hexagonal play area.
     */
    fun checkIfValidAxialCoordinates(x: Int, y: Int): Boolean {
        if (x < -5 || x > 5 || y < -5 || y > 5) return false
        if ((x < 0 && y < -5 - x) || (x > 0 && y > 5 - x)) return false
        return true
    }

    /**
     * Returns the Tile at the specified Axial Coordinates.
     * Throws IndexOutOfBounds exception if Coordinates are out of bounds.
     */
    fun getTileFromAxialCoordinates(x: Int, y: Int): Tile {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null. No Game is currently running." }

        if(!checkIfValidAxialCoordinates(x, y)) {
            throw IndexOutOfBoundsException("Position ($x, $y) is out of Bounds for gameLayout.")
        }

        return game.gameLayout[x + 5][y + 5]
    }

    /**
     * Sets the Tile passed as argument at the specified Axial Coordinates.
     * Throws IndexOutOfBounds exception if Coordinates are out of bounds.
     */
    fun setTileFromAxialCoordinates(x: Int, y: Int, tile: Tile) {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null. No Game is currently running." }

        if(!checkIfValidAxialCoordinates(x, y)) {
            throw IndexOutOfBoundsException("Position ($x, $y) is out of Bounds for gameLayout.")
        }

        game.gameLayout[x + 5][y + 5] = tile
    }
}
