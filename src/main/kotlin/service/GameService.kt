package service
import entity.*


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
     * This method assigns gates to each player
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

                if (i%2 == 0) {
                    val playerIndex = i % 3
                    game.playerList[playerIndex].gateList.add(game.gateList[i])
                }
                else {
                    when(i){
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

            for (i in 0 until game.gateList.size)
            {
               allGemsInGate += game.gateList[i].gemsCollected.size
            }

            if (allGemsInGate == 12)
            {
                //endGame
            }

        }

    /**
     * Placeholder for isPlaceable
     */
    fun isPlaceable(xCoordinate: Int, yCoordinate: Int, tile: PathTile): Boolean{
        return true
    }
}
