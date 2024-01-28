package service

import entity.*
import java.lang.IndexOutOfBoundsException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.*

/**
 *  Service that handles the logic of the Turns made by AI.
 *  There are two types of AI moves, one made mit an AI and one that is random
 *  @param rootService The root service that holds the current state of the game.
 *
 */
class AIService(private val rootService: RootService) {

    private val possibleCoordinates: MutableList<Pair<Int,Int>> = mutableListOf()
    private var positiveScore : Int = 0
    private var negativeScore : Int = 0
    var isPaused: Boolean = false


    /**
     *  Function calculates and executes the next best move for the AI player.
     *  It utilizes the minimax function to evaluate and choose the move that
     *  maximizes the AI's position in the game.
     */
    suspend fun calculateNextTurn() {
        if(isPaused) return
        if(rootService.gameService.checkIfGameEnded()) return

        println("Starting calculation")
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.SMARTAI)

        initializePossibleCoordinates(currentGame)

        val possibleMoves = possibleMovesInGameState(currentGame)
        if(possibleMoves.isEmpty()){
            println("no possibleMoves ? ")
            randomNextTurn()
        }
        println("Total moves: ${possibleMoves.size}")

        var bestMove: Pair<Int, Pair<Int, Int>>
        val availableProcessors = Runtime.getRuntime().availableProcessors()

        var chunkedBy = possibleMoves.size / availableProcessors
        chunkedBy = 1
        if(chunkedBy == 0) chunkedBy = 1
        val dividedMoves = possibleMoves.chunked(chunkedBy)

        /*coroutineScope{
            //Map each chunk of moves to a thread and use Default Dispatcher to assure time limit is held
            val moves = dividedMoves.map { moves ->
                async(Dispatchers.Default){
                    calculateBestMove(moves)
                }
            }
            //Wait for all Threads to finish
            val result = moves.awaitAll()
            var bestResult = result.first()
            println("First result: ${bestResult.first}")
            for(i in 1 until result.size){
                //println("$i Result : ${result[i].first}")
                if(result[i].first > bestResult.first){
                    bestResult = result[i]
                }
            }
            println("bestscore: ${bestResult.first}")
            bestMove = bestResult.second
        }*/

        val bestResult = calculateBestMove(possibleMoves)
        bestMove = bestResult.second

        println("bestMove: $bestMove")
        // Execute the best move
        for (i in 1..bestMove.first) playerService.rotateTile(true)
        val x = bestMove.second.first
        val y = bestMove.second.second

        playerService.placeTile(x, y)

    }

    private fun calculateBestMove(possibleMoves: List<Pair<Int, Pair<Int, Int>>>):
            Pair<Int,Pair<Int, Pair<Int, Int>>> {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Pair<Int, Int>>? = null
        var moveCounter = 0

        val posScore : Int = positiveScore
        val negScore : Int = negativeScore

        val maxDuration: Duration = 10000.milliseconds
        val startTime = System.currentTimeMillis()
        for (move in possibleMoves) {
            moveCounter++
            val simGame: IndigoGame = currentGame.deepCopy()
            val x = move.second.first
            val y = move.second.second
            val rotation = move.first

            // Apply rotation
            for (i in 1..rotation) rotateTile(simGame)

            // Simulate placing the tile
            val newGame = placeTile(simGame, x, y)

            // Evaluate the move
            val score = minimax(
                newGame,
                depth = 2,
                initialAlpha = Int.MIN_VALUE,
                initialBeta = Int.MAX_VALUE,
                playerIndex = currentGame.activePlayerID,
                mainIndex = currentGame.activePlayerID,
                startTime = startTime,
                maxDuration = maxDuration
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(rotation, Pair(x, y))
                println("bestMove: ${Pair(rotation, Pair(x, y))}")
            }
            if((System.currentTimeMillis()-startTime).milliseconds > maxDuration){
                break
            }
        }

        positiveScore = posScore
        negativeScore = negScore

        println("Did $moveCounter out of ${possibleMoves.size} possible moves")
        checkNotNull(bestMove){"Value of bestMove: $bestMove"}
        return Pair(bestScore, bestMove)
    }

    /**
     *  The minimax function is a recursive algorithm used for optimizing the move of the AI player.
     *  Function evaluates the best possible move for the AI player in the game "Indigo",
     *  considering multiple players.
     *
     *  @param [game] The current state of the game.
     *  @param [depth] The depth of the search tree. A larger depth results in a more thorough search
     *  but requires more computation.
     *  @param [initialAlpha] The initial value of alpha for alpha-beta pruning. Typically set to Int.MIN_VALUE.
     *  @param [initialBeta] The initial value of beta for alpha-beta pruning. Typically set to Int.MAX_VALUE.
     *  @param [playerIndex] The index of the current player. This determines whether the function is
     *  maximizing or minimizing.
     *  @param [mainIndex] The index of the player minimax algorithm serves to.
     *
     *  @return The score of the best move found for the current player at the given depth.
     */
    private fun minimax(
        game: IndigoGame,
        depth: Int,
        initialAlpha: Int,
        initialBeta: Int,
        playerIndex: Int,
        mainIndex: Int,
        startTime: Long,
        maxDuration: Duration
    ): Int {
        if (depth == 0 || checkIfGameEnded(game)
            || (System.currentTimeMillis() - startTime).milliseconds > maxDuration)
        {
            return evaluateGameState(game, mainIndex)
        }

        var alpha = initialAlpha
        var beta = initialBeta

        if (playerIndex == mainIndex) {
            var maxEval = Int.MIN_VALUE
            for (move in possibleMovesInGameState(game)) {
                val simGame: IndigoGame = game.deepCopy()
                val x = move.second.first
                val y = move.second.second

                for(i in 1..move.first) rotateTile(simGame)

                val newGame = placeTile(simGame, x, y)
                val nextPlayerIndex = newGame.activePlayerID
                val evaluation = minimax(newGame, depth - 1, alpha,
                    beta, nextPlayerIndex, mainIndex, startTime, maxDuration)
                maxEval = max(maxEval, evaluation)
                alpha = max(alpha, evaluation)
                if (beta <= alpha) {
                    break
                }
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in possibleMovesInGameState(game)) {
                val simGame: IndigoGame = game.deepCopy()
                val x = move.second.first
                val y = move.second.second

                for(i in 1..move.first) rotateTile(simGame)

                val newGame = placeTile(simGame, x, y)
                val nextPlayerIndex = newGame.activePlayerID
                val evaluation = minimax(newGame, depth - 1, alpha, beta, nextPlayerIndex, mainIndex, startTime, maxDuration)
                minEval = min(minEval, evaluation)
                beta = min(beta, evaluation)
                if (beta <= alpha) {
                    break
                }
            }
            return minEval
        }
    }



    /**
     *  Function to get all possible moves in a game state of a given IndigoGame object.
     *
     *  @param [game] The IndigoGame object in which the function will be implemented
     *
     *  @return a list of Pair<Int, Pair<Int, Int>> which contains the possible rotations and tile coordinates
     */
    private fun possibleMovesInGameState(game: IndigoGame): List<Pair<Int, Pair<Int, Int>>> {

        val allPossibleMoves : MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()

        for(r in 1..6){

            rotateTile(game)
            val tile = game.playerList[game.activePlayerID].playHand[0]
            val rotation = tile.rotationOffset

            for(coordinate in possibleCoordinates){

                if(isPlaceAble(game, coordinate.first, coordinate.second, tile) &&
                    gemIsMoved(game,coordinate.first, coordinate.second))
                {
                    allPossibleMoves.add(Pair(rotation, coordinate))
                }

            }
        }
        return allPossibleMoves
    }

    /**
     *  Function to evaluate the game state of the IndigoGame it receives as parameter.
     *
     *  @param [game] The IndigoGame object in which the function will be implemented
     */
    private fun evaluateGameState(game : IndigoGame, mainPlayerIndex: Int) : Int {

        val gemMovements = game.undoStack.last().gemMovements
        val playerIndex = game.undoStack.last().playerID
        for(movement in gemMovements){
            val start = movement.startTile
            val end = movement.endTile
            val next = getAdjacentTileByConnection(game, end, movement.positionOnEndTile) ?: end

            if( minDistance(game.playerList[playerIndex], next) < minDistance(game.playerList[playerIndex], start)){

                val point = when(next){
                    is GateTile -> movement.gemType.toInt() * 10
                    else -> movement.gemType.toInt()
                }

                if( game.undoStack.last().playerID == mainPlayerIndex )
                    positiveScore += point
                else
                    negativeScore += point
            }

        }
        return positiveScore - negativeScore
    }


    private fun gemIsMoved(game: IndigoGame, x: Int, y: Int) : Boolean {

        val simGame : IndigoGame = game.deepCopy()
        val nextGame = placeTile(simGame, x, y)

        val gemMovements = nextGame.undoStack.last().gemMovements

        for(movement in gemMovements){

            val end = movement.endTile
            val next = getAdjacentTileByConnection(nextGame, end, movement.positionOnEndTile) ?: end

            if(minDistance(game.getActivePlayer(), next) <= minDistance(game.getActivePlayer(), end) )
                return true
        }
        return false

    }


    private fun minDistance(player : Player, tile: Tile) : Int {

        val pointA: Pair<Int, Int> = Pair(tile.xCoordinate, tile.yCoordinate)
        var minDistance: Int = Int.MAX_VALUE

        for(gate in player.gateList){
            val pointB = Pair(gate.xCoordinate, gate.yCoordinate)
            val distanceToGate = axialDistance(pointA, pointB)
            minDistance = min(minDistance, distanceToGate)
        }
        return minDistance

    }

    private fun axialDistance(a: Pair<Int,Int>, b: Pair<Int, Int>): Int{
        return (abs(a.first - b.first)
                + abs(a.first + a.second - b.first - b.second)
                + abs(a.second - b.second)) / 2
    }


    /**
     *  Function that initializes all possible tile coordinates on the game layout where
     *  the Tile of SMART AI can be placed.
     */
    private fun initializePossibleCoordinates( game: IndigoGame ){
        if (possibleCoordinates.isNotEmpty()){
            return
        }

        for (list in game.gameLayout) {
            for (tile in list) {
                // Check if the conditions are met
                if (tile is EmptyTile) {
                    possibleCoordinates.add(Pair(tile.xCoordinate,tile.yCoordinate))
                }
            }
        }
    }




    /**
     * Function to rotate the tile in the current players hand.
     *
     * @param [game] The IndigoGame object in which the function will be implemented
     *
     * This function updates the rotationOffset and connections of the provided PathTile.
     * Each call to this method rotates the tile by 60 degrees clockwise.
     */
    private fun rotateTile(game : IndigoGame) {
        val tile = game.playerList[game.activePlayerID].playHand[0]

        // map to store the new Connections
        val newConnections = mutableMapOf<Int, Int>()

        // update the rotationOffset of the tile 1 = 60 grad
        tile.rotationOffset = (tile.rotationOffset + 1) % 6

        tile.connections.forEach { (key, value) ->
            // update the keys und the values 1 = 60 grad
            val newKey = (key + 1) % 6
            val newValue = (value + 1) % 6
            //update the connection of each key to the new value
            newConnections[newKey] = newValue

        }
        // set the connections to the tile connections
        tile.connections = newConnections

    }

    /**
     *  Places the Tile on the hexagon Grid if no rules are broken and the tile is Empty
     *
     *  @param [game] The IndigoGame object in which the function will be implemented
     *  @param xCoordinate the x Coordinate, in the Axial System
     *  @param yCoordinate the y Coordinate, in the Axial System
     *
     *  @return the new IndigoGame variant after tile placement
     */
    private fun placeTile(game: IndigoGame, xCoordinate: Int, yCoordinate: Int): IndigoGame {

        val tileFromPlayer = game.playerList[game.activePlayerID].playHand.first()
        val gemsOnTile = mutableListOf<GemType>()

        for (i in 0..5) gemsOnTile.add(GemType.NONE)

        // new Tile because Coordinates are values
        val tileToBePlaced = PathTile(
            connections = tileFromPlayer.connections,
            rotationOffset = tileFromPlayer.rotationOffset,
            xCoordinate = xCoordinate, yCoordinate = yCoordinate,
            gemPositions = gemsOnTile,
            type = tileFromPlayer.type
        )

        if (!isPlaceAble(game, xCoordinate, yCoordinate, tileToBePlaced)) return game

        // placing the Tile in the GameLayout and moving the Gems
        setTileFromAxialCoordinates(game, xCoordinate, yCoordinate, tileToBePlaced)

        val scoreChanges = MutableList(game.playerList.size) { 0 }
        val turn = Turn(game.activePlayerID, scoreChanges, tileToBePlaced)
        moveGems(game, turn)

        // Updates the PlayHand for the current Player and then switches the Player
        if (game.drawPile.isNotEmpty()) {
            game.playerList[game.activePlayerID].playHand[0] = game.drawPile.removeFirst()
        } else {
            game.playerList[game.activePlayerID].playHand.clear()
        }
        game.activePlayerID = (game.activePlayerID + 1) % game.playerList.size

        // if placed tile has same properties as last on redoStack, remove one turn from redoStack
        if(game.redoStack.isNotEmpty()) {
            val lastFromRedoStack = game.redoStack.last()
            if(xCoordinate == lastFromRedoStack.first.first && yCoordinate == lastFromRedoStack.first.second
                && tileToBePlaced.rotationOffset == lastFromRedoStack.second) {
                game.redoStack.removeLast()
            } else { // else remove everything from redoStack
                game.redoStack.clear()
            }
        }

        game.undoStack.add(turn)

        return game

    }

    /**
     * This function checks whether the specified position is blocked or if placing.
     * The tile would result in an illegal connection with an adjacent GateTile.
     *
     * @param[game] The IndigoGame object in which the function will be implemented.
     * @param xCoordinate The X coordinate where the tile is to be placed.
     * @param yCoordinate The Y coordinate where the tile is to be placed.
     * @param tile The PathTile object to be placed.
     *
     * @return true if the tile can be placed, false otherwise.
     *
     */
    private fun isPlaceAble(game : IndigoGame, xCoordinate: Int, yCoordinate: Int, tile: PathTile): Boolean {

        if (xCoordinate < -4 || xCoordinate > 4) return false
        if (yCoordinate < -4 || yCoordinate > 4) return false
        if(!checkIfValidAxialCoordinates(xCoordinate, yCoordinate)) return false

        val adjacentTiles = findAdjacentTiles(game, xCoordinate, yCoordinate)

        val targetTile = getTileFromAxialCoordinates(game, xCoordinate, yCoordinate)
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
     * Function that checks whether all stones have been removed from the game field,
     * and if they have been removed, the game ends.
     */
    private fun checkIfGameEnded(game: IndigoGame) : Boolean {

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
     * Sets the Tile passed as argument at the specified Axial Coordinates.
     * Throws IndexOutOfBounds exception if Coordinates are out of bounds.
     */
    private fun setTileFromAxialCoordinates(game : IndigoGame, x: Int, y: Int, tile: Tile) {

        if (!checkIfValidAxialCoordinates(x, y)) {
            throw IndexOutOfBoundsException("Position ($x, $y) is out of Bounds for gameLayout.")
        }

        game.gameLayout[x + 5][y + 5] = tile
    }

    /**
     * Checks if Axial Coordinates are valid. Coordinates are invalid if they are out of bounds of the
     * gameLayout 2d List, and if they are not inside the hexagonal play area.
     */
    private fun checkIfValidAxialCoordinates(x: Int, y: Int): Boolean {
        if (x < -5 || x > 5) return false
        if (y < -5 || y > 5) return false
        if (x < 0 && y < -5 - x) return false
        if (x > 0 && y > 5 - x) return false
        return true
    }

    /**
     * Private function that can help to Find all tiles adjacent to a given position.
     *
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @param x The X coordinate where the tile is to be placed.
     * @param y The Y coordinate where the tile is to be placed.
     *
     * @return a list that contains all adjacent Tiles.
     *
     */
    private fun findAdjacentTiles(game : IndigoGame, x: Int, y: Int): List<Tile> {

        val adjacentTile = mutableListOf<Tile>()

        // Define the relative positions that would be adjacent in a hexagonal grid
        val positions = setOf(
            Pair(x, y - 1), Pair(x, y + 1),
            Pair(x - 1, y + 1), Pair(x - 1, y),
            Pair(x + 1, y), Pair(x + 1, y - 1)
        )
        //add each adjacent tile to a list
        positions.forEach { (first, second) ->
            adjacentTile.add(getTileFromAxialCoordinates(game, first, second))
        }
        return adjacentTile

    }

    /**
     * Returns the Tile at the specified Axial Coordinates.
     * Throws IndexOutOfBounds exception if Coordinates are out of bounds.
     */
    private fun getTileFromAxialCoordinates(game : IndigoGame, x: Int, y: Int): Tile {

        if (!checkIfValidAxialCoordinates(x, y)) {
            throw IndexOutOfBoundsException("Position ($x, $y) is out of Bounds for gameLayout.")
        }

        return game.gameLayout[x + 5][y + 5]
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
     * Function to get the neighbour according to the [connection]
     *
     * @param [tile] Represents current tile
     * @param [connection] Represents the connection at which the required
     * neighbour sits
     *
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @return's [Tile]? which sits at the [connection] of [tile]. Return's null
     * if [connection] is invalid or there is no neighbour to be found
     */
    private fun getAdjacentTileByConnection(game : IndigoGame, tile: Tile, connection: Int): Tile? {
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

        val neighbourTile = getTileFromAxialCoordinates(game, neighbourCoordinate.first, neighbourCoordinate.second)
        if(neighbourTile is EmptyTile || neighbourTile is InvisibleTile){
            return null
        }
        return neighbourTile
    }

    /**
     * Function to recursively search for the end position
     * of a gem.
     *
     * The Function recursively searches for the next neighbour to
     * traverse, if there is no next neighbour it returns current [tile]
     *
     * @param[game] The IndigoGame object in which the function will be implemented.
     *
     * @return The tile where the gem ends up.
     */
    private fun moveGems(game: IndigoGame, turn: Turn): Turn{
        val currentGame = rootService.currentGame
        checkNotNull(currentGame){"No active Game"}
        val tile = turn.placedTile

        //Getting neighbours according to connection
        val neighbours = mutableMapOf<Int, Tile>()
        for (i in 0 until 6) {
            val neighbourTile = getAdjacentTileByConnection(game, tile, i)
            if(neighbourTile != null){
                neighbours[i] = neighbourTile
            }
        }

        checkCollisions(tile, neighbours, turn, game)
        moveStonesToEndOfPath(tile, neighbours, turn, game)

        return turn
    }

    private fun moveStonesToEndOfPath(tile: PathTile, neighbours: MutableMap<Int, Tile>, turn: Turn, game: IndigoGame){
        var currentGem: GemType
        //Move all stones that are on my tile to the end of the respective path
        for(i in 0 until tile.gemPositions.size){
            if(tile.gemPositions[i] != GemType.NONE){
                currentGem = tile.gemPositions[i]
                val originTile = neighbours[tile.connections[i]]
                val originConnection = tile.connections[i]
                checkNotNull(originConnection)
                checkNotNull(originTile){"The Gem cannot come from a nonexistent tile"}

                val endPos = findEndPosition(tile, i, false, game)
                if(endPos.third){
                    println("originTile: $originTile x: ${originTile.xCoordinate} and y: ${originTile.yCoordinate}")
                    val trueOrigin = originTile.connections[(originConnection +3)%6]
                    checkNotNull(trueOrigin){"trueOrigin: $trueOrigin"}
                    handleComplexCollision(turn, originTile, trueOrigin, endPos, currentGem, game)
                }

                else if(endPos.first is GateTile){
                    val startCon = (originConnection+3) %6
                    scoringAction(
                        game, originTile, startCon, endPos.first, endPos.second, currentGem, turn)
                }
                else {
                    val startCon = (originConnection + 3) % 6
                    val gemMovement = GemMovement(
                        currentGem, originTile, startCon,endPos.first, endPos.second, false
                    )

                    turn.gemMovements.add(gemMovement)
                }
            }
        }
    }

    private fun handleComplexCollision(turn: Turn, originTile: Tile, originConnection: Int,
                                       endPos: Triple<Tile, Int, Boolean>, gem: GemType, game: IndigoGame){
        val collisionTile = endPos.first

        val collisionMovement1 = GemMovement(
            gem,
            originTile,
            originConnection,
            collisionTile,
            endPos.second,
            true
        )
        if(collisionTile !is TraverseAbleTile) throw Error("Error in moveGems")

        val secondStartConnection = endPos.first.connections[endPos.second]
        checkNotNull(secondStartConnection)

        val secondOriginTile = getAdjacentTileByConnection(game, endPos.first, secondStartConnection)
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
            originTile.gemPositions[originConnection] = GemType.NONE
        }

        turn.gemMovements.add(collisionMovement1)
        turn.gemMovements.add(collisionMovement2)
    }

    /**
     * Function to check for Collisions and move the gems if there are none (in a movement of size 1)
     */
    private fun checkCollisions(tile: PathTile, neighbours: MutableMap<Int, Tile>, turn: Turn, game: IndigoGame ){
        for(i in 0 until 6){
            val currentNeighbour = neighbours[i]
            val currentConnection = (i+3)%6
            if(currentNeighbour == null){
                continue
            }

            when(currentNeighbour){
                is PathTile ->  collisionCheck(game, tile, i, currentNeighbour, currentConnection, turn)
                is CenterTile -> collisionCheck(game, tile, i, currentNeighbour, turn)
                is GateTile -> collisionCheck(game, tile, i, currentNeighbour, turn)
                is TreasureTile -> collisionCheck(game, tile, i, currentNeighbour, currentConnection, turn)
                is EmptyTile -> 1+1 // do nothing
                is InvisibleTile -> 1+1 // do nothing
            }
        }
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
    private fun findEndPosition(tile: Tile, currentConnection: Int, collision: Boolean, game: IndigoGame)
                                :Triple<Tile, Int, Boolean>
    {
        if(collision){
            return Triple(tile, currentConnection, true)
        }

        val nextTile = getAdjacentTileByConnection(game, tile, currentConnection)

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
                        return findEndPosition(nextTile, collisionConnection, true, game)
                    }

                    val nextConnection = nextTile.connections[(currentConnection + 3) % 6]
                    checkNotNull(nextConnection)

                    nextTile.gemPositions[nextConnection] = gem

                    return findEndPosition(nextTile, nextConnection, false, game)
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
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [neighbourTile] sits
     * @param [neighbourTile] The neighbouring tile which sits at [currentConnection]
     * @param [neighbourConnection] The connection at which [placedTile] sits, from the
     * perspective of [neighbourTile]
     * @param [turn] The currently active [Turn]
     */
    private fun collisionCheck(game: IndigoGame
                               , placedTile: PathTile
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

            val originTile = getAdjacentTileByConnection(game, placedTile, originConnection)
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
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [centerTile] sits
     * @param [centerTile] The neighbouring tile which sits at [currentConnection]
     * @param [turn] The currently active [Turn]
     */
    private fun collisionCheck(game: IndigoGame
                               , placedTile: PathTile
                               , currentConnection: Int
                               , centerTile: CenterTile
                               , turn: Turn ){
        val gemOnPlacedTile = placedTile.gemPositions[currentConnection]

        if(gemOnPlacedTile != GemType.NONE){
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(game, placedTile, originConnection)
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
            centerTile.availableGems.removeLast()
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
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @param [placedTile] The tile which was placed in the current [turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [treasureTile] sits
     * @param [treasureTile] The neighbouring tile which sits at [currentConnection]
     * @param [treasureTileConnection] The corresponding connection on the [treasureTile]
     * @param [turn] The currently active [Turn]
     */

    private fun collisionCheck(game: IndigoGame
                               , placedTile: PathTile
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

            val originTile = getAdjacentTileByConnection(game, placedTile, originConnection)
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
     * @param [game] The IndigoGame object in which the function will be implemented.
     * @param[placedTile] The tile which was placed in the current [Turn]
     * @param [currentConnection] The connection on the [placedTile], at which the
     * [gateTile] sits
     * @param[gateTile] The Tile at the corresponding connection
     * @param[turn] The current [Turn]
     */
    private fun collisionCheck(game: IndigoGame,
                               placedTile: PathTile,
                               currentConnection: Int,
                               gateTile: GateTile,
                               turn: Turn
    ){
        val gemOnPlacedTile = placedTile.gemPositions[currentConnection]

        if(gemOnPlacedTile != GemType.NONE){
            val originConnection = placedTile.connections[currentConnection]
            checkNotNull(originConnection)

            val originTile = getAdjacentTileByConnection(game, placedTile, originConnection)
            checkNotNull(originTile)

            scoringAction(
                game,
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

    private fun scoringAction(game : IndigoGame,
                              startTile: Tile,
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

        for((index, player) in game.playerList.withIndex()){
            if(endTile in player.gateList){
                player.score += gem.toInt()
                player.amountOfGems++

                turn.scoreChanges[index] += gem.toInt()
            }
        }
    }



    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------




    private val placeableTiles: MutableList<Pair<Int,Int>> = mutableListOf()

    /**
     * Function to decide on a random move for the [PlayerType.RANDOMAI]
     * Also keeps track of the already placed tiles each time it is called,
     * in [placeableTiles]
     *
     * @throws [IllegalArgumentException] if the active player is not of
     * [PlayerType.RANDOMAI]
     */
    fun randomNextTurn() {
        if(isPaused) return
        if(rootService.gameService.checkIfGameEnded()) return

        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame

        checkNotNull(currentGame) { "game is null" }

        val player = currentGame.getActivePlayer()
        if(player.playHand.size == 0) return

        require(player.playerType == PlayerType.RANDOMAI || player.playerType == PlayerType.SMARTAI)

        initializePlaceableTiles()
        placeableTiles.shuffle()

        //Rotate the tile by a random amount
        val randomRotation = Random.nextInt(0, 6)
        for (i in 0 until randomRotation){
            playerService.rotateTile(true)
        }

        var selectedPos: Pair<Int,Int> ?= null
        var selectedTile: Tile ?
        while (placeableTiles.isNotEmpty()){
            selectedPos = placeableTiles.first()
            selectedTile = gameService.getTileFromAxialCoordinates(selectedPos.first, selectedPos.second)

            if(selectedTile !is EmptyTile) {
                placeableTiles.removeFirst()
                continue
            } else if(gameService.isPlaceAble(selectedPos.first, selectedPos.second, player.playHand.first())){
                break
            } else {
                // If isPlaceable returns false for an empty position it means that the tile blocks an exit
                // Then a rotation will always solve it given the existing tile types
                playerService.rotateTile()
                break
            }
        }

        if(placeableTiles.isEmpty()){
            return
        }
        checkNotNull(selectedPos)
        placeableTiles.removeFirst()

        //Turn object is created in placeTile
        playerService.placeTile(selectedPos.first, selectedPos.second)
    }

    private fun initializePlaceableTiles() {
        placeableTiles.clear()

        for (x in -4..4) {
            for (y in -4..4) {
                // Check if the conditions are met
                if (checkIfValidAxialCoordinates(x, y) && rootService.gameService.getTileFromAxialCoordinates(x, y) is EmptyTile) {
                    placeableTiles.add(Pair(x,y))
                }
            }
        }
    }
}
