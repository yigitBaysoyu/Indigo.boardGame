package service
import entity.*
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.IndexOutOfBoundsException
import kotlin.IllegalArgumentException
import kotlin.system.measureTimeMillis


/**
 * A service responsible for the game logic of an Indigo game. It manages the state
 * and progression of the game, including starting new games, ending games.
 *
 * @param rootService The root service that holds the current state of the game.
 */
class GameService (private  val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Function to initializes and starts a new game with the given parameters.
     *
     * @param players A list of players in the game.
     * @param threePlayerVariant A boolean for two threePlayerVariant.
     * @param simulationSpeed The speed at which the game simulation runs.
     * @param isNetworkGame A boolean if the game is a network game.
     */
    fun startNewGame(
        players: MutableList<Player>,
        threePlayerVariant: Boolean,
        simulationSpeed: Double,
        isNetworkGame: Boolean
    ) {
        val undoStack = ArrayDeque<Turn>()
        val redoStack = ArrayDeque<Pair<Pair<Int,Int>,Int>>()
        val gateList: MutableList<MutableList<GateTile>> = MutableList(6){ mutableListOf()}
        val drawPile: MutableList<PathTile> = loadTiles()
        drawPile.shuffle()
        val gameLayout: MutableList<MutableList<Tile>> = mutableListOf()

        val game = IndigoGame(
            0, simulationSpeed, isNetworkGame, undoStack,
            redoStack, players, gateList, drawPile, gameLayout
        )
        rootService.currentGame = game
        if (!isNetworkGame) {
            for(player in players) {
                player.playHand.clear()
                player.playHand.add(drawPile.removeFirst())
            }
        }

        rootService.aiService.isPaused = false

        setDefaultGameLayout()
        setSimulationSpeed(simulationSpeed)
        setGates(threePlayerVariant)
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Only used as helper function in startNewGame. Fills the GameLayout with the correct tiles to start a new Game.
     */
    private fun setDefaultGameLayout() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null. No Game is currently running." }

        // Set everything to invisible Tile
        for (i in 0..10) {
            game.gameLayout.add(mutableListOf())
            for (j in 0..10) game.gameLayout[i].add(InvisibleTile())
        }

        for (x in -5..5) {
            for (y in -5..5) {
                // continue if coordinate is not in hexagonal play area
                if (!checkIfValidAxialCoordinates(x, y)) continue

                val distanceToCenter = (kotlin.math.abs(x) + kotlin.math.abs(x + y) + kotlin.math.abs(y)) / 2

                // Outer ring of tiles should be gateTiles. They all have distance 5 to center
                if (distanceToCenter == 5) {
                    val gateTile = GateTile(
                        connections = mutableMapOf(
                            Pair(0, 3), Pair(1, 4), Pair(2, 5),
                            Pair(3, 0), Pair(4, 1), Pair(5, 2)),
                        rotationOffset = 0,
                        gemsCollected = mutableListOf(),
                        xCoordinate = x, yCoordinate = y
                    )

                    setTileFromAxialCoordinates(x, y, gateTile)

                    addGatesToList(x, y, gateTile)

                } else {
                    setTileFromAxialCoordinates(
                        x, y, EmptyTile(
                            connections = mutableMapOf(),
                            rotationOffset = 0,
                            xCoordinate = x, yCoordinate = y
                        )
                    )
                }
            }
        }

        val centerTileGems = ArrayDeque<GemType>()
        centerTileGems.add(GemType.SAPPHIRE)
        for (i in 0..4) centerTileGems.add(GemType.EMERALD)

        val centerTile = CenterTile(
            connections = mutableMapOf(),
            rotationOffset = 0,
            xCoordinate = 0, yCoordinate = 0,
            availableGems = centerTileGems
        )
        setTileFromAxialCoordinates(0, 0, centerTile)

        setTreasureTiles()
    }

    /**
     * Only used as helper function in setDefaultGameLayout.
     * Fills the GameLayout with the correct treasureTiles to start a new Game.
     */
    private fun  setTreasureTiles(){
        val treasureTile1 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 0, xCoordinate = 4, yCoordinate = 0,
            gemPositions = mutableListOf(
                GemType.NONE, GemType.NONE, GemType.NONE,
                GemType.NONE, GemType.AMBER, GemType.NONE )
        )
        setTileFromAxialCoordinates(4, 0, treasureTile1)

        val treasureTile2 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 1, xCoordinate = 0, yCoordinate = 4,
            gemPositions = mutableListOf(
                GemType.NONE, GemType.NONE, GemType.NONE,
                GemType.NONE, GemType.NONE, GemType.AMBER )
        )
        rotateConnections(treasureTile2)
        setTileFromAxialCoordinates(0, 4, treasureTile2)

        val treasureTile3 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 2, xCoordinate = -4, yCoordinate = 4,
            gemPositions = mutableListOf(
                GemType.AMBER, GemType.NONE, GemType.NONE,
                GemType.NONE, GemType.NONE, GemType.NONE )
        )
        rotateConnections(treasureTile3)
        setTileFromAxialCoordinates(-4, 4, treasureTile3)

        val treasureTile4 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 3, xCoordinate = -4, yCoordinate = 0,
            gemPositions = mutableListOf(
                GemType.NONE, GemType.AMBER, GemType.NONE,
                GemType.NONE, GemType.NONE, GemType.NONE )
        )
        rotateConnections(treasureTile4)
        setTileFromAxialCoordinates(-4, 0, treasureTile4)

        val treasureTile5 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 4, xCoordinate = 0, yCoordinate = -4,
            gemPositions = mutableListOf(
                GemType.NONE, GemType.NONE, GemType.AMBER,
                GemType.NONE, GemType.NONE, GemType.NONE )
        )
        rotateConnections(treasureTile5)
        setTileFromAxialCoordinates(0, -4, treasureTile5)

        val treasureTile6 = TreasureTile(
            connections = mutableMapOf(Pair(3, 5), Pair(5, 3), Pair(4, 4)),
            rotationOffset = 5, xCoordinate = 4, yCoordinate = -4,
            gemPositions = mutableListOf(
                GemType.NONE, GemType.NONE, GemType.NONE,
                GemType.AMBER, GemType.NONE, GemType.NONE )
        )
        rotateConnections(treasureTile6)
        setTileFromAxialCoordinates(4, -4, treasureTile6)
    }

    /**
     * Rotates the connections of a treasure tile based on its rotation offset.
     * Only used as helper function for setDefaultGameLayout.
     */
    private fun rotateConnections(tile: TreasureTile) {
        for (i in 0 until tile.rotationOffset) {
            val newConnections = mutableMapOf<Int, Int>()
            tile.connections.forEach { (key, value) ->
                val newKey = (key + 1) % 6
                val newValue = (value + 1) % 6

                newConnections[newKey] = newValue
            }

            tile.connections = newConnections
        }
    }

    /**
     * This function checks whether the specified position is blocked or if placing.
     * The tile would result in an illegal connection with an adjacent GateTile.
     *
     * @param xCoordinate The X coordinate where the tile is to be placed.
     * @param yCoordinate The Y coordinate where the tile is to be placed.
     * @param tile The PathTile object to be placed.
     *
     * @return true if the tile can be placed, false otherwise.
     *
     */
    fun isPlaceAble(xCoordinate: Int, yCoordinate: Int, tile: PathTile): Boolean {

        val game = rootService.currentGame
        checkNotNull(game)

        if (xCoordinate < -4 || xCoordinate > 4) return false
        if (yCoordinate < -4 || yCoordinate > 4) return false
        if(!checkIfValidAxialCoordinates(xCoordinate, yCoordinate)) return false

        val adjacentTiles = findAdjacentTiles(xCoordinate, yCoordinate)

        val targetTile = getTileFromAxialCoordinates(xCoordinate, yCoordinate)
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
     * Private function that can help to Find all tiles adjacent to a given position.
     *
     * @param x The X coordinate where the tile is to be placed.
     * @param y The Y coordinate where the tile is to be placed.
     *
     * @return a list that contains all adjacent Tiles.
     *
     */
    private fun findAdjacentTiles(x: Int, y: Int): List<Tile> {

        val adjacentTile = mutableListOf<Tile>()

        // Define the relative positions that would be adjacent in a hexagonal grid
        val positions = setOf(
            Pair(x, y - 1), Pair(x, y + 1),
            Pair(x - 1, y + 1), Pair(x - 1, y),
            Pair(x + 1, y), Pair(x + 1, y - 1)
        )
        //add each adjacent tile to a list
        positions.forEach { (first, second) ->
            adjacentTile.add(getTileFromAxialCoordinates(first, second))
        }
        return adjacentTile

    }

    /**
     * Private Function that checks if placing a PathTile next to a GateTile would result in an illegal connection.
     *
     * If any of these connections would illegally connect to a GateTile.
     *
     * @param x The X coordinate of the placement position.
     * @param y The Y coordinate of the placement position.
     * @param tile The PathTile being placed.
     *
     * @return false if placing the tile would result in an illegal connection, true otherwise.
     */
    private fun adjacentGate(x: Int, y: Int, tile: PathTile): Boolean {
        // The tile with these conditions cannot be placed.
        if (x == 4 && tile.connections[0] == 1 ) return false
        if (y == 4 && tile.connections[2] == 3 ) return false
        if (x == -4 && tile.connections[3] == 4 ) return false
        if (y == -4 && tile.connections[5] == 0 ) return false

        val tilesPositions = listOf(
            //two Pairs that have a list of positions and a pair of connections
            Pair(listOf(Pair(1, 3), Pair(2, 2), Pair(3, 1)), Pair(1, 2)),
            Pair(listOf(Pair(-1, -3), Pair(-2, -2), Pair(-3, -1)), Pair(4, 5))
        )

        for ((positions, connection) in tilesPositions) {
            if (positions.contains(Pair(x, y)) && tile.connections[connection.first] == connection.second ) {
                return false
            }
        }

        return true
    }

    /**
     * Private function that helps to fill the gateList with gateTiles.
     * And to numbering the gateLists, also gateList[0] = gate 1 and gateList[1] = gate 2 and so on.
     *
     * @param x The X coordinate of the placement position.
     * @param y The Y coordinate of the placement position.
     * @param tile The GateTile being added.
     */
    private fun addGatesToList(x : Int, y : Int ,tile : GateTile) {

        val game = rootService.currentGame
        checkNotNull(game)

        val pair= Pair(x,y)

        val gateTilesPositions = listOf(
            // Gate 1 positions
            listOf(Pair(5, -1), Pair(5, -2), Pair(5, -3), Pair(5, -4)),
            // Gate 2 positions
            listOf(Pair(1, 4), Pair(2, 3), Pair(3, 2), Pair(4, 1)),
            // Gate 3 positions
            listOf(Pair(-1, 5), Pair(-2, 5), Pair(-3, 5), Pair(-4, 5)),
            // Gate 4 positions
            listOf(Pair(-5, 1), Pair(-5, 2), Pair(-5, 3), Pair(-5, 4)),
            // Gate 5 positions
            listOf(Pair(-1, -4), Pair(-2, -3), Pair(-3, -2), Pair(-4, -1)),
            // Gate 6 positions
            listOf(Pair(1, -5), Pair(2, -5), Pair(3, -5), Pair(4, -5))
        )

        for (i in gateTilesPositions.indices){
            if (gateTilesPositions[i].contains(pair)){
                game.gateList[i].add(tile)
                return
            }
        }
    }

    /**
     * Private function that helps to assign gates to each player.
     *
     * @param threePlayerVariant if false gates are alternated between players,
     * else each player has one exclusive gate and shares two with others.
     */
    private fun setGates(threePlayerVariant: Boolean) {
        val game = rootService.currentGame
        checkNotNull(game)

        if (game.playerList.size != 3 && threePlayerVariant) {
            throw IllegalArgumentException(
                "You can't choose threePlayerVariant because the number of players is " +
                        game.playerList.size
            )
        }

        when (game.playerList.size) {

            2 -> {
                // for three players, gates are alternated between players
                game.gateList.forEachIndexed { index, gateList ->
                    val playerIndex = index % 2
                    game.playerList[playerIndex].gateList.addAll(gateList)
                }
            }

            3 -> {
                setGatesForThree(threePlayerVariant)
            }

            4 -> {
                //with four players shares each player one Gate with the other players
                val positions = listOf(
                    Pair(0, 1), Pair(1, 2),
                    Pair(0, 3), Pair(3, 1),
                    Pair(2, 0), Pair(2, 3)
                )

                positions.forEachIndexed { index, (first, second) ->

                    game.playerList[first].gateList.addAll(game.gateList[index])
                    game.playerList[second].gateList.addAll(game.gateList[index])

                }
            }

            else -> throw IllegalArgumentException(" The Number of Players can be only 2,3 or 4")
        }
    }

    /**
     * Private function that helps to assign gates to three players.
     *
     * @param threeVariant if false gates are alternated between players,
     * else each player has one exclusive gate and shares two with others.
     */
    private fun setGatesForThree(threeVariant : Boolean){
        val game = rootService.currentGame
        checkNotNull(game)


        if (!threeVariant) {
            // for three players, gates are alternated between players
            game.gateList.forEachIndexed { index, gateList ->
                val playerIndex = index % 3
                game.playerList[playerIndex].gateList.addAll(gateList)
            }
        } else {
            // for three player each player has one exclusive gate and shares two with others
            game.gateList.forEachIndexed { index, gateList ->
                when (index) {
                    0, 2, 4 -> {
                        val playerIndex = index / 2
                        game.playerList[playerIndex].gateList.addAll(gateList)
                    }
                    1 -> {
                        game.playerList[0].gateList.addAll(gateList)
                        game.playerList[2].gateList.addAll(gateList)
                    }
                    3 -> {
                        game.playerList[1].gateList.addAll(gateList)
                        game.playerList[0].gateList.addAll(gateList)
                    }
                    5 -> {
                        game.playerList[2].gateList.addAll(gateList)
                        game.playerList[1].gateList.addAll(gateList)
                    }
                }
            }
        }
    }

    /**
     * Function that checks whether all stones have been removed from the game field
     */
    fun checkIfGameEnded(): Boolean {
        val game = rootService.currentGame
        checkNotNull(game)

        var allGemsRemoved = true
        var allTilesPlaced = true

        for (row in game.gameLayout){
            for(tile in row){
                when(tile){
                    is PathTile -> {
                        if (!tile.gemPositions.all{ it == GemType.NONE}) {
                            allGemsRemoved = false
                            break
                        }
                    }
                    is TreasureTile -> {
                        if (!tile.gemPositions.all{ it == GemType.NONE}) {
                            allGemsRemoved = false
                            break
                        }
                    }
                    is CenterTile ->{
                        if (!tile.availableGems.all{ it == GemType.NONE} || tile.availableGems.isNotEmpty()) {
                            allGemsRemoved = false
                            break
                        }
                    }
                    is EmptyTile -> {
                        allTilesPlaced =false
                    }
                    else -> 1 + 1 // do nothing
                }
            }
        }

        return allGemsRemoved || allTilesPlaced
    }

    /**
     * Function that checks whether all stones have been removed from the game field,
     * and if they have been removed, the game ends.
     */
    fun endGameIfEnded() {
        if(checkIfGameEnded()) {
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
        val lines = loadTilesCsv()
        val playingTiles: MutableList<PathTile> = mutableListOf()

        for (line in lines) {
            val splitLine = line.split(";")
            val map: MutableMap<Int, Int> = mutableMapOf()

            //Create connections map going both ways
            for (i in 2 until splitLine.size step 2) {
                map[(splitLine[i].toInt()+5)%6] = (splitLine[i + 1].toInt()+5)%6
                map[(splitLine[i + 1].toInt()+5)%6] = (splitLine[i].toInt()+5)%6
            }

            for (i in 0 until splitLine[1].toInt()) {
                playingTiles.add(PathTile(map, 0, 0, 0, mutableListOf(),
                    splitLine[0].toInt()))
            }
        }
        return playingTiles
    }

    /**
     * reads the given CSV File at /tiles.csv and returns an MutableList which split the
     * file into Strings
     * */
    fun loadTilesCsv(): MutableList<String> {
        val file = GameService::class.java.getResource("/tiles.csv")
        checkNotNull(file) { "No file in defined position" }

        val bufferedReader = BufferedReader(InputStreamReader(file.openStream()))
        val lines = bufferedReader.readLines().toMutableList()

        lines.removeAt(0)   //Remove header line

        return lines
    }
    /**
     * Loads the current Game from the saveGame.ser file, this is achieved through the kotlinx serializable interface,
     * where the text in the file will be decoded into a Game Object
     */
    fun loadGame() {
        val file = File("saveGame.ser")
        if(!file.exists()) {
            onAllRefreshables { refreshAfterFileNotFound() }
            return
        }
        rootService.currentGame = Json.decodeFromString<IndigoGame>(file.readText())
        val game = checkNotNull(rootService.currentGame) { "game is null" }

        // Set gates in Player.gateList to the right object
        for(player in game.playerList) {
            for(i in 0 until player.gateList.size) {
                val x = player.gateList[i].xCoordinate
                val y = player.gateList[i].yCoordinate
                val gateTileFromBoard = getTileFromAxialCoordinates(x, y)

                if(gateTileFromBoard !is GateTile) throw IllegalStateException("Gate Tile did not have right x and y!")

                player.gateList[i] = gateTileFromBoard
            }
        }

        // Set gates in IndigoGame.gateList to the right object
        for(gateList in game.gateList) {
            for(i in 0 until gateList.size) {
                val x = gateList[i].xCoordinate
                val y = gateList[i].yCoordinate
                val gateTileFromBoard = getTileFromAxialCoordinates(x, y)

                if(gateTileFromBoard !is GateTile) throw IllegalStateException("Gate Tile did not have right x and y!")

                gateList[i] = gateTileFromBoard
            }
        }

        onAllRefreshables { refreshAfterLoadGame() }
        onAllRefreshables { refreshAfterStartNewGame() }

        println(game.undoStack.last().gemMovements.last())
    }

    /**
     * Saves the current Game in the file "saveGame.ser", this is achieved with the kotlinx serializable,
     * by serializing the current Game Object, and every Object attached to it, and converting it to a String,
     * then saving that String in the .ser file
     */
    fun saveGame() {
        val file = File("saveGame.ser")
        file.writeText(Json.encodeToString(rootService.currentGame))
    }

    /**
     * Checks if Axial Coordinates are valid. Coordinates are invalid if they are out of bounds of the gameLayout
     * 2d List,
     * and if they are not inside the hexagonal play area.
     */
    fun checkIfValidAxialCoordinates(x: Int, y: Int): Boolean {
        if (x < -5 || x > 5) return false
        if (y < -5 || y > 5) return false
        if (x < 0 && y < -5 - x) return false
        if (x > 0 && y > 5 - x) return false
        return true
    }

    /**
     * Returns the Tile at the specified Axial Coordinates.
     * Throws IndexOutOfBounds exception if Coordinates are out of bounds.
     */
    fun getTileFromAxialCoordinates(x: Int, y: Int): Tile {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null. No Game is currently running." }

        if (!checkIfValidAxialCoordinates(x, y)) {
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

        if (!checkIfValidAxialCoordinates(x, y)) {
            throw IndexOutOfBoundsException("Position ($x, $y) is out of Bounds for gameLayout.")
        }

        game.gameLayout[x + 5][y + 5] = tile
    }

    /**
     * Function to initialize all gem Movements needed,
     * after a new tile is placed. Checks for collisions and
     * moves gems until the gem is at the end of its path.
     *
     * @param[turn] The turn in which the movement occurs
     *
     * @return A turn modified so that the movement of the gems
     * and collisions, if any happened, are represented by the [GemMovement]
     * objects in [Turn.gemMovements]
     */

    fun moveGems(turn: Turn): Turn{
        val currentGame = rootService.currentGame
        checkNotNull(currentGame){"No active Game"}
        val tile = turn.placedTile

        //Getting neighbours according to connection
        val neighbours = mutableMapOf<Int, Tile>()
        for (i in 0 until 6) {
            val neighbourTile = getAdjacentTileByConnection(tile, i)
            if(neighbourTile != null){
                neighbours[i] = neighbourTile
            }
        }

        //Moving gems of neighbours and checking for collisions
        for(i in 0 until 6){
            val currentNeighbour = neighbours[i]
            val currentConnection = (i+3)%6
            if(currentNeighbour == null){
                continue
            }

            when(currentNeighbour){
                is PathTile ->  collisionCheck(tile, i, currentNeighbour, currentConnection, turn)
                is CenterTile -> collisionCheck(tile, i, currentNeighbour, turn)
                is GateTile -> collisionCheck(tile, i, currentNeighbour, turn)
                is TreasureTile -> collisionCheck(tile, i, currentNeighbour, currentConnection, turn)
                is EmptyTile -> 1+1 // do nothing
                is InvisibleTile -> 1+1 // do nothing
            }
        }

        var currentGem: GemType
        //Move all stones that are on my tile to the end of the respective path
        for(i in 0 until tile.gemPositions.size){
            if(tile.gemPositions[i] != GemType.NONE){
                currentGem = tile.gemPositions[i]
                val originTile = neighbours[tile.connections[i]]
                val originConnection = tile.connections[i]
                checkNotNull(originConnection)
                checkNotNull(originTile){"The Gem cannot come from a nonexistent tile"}

                val endPos = findEndPosition(tile, i, false)
                if(endPos.third){
                    val collisionTile = endPos.first
                    val trueOrigin = originTile.connections[(originConnection +3)%6]
                    checkNotNull(trueOrigin)
                    val collisionMovement1 = GemMovement(
                        currentGem,
                        originTile,
                        trueOrigin,
                        collisionTile,
                        endPos.second,
                        true
                    )
                    if(collisionTile !is TraverseAbleTile) throw Error("Error in moveGems")

                    val secondStartConnection = endPos.first.connections[endPos.second]
                    checkNotNull(secondStartConnection)

                    val secondOriginTile = getAdjacentTileByConnection(endPos.first, secondStartConnection)
                    checkNotNull(secondOriginTile)

                    val collisionMovement2 = GemMovement(
                        collisionTile.gemPositions[endPos.second],
                        secondOriginTile,
                        (secondStartConnection +3) % 6,
                        collisionTile,
                        endPos.second,
                        true
                    )

                    collisionTile.gemPositions[endPos.second] = GemType.NONE
                    if(originTile is TraverseAbleTile){
                        originTile.gemPositions[trueOrigin] = GemType.NONE
                    }

                    turn.gemMovements.add(collisionMovement1)
                    turn.gemMovements.add(collisionMovement2)
                }

                else if(endPos.first is GateTile){
                    scoringAction(
                        originTile,
                        (originConnection+3)%6,
                        endPos.first,
                        endPos.second,
                        currentGem,
                        turn
                    )
                }
                else {
                    val gemMovement = GemMovement(
                        currentGem,
                        originTile,
                        (originConnection + 3) % 6,
                        endPos.first,
                        endPos.second,
                        false
                    )

                    turn.gemMovements.add(gemMovement)
                }
            }
        }
        return turn
    }

    /**
     * Function to recursively search for the end position
     * of a gem.
     *
     * The Function recursively searches for the next neighbour to
     * traverse, if there is no next neighbour it returns current [tile]
     *
     * @param[tile] current position of gem
     * @param[currentConnection] current position of the gem on [tile]
     *
     * @return The tile where the gem ends up.
     */
    private fun findEndPosition(tile: Tile, currentConnection: Int, collision: Boolean): Triple<Tile, Int, Boolean>{
        if(collision){
            return Triple(tile, currentConnection, true)
        }

        val nextTile = getAdjacentTileByConnection(tile, currentConnection)

        if(tile is TraverseAbleTile){
            when(nextTile){
                //Increase score action
                is GateTile ->{
                    nextTile.gemsCollected.add(tile.gemPositions[currentConnection])
                    tile.gemPositions[currentConnection] = GemType.NONE
                    return Triple(nextTile, (currentConnection+3)%6, false)
                }
                is TraverseAbleTile -> {
                    val gem: GemType = tile.gemPositions[currentConnection]
                    tile.gemPositions[currentConnection] = GemType.NONE

                    //Connection where a collision could occur
                    val collisionConnection = (currentConnection + 3)%6

                    if(nextTile.gemPositions[collisionConnection] != GemType.NONE){
                        return findEndPosition(nextTile, collisionConnection, true)
                    }

                    val nextConnection = nextTile.connections[(currentConnection + 3) % 6]
                    checkNotNull(nextConnection)

                    nextTile.gemPositions[nextConnection] = gem

                    return findEndPosition(nextTile, nextConnection, false)
                }
                else -> return Triple(tile, currentConnection, false)
            }
        }
        return Triple(tile, currentConnection, false)
    }

    /**
     * Function which focuses on checking whether there are collisions between
     * two [PathTile]'s, if there is no collision detected it moves the gem to the [placedTile].
     *
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [neighbourTile] sits
     * @param [neighbourTile] The neighbouring tile which sits at [currentConnection]
     * @param [neighbourConnection] The connection at which [placedTile] sits, from the
     * perspective of [neighbourTile]
     * @param [turn] The currently active [Turn]
     */
    private fun collisionCheck(placedTile: PathTile
                               , currentConnection: Int
                               , neighbourTile: PathTile
                               , neighbourConnection: Int
                               , turn: Turn )
    {
        val gemAtStart = placedTile.gemPositions[currentConnection]
        val gemAtEnd = neighbourTile.gemPositions[neighbourConnection]

        //Create GemMovements for colliding gems
        if(gemAtStart != GemType.NONE && gemAtEnd != GemType.NONE){
            //Get origin tile of the gem on the placedTile
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(placedTile, originConnection)
            checkNotNull(originTile)

            val collisionMovePlacedTile = GemMovement(
                gemAtStart,
                originTile,
                (originConnection+3)%6,
                placedTile,
                currentConnection,
                true
            )
            val collisionMoveEndTile = GemMovement(
                gemAtEnd,
                neighbourTile,
                neighbourConnection,
                placedTile,
                currentConnection,
                true
            )

            turn.gemMovements.add(collisionMovePlacedTile)
            turn.gemMovements.add(collisionMoveEndTile)

            placedTile.gemPositions[currentConnection] = GemType.NONE
            neighbourTile.gemPositions[neighbourConnection] = GemType.NONE
        }
        else if(gemAtEnd != GemType.NONE){
            neighbourTile.gemPositions[neighbourConnection] = GemType.NONE

            val destination = placedTile.connections[currentConnection]
            checkNotNull(destination)
            placedTile.gemPositions[destination] = gemAtEnd
        }
    }

    /**
     * Function which focuses on checking whether there are collisions between
     * a [PathTile] and a [CenterTile], if there is no collision detected it moves the gem to the [placedTile].
     * It is important to note, that a [CenterTile] has a Gem for every [Tile] that is placed at a connection
     *
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [centerTile] sits
     * @param [centerTile] The neighbouring tile which sits at [currentConnection]
     * @param [turn] The currently active [Turn]
     */
    private fun collisionCheck(placedTile: PathTile, currentConnection: Int, centerTile: CenterTile, turn: Turn ){
        val gemOnPlacedTile = placedTile.gemPositions[currentConnection]

        if(gemOnPlacedTile != GemType.NONE){
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(placedTile, originConnection)
            checkNotNull(originTile)

            val collisionPathTile = GemMovement(
                gemOnPlacedTile,
                originTile,
                (originConnection + 3) % 6,
                placedTile,
                currentConnection,
                true
            )

            val collisionCenterTile = GemMovement(
                centerTile.availableGems.last(),
                centerTile,
                (currentConnection + 3) % 6,
                placedTile,
                currentConnection,
                true
            )

            turn.gemMovements.add(collisionPathTile)
            turn.gemMovements.add(collisionCenterTile)

            placedTile.gemPositions[currentConnection] = GemType.NONE
            centerTile.availableGems.removeFirst()
        }
        else{
            val destination = placedTile.connections[currentConnection]
            checkNotNull(destination)
            placedTile.gemPositions[destination] = centerTile.availableGems.removeLast()
        }
    }

    /**
     * Function which focuses on checking whether there are collisions between
     * a [PathTile] and a [TreasureTile], if there is no collision detected it moves the gem to the [placedTile].
     *
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [treasureTile] sits
     * @param [treasureTile] The neighbouring tile which sits at [currentConnection]
     * @param [treasureTileConnection] The corresponding connection on the [treasureTile]
     * @param [turn] The currently active [Turn]
     */

    private fun collisionCheck(placedTile: PathTile
                               , currentConnection: Int
                               , treasureTile: TreasureTile
                               , treasureTileConnection: Int
                               , turn: Turn )
    {
        val gemAtTreasureTile = treasureTile.gemPositions[treasureTileConnection]
        val gemAtPlacedTile = placedTile.gemPositions[currentConnection]

        //Create collision report
        if(gemAtTreasureTile != GemType.NONE && gemAtPlacedTile != GemType.NONE){
            //Find origin of gem on placedTile
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(placedTile, originConnection)
            checkNotNull(originTile)

            val collisionPathTile = GemMovement(
                gemAtTreasureTile,
                originTile,
                (originConnection + 3) % 6,
                placedTile,
                currentConnection,
                true
            )

            val collisionTreasureTile = GemMovement(
                gemAtPlacedTile,
                treasureTile,
                treasureTileConnection,
                placedTile,
                currentConnection,
                true
            )

            turn.gemMovements.add(collisionPathTile)
            turn.gemMovements.add(collisionTreasureTile)

            placedTile.gemPositions[currentConnection] = GemType.NONE
            treasureTile.gemPositions[treasureTileConnection] = GemType.NONE
        }

        else if(gemAtTreasureTile != GemType.NONE){
            treasureTile.gemPositions[treasureTileConnection] = GemType.NONE

            val destination = placedTile.connections[currentConnection]
            checkNotNull(destination)
            placedTile.gemPositions[destination] = gemAtTreasureTile
        }
    }

    /**
     * Function to check if there was a scoring move directly after a tile is placed
     *
     * @param[placedTile] The tile which was placed in the current [Turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [gateTile] sits
     * @param[gateTile] The Tile at the corresponding connection
     * @param[turn] The current [Turn]
     */
    private fun collisionCheck(placedTile: PathTile, currentConnection: Int, gateTile: GateTile, turn: Turn ){
        val gemOnPlacedTile = placedTile.gemPositions[currentConnection]

        if(gemOnPlacedTile != GemType.NONE){
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(placedTile, originConnection)
            checkNotNull(originTile)

            scoringAction(
                originTile,
                (originConnection+3)%6,
                gateTile,
                (currentConnection+3)%6,
                gemOnPlacedTile,
                turn
            )

            placedTile.gemPositions[currentConnection] = GemType.NONE
            gateTile.gemsCollected.add(gemOnPlacedTile)
        }
    }


    /**
     * Function which creates a Gem movement representing the scoring move,
     * and adds the points according to the move and [gem]
     *
     * @param[startTile] The Tile where the scoring move started
     * @param[startConnection] The connection where the scoring move started
     * @param[endTile] The Tile where the scoring move ended
     * @param[endConnection] The connection where the scoring move ended
     * @param[gem] The gem which moved to a gate tile and thus scored points
     * according to [GemType.toInt]
     * @param[turn] The current [Turn]
     */
    private fun scoringAction(startTile: Tile,
                              startConnection: Int,
                              endTile: Tile,
                              endConnection: Int,
                              gem: GemType,
                              turn: Turn
    ){
        val scoringMovement = GemMovement(
            gem,
            startTile,
            startConnection,
            endTile,
            endConnection,
            false
        )
        turn.gemMovements.add(scoringMovement)

        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        for((index, player) in currentGame.playerList.withIndex()){
            if(endTile in player.gateList){
                player.score += gem.toInt()
                player.amountOfGems++

                turn.scoreChanges[index] += gem.toInt()
            }
        }
    }

    /**
     * Function to get the neighbour according to the [connection]
     *
     * @param [tile] Represents current tile
     * @param [connection] Represents the connection at which the required
     * neighbour sits
     *
     * @return's [Tile]? which sits at the [connection] of [tile]. Return's null
     * if [connection] is invalid or there is no neighbour to be found
     */
    private fun getAdjacentTileByConnection(tile: Tile, connection: Int): Tile? {
        val neighbourCoordinate: Pair<Int, Int> = when(connection){
            0 -> Pair(tile.xCoordinate+1, tile.yCoordinate-1)
            1 -> Pair(tile.xCoordinate+1, tile.yCoordinate)
            2 -> Pair(tile.xCoordinate, tile.yCoordinate+1)
            3 -> Pair(tile.xCoordinate-1, tile.yCoordinate+1)
            4 -> Pair(tile.xCoordinate-1, tile.yCoordinate)
            5 -> Pair(tile.xCoordinate, tile.yCoordinate-1)
            else -> return null
        }

        if(!checkIfValidAxialCoordinates(neighbourCoordinate.first, neighbourCoordinate.second)){
            return null
        }

        val neighbourTile = getTileFromAxialCoordinates(neighbourCoordinate.first, neighbourCoordinate.second)
        if(neighbourTile is EmptyTile || neighbourTile is InvisibleTile){
            return null
        }
        return neighbourTile
    }

    /**
     * Setter for simulationSpeed which allows values between 1 and 100.
     */
    fun setSimulationSpeed(speed: Double) {
        val game = rootService.currentGame
        checkNotNull(game)

        var newSpeed = speed
        if(newSpeed < 1) newSpeed = 1.0
        if(newSpeed > 100) newSpeed = 100.0
        game.simulationSpeed = newSpeed

        onAllRefreshables { refreshAfterSimulationSpeedChange(newSpeed) }
    }

    /**
     * Function which simply switches the player and allows
     * for additional features to be added in between turns
     */
    fun switchPlayer(){
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        when(currentGame.getActivePlayer().playerType){
            PlayerType.RANDOMAI -> {
                rootService.aiService.randomNextTurn()
            }
            PlayerType.SMARTAI -> {
                val timeTaken = measureTimeMillis {
                    runBlocking {
                        rootService.aiService.calculateNextTurn()
                    }
                }
                println("Took : ${timeTaken/1000} sec")
            }
            else -> return
        }
    }
}