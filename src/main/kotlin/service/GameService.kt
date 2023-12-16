package service
import entity.*
import java.io.BufferedReader
import java.io.InputStreamReader


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
            redostack, players, gatelist, drawpile, gamelayout
        )
        rootService.currentGame = game
        setGates(threePlayerVariant)

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

        // a list of positions that are blocked
        val blockedPlaces = setOf(
            Pair(0, 0),
            Pair(0, 4),
            Pair(4, 0),
            Pair(-4, 0),
            Pair(0, -4),
            Pair(4, -4),
            Pair(-4, 4)
        )

        val position = Pair(xCoordinate,yCoordinate)

        // if a player tried to place a tile on the blocked places should not accept
        if (blockedPlaces.contains(position))
        {
            return false
        }

        val adjacentTile = findAdjacentTiles(xCoordinate,yCoordinate, game)

        for ( i in adjacentTile.indices) {
            if (adjacentTile.contains(game.gateList[i]))
                tile.connections.forEach { (pathExit, pathEntry) ->
                    val connectingExit = game.gateList[i].connections[pathEntry]
                    if (connectingExit != null && game.gateList[i].connections.containsValue(pathExit)) {

                        return false
                    }
                }
        }
        return true
    }

    /**
     * Private funktion that can help to * Finds all tiles adjacent to a given position
     *
     * @param x The X coordinate where the tile is to be placed.
     * @param y The Y coordinate where the tile is to be placed.
     *
     */
    private fun findAdjacentTiles(x: Int, y: Int, game: IndigoGame): List<Tile> {

        val adjacentTile = mutableListOf<Tile>()

        val positions = setOf(
            Pair(x,y-1),
            Pair(x,y+1),
            Pair(x-1,y+1),
            Pair(x-1,y),
            Pair(x+1,y),
            Pair(x+1,y-1)
        )
        //TODO
        return listOf()


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

}
