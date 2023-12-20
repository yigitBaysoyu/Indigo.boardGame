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

        // Set everything to invisible Tile
        for(i in 0 ..10) {
            game.gameLayout.add(mutableListOf())
            for(j in 0..10) game.gameLayout[i].add(InvisibleTile())
        }

        for(x in -5 .. 5) {
            for(y in -5 .. 5) {
                // continue if coordinate is not in hexagonal play area
                if(!checkIfValidAxialCoordinates(x, y)) continue

                val distanceToCenter = (kotlin.math.abs(x) + kotlin.math.abs(x + y) + kotlin.math.abs(y)) / 2

                // Outer ring of tiles should be gateTiles. They all have distance 5 to center
                if(distanceToCenter == 5) {
                    setTileFromAxialCoordinates(x, y, GateTile(
                        connections = mutableMapOf(Pair(0, 3), Pair(1, 4), Pair(2, 5), Pair(3, 0), Pair(4, 1), Pair(5, 2)),
                        rotationOffset = 0,
                        gemsCollected = mutableListOf(),
                        xCoordinate = x,
                        yCoordinate = y
                    ))
                } else {
                    setTileFromAxialCoordinates(x, y, EmptyTile(
                        connections = mutableMapOf(),
                        rotationOffset = 0,
                        xCoordinate = x,
                        yCoordinate = y
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
            xCoordinate = 4,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.AMBER, GemType.NONE)
        )
        setTileFromAxialCoordinates(4, 0, treasureTile1)

        val treasureTile2 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 0,
            xCoordinate = 0,
            yCoordinate = 4,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.AMBER)
        )
        rotateConnections(treasureTile2)
        setTileFromAxialCoordinates(0, 4, treasureTile2)

        val treasureTile3 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 2,
            xCoordinate = -4,
            yCoordinate = 4,
            gemPositions = mutableListOf(GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile3)
        setTileFromAxialCoordinates(-4, 4, treasureTile3)

        val treasureTile4 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 3,
            xCoordinate = -4,
            yCoordinate = 0,
            gemPositions = mutableListOf(GemType.NONE, GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile4)
        setTileFromAxialCoordinates(-4, 0, treasureTile4)

        val treasureTile5 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 4,
            xCoordinate = 0,
            yCoordinate = -4,
            gemPositions = mutableListOf(GemType.NONE, GemType.NONE, GemType.AMBER, GemType.NONE, GemType.NONE, GemType.NONE)
        )
        rotateConnections(treasureTile5)
        setTileFromAxialCoordinates(0, -4, treasureTile5)

        val treasureTile6 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 5,
            xCoordinate = 4,
            yCoordinate = -4,
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

    /**
     * * This function checks whether the specified position is blocked or if placing
     * the tile would result in an illegal connection with an adjacent GateTile.
     *
     * @param xCoordinate The X coordinate where the tile is to be placed.
     * @param yCoordinate The Y coordinate where the tile is to be placed.
     * @param tile The PathTile object to be placed.
     *
     * @return true if the tile can be placed, false otherwise.
     *
     */
    fun isPlaceAble(xCoordinate: Int, yCoordinate: Int, tile: PathTile) : Boolean {

        val game = rootService.currentGame
        checkNotNull(game)

        val adjacentTiles = findAdjacentTiles(xCoordinate,yCoordinate)

        val targetTile = getTileFromAxialCoordinates(xCoordinate,yCoordinate)
        // Check if the targeted placement position is an EmptyTile, which means placement is allowed.
        if (targetTile is EmptyTile) {
            // If it's an EmptyTile, check for adjacent GateTiles.
            return if (adjacentTiles.none { it is GateTile }) {
                // If there are no adjacent GateTiles, placement is allowed.
                true
            } else {
                // If there is at least one adjacent GateTile, check for illegal connections.
                adjacentGate(xCoordinate, yCoordinate, tile)
            }
        }
        return false

    }

    /**
     * Private funktion that can help to Find all tiles adjacent to a given position.
     *
     * @param x The X coordinate where the tile is to be placed.
     * @param y The Y coordinate where the tile is to be placed.
     *
     * return a list that contains all adjacent Tiles.
     *
     */
    private fun findAdjacentTiles(x: Int, y: Int): List<Tile> {

        val adjacentTile = mutableListOf<Tile>()

        // Define the relative positions that would be adjacent in a hexagonal grid
        val positions = setOf(
            Pair(x,y-1), Pair(x,y+1),
            Pair(x-1,y+1), Pair(x-1,y),
            Pair(x+1,y), Pair(x+1,y-1)
        )
        //add each adjacent tile to a list
        positions.forEach{ (first , second) ->
            adjacentTile.add(getTileFromAxialCoordinates(first,second))
        }
        return adjacentTile

    }

    /**
     * Private Function that checks if placing a PathTile next to a GateTile would result in an illegal connection.
     *
     * if any of these connections would illegally connect to a GateTile.
     *
     * @param x The X coordinate of the placement position.
     * @param y The Y coordinate of the placement position.
     * @param tile The PathTile being placed.
     *
     * @return false if placing the tile would result in an illegal connection, true otherwise.
     */
    private fun adjacentGate(x: Int, y: Int, tile: PathTile): Boolean {
        // Check for a specific connection (0 to 1) when the y-coordinate is 4.
        if (x == 4 && (tile.connections[0] == 1 || tile.connections[1] == 0)) {
            return false
        }

        // Check for a connection (1 to 2) at specific positions.
        val positionsForConnection1to2 = listOf(Pair(1,3), Pair(2,2), Pair(3,1))
        if (positionsForConnection1to2.any { it.first == x && it.second == y } && (tile.connections[1] == 2
                    || tile.connections[2] == 1 ) ) {
            return false
        }

        // Check for a connection (4 to 5) at specific positions.
        val positionsForConnection4to5 = listOf(Pair(-1,-3), Pair(-2,-2), Pair(-3,-1))
        if (positionsForConnection4to5.any { it.first == x && it.second == y } && (tile.connections[4] == 5
                    || tile.connections[5] == 4)) {
            return false
        }

        // Check for a specific connection (2 to 3) when the x-coordinate is 4.
        if (y == 4 && (tile.connections[2] == 3 || tile.connections[3] == 2)) {
            return false
        }

        // Check for a specific connection (3 to 4) when the y-coordinate is -4.
        if (y == -4 && (tile.connections[3] == 4 ||tile.connections[4] == 3 )) {
            return false
        }

        // Check for a specific connection (5 to 0) when the x-coordinate is -4.
        if (x == -4 && (tile.connections[5] == 0 || tile.connections[0] == 5)) {
            return false
        }

        // If none of the above conditions are met, then the placement is legal.
        return true
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
        }
        else {
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
            onAllRefreshables { refreshAfterEndGame() }
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
