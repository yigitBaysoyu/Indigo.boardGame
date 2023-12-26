package service
import entity.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.IndexOutOfBoundsException
import kotlin.IllegalArgumentException

import java.nio.file.Path


/**
 * A service responsible for the game logic of a Indigo game. It manages the state
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
        val redoStack = ArrayDeque<Turn>()
        val gateList: MutableList<MutableList<GateTile>> = MutableList(6){ mutableListOf()}
        val drawPile: MutableList<PathTile> = loadTiles()
        val gameLayout: MutableList<MutableList<Tile>> = mutableListOf()

        val game = IndigoGame(
            0, simulationSpeed, isNetworkGame, undoStack,
            redoStack, players, gateList, drawPile, gameLayout
        )
        rootService.currentGame = game

        setDefaultGameLayout()
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
             rotationOffset = 0, xCoordinate = 0, yCoordinate = 4,
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
     * Private funktion that can help to Find all tiles adjacent to a given position.
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
        if (y == 4 && tile.connections[0] == 1 ) return false
        if (x == 4 && tile.connections[2] == 3 ) return false
        if (y == -4 && tile.connections[3] == 4 ) return false
        if (x == -4 && tile.connections[5] == 0 ) return false

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
     * Private Funktion that helps to assign gates to each player.
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
     * Private Funktion that helps to assign gates to three players.
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
     * Function that checks whether all stones have been removed from the game field,
     * and if they have been removed, the game ends.
     */
    fun checkIfGameEnded() {

        val game = rootService.currentGame
        checkNotNull(game)

        var allGemsInGate = 0

        for (i in 0 until game.gateList.size) {
            for (j in 0 until game.gateList[i].size) {
                allGemsInGate += game.gateList[i][j].gemsCollected.size
            }
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
                playingTiles.add(PathTile(map, 0, 0, 0, mutableListOf()))
            }
        }
        return playingTiles
    }

    /**
     * Loads the current Game from the saveGame.ser file, this is achieved through the kotlinx serializable interface,
     * where the text in the file will be decoded into a Game Object
     */
    fun loadGame() {
        val file = File("saveGame.ser")
        rootService.currentGame = Json.decodeFromString<IndigoGame>(file.readText())
        onAllRefreshables { refreshAfterStartNewGame() }
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
     * Checks if Axial Coordinates are valid. Coordinates are invalid if they are out of bounds of the gameLayout 2d List,
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
     * moves gems until of the path the gem is on.
     *
     * @param[turn] The turn in which the movement occurs
     * @param[tile] The tile that is placed in the turn
     *
     * @return A turn modified so that the movement of the gems
     * and collisions, if any happened, are represented by the turn
     */

    fun moveGems(turn: Turn, tile: PathTile): Turn{
        val currentGame = rootService.currentGame
        checkNotNull(currentGame){"No active Game"}

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
            //Still need to consider collision handling, mb giving each movegemTo method the
            //stone movement entity and letting them add collision handling
            //For each
            when(currentNeighbour){
                is PathTile ->  moveGemTo(tile, i, currentNeighbour, currentConnection)
                is CenterTile -> moveGemTo(currentNeighbour, currentConnection, tile, i)
                is GateTile -> moveGemTo(currentNeighbour, currentConnection, tile, i)
                is TreasureTile -> moveGemTo(currentNeighbour, currentConnection, tile, i)
            }
        }

        //Move all stones that are on my tile to the end of the respective path
        for(i in 0 until tile.gemPositions.size){
            if(tile.gemPositions[i] != GemType.NONE){
                val endPosition = findNextPosition(tile, i)
                //Create turn or stoneMovement entity with endPosition
            }
        }

        return turn
    }

    //TODO: INTERFACES BENUTZEN


    /**
     * Function to find the next position, of a gem,
     * on the correct neighbour according to the current connection
     * of the gem. If no next Tile is found the function ends
     *
     *
     */
    private fun findNextPosition(startTile: Tile, startConnection: Int): Tile{
        val nextTile = getAdjacentTileByConnection(startTile, startConnection)

        //Both large blocks do basically the same but cant access gemPositions if I just check for both
        if(startTile is PathTile){
            when(nextTile){
                //Increase score action
                is GateTile -> nextTile.gemsCollected.add(startTile.gemPositions.removeAt(startConnection))
                is PathTile -> {
                    val gem = startTile.gemPositions.removeAt(startConnection)
                    val nextConnection = nextTile.connections[(startConnection + 3) % 6]
                    checkNotNull(nextConnection)
                    nextTile.gemPositions[nextConnection] = gem

                    return findNextPosition(nextTile, nextConnection)
                }
                is TreasureTile -> {
                    val gem = startTile.gemPositions.removeAt(startConnection)
                    val nextConnection = nextTile.connections[(startConnection + 3) % 6]
                    checkNotNull(nextConnection)
                    nextTile.gemPositions[nextConnection] = gem

                    return findNextPosition(nextTile, nextConnection)
                }
                else -> return startTile
            }
        }

        if(startTile is TreasureTile){
            when(nextTile){
                //Increase score action
                is GateTile -> nextTile.gemsCollected.add(startTile.gemPositions.removeAt(startConnection))
                is PathTile -> {
                    val gem = startTile.gemPositions.removeAt(startConnection)
                    val nextConnection = nextTile.connections[(startConnection + 3) % 6]
                    checkNotNull(nextConnection)
                    nextTile.gemPositions[nextConnection] = gem

                    return findNextPosition(nextTile, nextConnection)
                }
                is TreasureTile -> {
                    val gem = startTile.gemPositions.removeAt(startConnection)
                    val nextConnection = nextTile.connections[(startConnection + 3) % 6]
                    checkNotNull(nextConnection)
                    nextTile.gemPositions[nextConnection] = gem

                    return findNextPosition(nextTile, nextConnection)
                }
                else -> return startTile
            }
        }
        return startTile
    }

    /**
     * Moves a gem from [start] to [end] or the other way around and
     * checks for collisions
     */
    private fun moveGemTo(start: PathTile, startConnection: Int, end: PathTile, endConnection: Int ){
        val gemAtStart = start.gemPositions[startConnection]
        val gemAtEnd = end.gemPositions[endConnection]


        if(gemAtStart != GemType.NONE && gemAtEnd != GemType.NONE){
            //Collision
            println("collision")
        }
        else if(gemAtStart != GemType.NONE){
            start.gemPositions[startConnection] = GemType.NONE

            val destination = end.connections[endConnection]
            checkNotNull(destination)
            end.gemPositions[destination] = gemAtStart
            findNextPosition(end, destination)
        }
        else if(gemAtEnd != GemType.NONE){
            end.gemPositions[endConnection] = GemType.NONE

            val destination = start.connections[startConnection]
            checkNotNull(destination)
            start.gemPositions[destination] = gemAtEnd
        }
    }

    /**
     * In the case of center tile and pathtile either there is a stone on the pathtile
     * which means a collision will happen or the stone needs to be moved to the pathtile destination
     */
    private fun moveGemTo(start: CenterTile, startConnection: Int, end: PathTile, endConnection: Int ){
        val gemAtEnd = end.gemPositions[endConnection]

        if(gemAtEnd != GemType.NONE){
            //Collision
            println("collision")
        }
        else{
            val destination = end.connections[endConnection]
            checkNotNull(destination)
            end.gemPositions[destination] = start.availableGems.removeLast()
        }
    }

    private fun moveGemTo(start: TreasureTile, startConnection: Int, end: PathTile, endConnection: Int ){
        val gemAtStart = start.gemPositions[startConnection]
        val gemAtEnd = end.gemPositions[endConnection]

        if(gemAtStart != GemType.NONE && gemAtEnd != GemType.NONE){
            //Collision
            println("collision")
        }
        else if(gemAtStart != GemType.NONE){
            start.gemPositions[startConnection] = GemType.NONE

            val destination = end.connections[endConnection]
            checkNotNull(destination)
            end.gemPositions[destination] = gemAtStart
        }
        else if(gemAtEnd != GemType.NONE){
            end.gemPositions[endConnection] = GemType.NONE

            val destination = start.connections[startConnection]
            checkNotNull(destination)
            start.gemPositions[destination] = gemAtEnd
            findNextPosition(start, destination)
        }
    }

    private fun moveGemTo(start: GateTile, startConnection: Int, end: PathTile, endConnection: Int ){
        val gemAtEnd = end.gemPositions[endConnection]

        if(gemAtEnd != GemType.NONE){
            //Scoring action
            val currentGame = rootService.currentGame
            checkNotNull(currentGame)

            end.gemPositions[endConnection] = GemType.NONE
            start.gemsCollected.add(gemAtEnd)
            //Add points to both players TODO
        }
    }


    /**
     * Helper function to calculate the neighbour of a given connection
     */
    private fun getAdjacentTileByConnection(tile: Tile, connection: Int): Tile? {
        val neighbourCoordinate: Pair<Int, Int> = when(connection){
            0 -> Pair(tile.xCoordinate+1, tile.yCoordinate-1)
            1 -> Pair(tile.xCoordinate+1, tile.yCoordinate)
            2 -> Pair(tile.xCoordinate, tile.yCoordinate+1)
            3 -> Pair(tile.xCoordinate-1, tile.yCoordinate+1)
            4 -> Pair(tile.xCoordinate-1, tile.yCoordinate)
            5 -> Pair(tile.xCoordinate, tile.yCoordinate-1)
            else -> Pair(5,5) //Doesnt pass valid Coordinates check
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

}
