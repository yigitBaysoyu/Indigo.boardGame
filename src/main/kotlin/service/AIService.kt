package service

import entity.*
import java.lang.IndexOutOfBoundsException
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class AIService(private val rootService: RootService) {

    private val possibleCoordinates: MutableList<Pair<Int,Int>> = mutableListOf()

    /**
     * Function to calculate and execute the best possible move for a SMARTAI player.
     */
    fun calculateNextTurn() {

        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        gameService.checkIfGameEnded()

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.SMARTAI)

        initializePossibleCoordinates(currentGame)

        val moves = tryPossibleMoves(currentGame, possibleCoordinates)

        val bestMove = moves.maxBy { it.first }

        val rotation = bestMove.second.first
        val x = bestMove.second.second.first
        val y = bestMove.second.second.second


        for(r in 1..rotation) playerService.rotateTile()
        playerService.placeTile(x, y)

    }


    /**
     * Function, which tries every possible move in the current game state to determine which move is the best.
     *
     * @param [game] IndigoGame Object, the function will be applied to.
     * @param [possibleCoordinates] possible coordinates the moves can be tried on.
     *
     * @return a Mutable list consists pairs of rotations and coordinates (possible moves).
     */
    private fun tryPossibleMoves(game : IndigoGame, possibleCoordinates : MutableList<Pair<Int,Int>> )
    : MutableList<Pair<Int,Pair<Int, Pair<Int, Int>>>> {

        val moves : MutableList<Pair<Int,Pair<Int, Pair<Int, Int>>>> = mutableListOf()
        val tile = game.getActivePlayer().playHand.first()

        for(rot in 1..6) {

            rotateTile(game)
            val rotation = tile.rotationOffset

            for(coordinate in possibleCoordinates) {

                val x = coordinate.first
                val y = coordinate.second

                if( isPlaceAble(game, coordinate.first, coordinate.second, tile) ) {
                    val simGame = game.deepCopy()
                    placeTile(simGame, coordinate.first, coordinate.second)

                    val score = evaluateGameState(simGame)
                    moves.add(Pair(score, Pair(rotation, Pair(x, y))))
                }
            }
        }
        return moves
    }

    /**
     *  Function to evaluate the current game state of the IndigoGame object given as the parameter.
     *  Uses simple heuristic evaluation statements to determine the score.
     *
     *  @param [game] IndigoGame Object, the function will be applied to.
     *  @return Int, heuristic sore of the game state.
     */
    private fun evaluateGameState(game: IndigoGame) : Int {

        var heuristicScore = 0

        if(game.undoStack.isNotEmpty()) {

            val previousTurn = game.undoStack.last()
            val player = game.playerList[previousTurn.playerID]

            for((index, scoreChange) in previousTurn.scoreChanges.withIndex()){
                if(index == previousTurn.playerID){
                    heuristicScore += scoreChange * 100
                }
                else {
                    heuristicScore -= scoreChange * 200
                }
            }

            val gemMovements = previousTurn.gemMovements
            if(gemMovements.isNotEmpty()){

                for(movement in gemMovements) {
                    val (startTile, endTile) = movement.run { startTile to endTile }
                    val nextTile = getAdjacentTileByConnection(game, endTile, movement.positionOnEndTile) ?: endTile

                    val endTileDistance = minDistance(player, endTile)
                    val startTileDistance = minDistance(player, startTile)
                    val nextTileDistance = minDistance(player, nextTile)

                    var improvementFactor = 0
                    if (endTileDistance < startTileDistance) improvementFactor += 1
                    if (nextTileDistance < endTileDistance) improvementFactor += 1
                    if (movement.didCollide) improvementFactor = -1

                    heuristicScore += (movement.gemType.toInt() * improvementFactor)
                }
            }

        }
        return heuristicScore
    }


    /**
     *  Function to calculate the minimum distance between a given tile and the gates of given player.
     *
     *  @param [player] Player object.
     *  @param [tile] Tile to check the distance of.
     */
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


    /**
     *  Function to calculate the distance between two pairs of coordinates.
     *  @param [a] Pair of x and y coordinates of point a .
     *  @param [b] Pair of x and y coordinates of point b .
     */
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
        rootService.gameService.checkIfGameEnded()
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
     * Function to initialize all gem Movements needed,
     * after a new tile is placed. Checks for collisions and
     * moves gems until the gem is at the end of its path.
     *
     * @param [game] The IndigoGame object in which the function will be implemented
     * @param[turn] The turn in which the movement occurs
     *
     * @return A turn modified so that the movement of the gems
     * and collisions, if any happened, are represented by the [GemMovement]
     * objects in [Turn.gemMovements]
     */
    private fun moveGems(game : IndigoGame, turn: Turn): Turn{

        val tile = turn.placedTile

        //Getting neighbours according to connection
        val neighbours = mutableMapOf<Int, Tile>()
        for (i in 0 until 6) {
            val neighbourTile = getAdjacentTileByConnection(game, tile, i)
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
                is PathTile ->  collisionCheck(game, tile, i, currentNeighbour, currentConnection, turn)
                is CenterTile -> collisionCheck(game, tile, i, currentNeighbour, turn)
                is GateTile -> collisionCheck(game,tile, i, currentNeighbour, turn)
                is TreasureTile -> collisionCheck(game, tile, i, currentNeighbour, currentConnection, turn)
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

                val endPos = findEndPosition(game, tile, i)
                if(endPos.first is GateTile){
                    scoringAction(
                        game,
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
     * @param[tile] current position of gem
     * @param[currentConnection] current position of the gem on [tile]
     *
     * @return The tile where the gem ends up.
     */
    private fun findEndPosition(game: IndigoGame, tile: Tile, currentConnection: Int): Pair<Tile, Int>{
        val nextTile = getAdjacentTileByConnection(game, tile, currentConnection)

        if(tile is TraverseAbleTile){
            when(nextTile){
                //Increase score action
                is GateTile ->{
                    nextTile.gemsCollected.add(tile.gemPositions[currentConnection])
                    tile.gemPositions[currentConnection] = GemType.NONE
                    return Pair(nextTile, (currentConnection+3)%6)
                }
                is TraverseAbleTile -> {
                    val gem: GemType = tile.gemPositions[currentConnection]
                    tile.gemPositions[currentConnection] = GemType.NONE

                    val nextConnection = nextTile.connections[(currentConnection + 3) % 6]
                    checkNotNull(nextConnection)

                    nextTile.gemPositions[nextConnection] = gem

                    return findEndPosition(game, nextTile, nextConnection)
                }
                else -> return Pair(tile, currentConnection)
            }
        }
        return Pair(tile, currentConnection)
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

    /**
     * Function to check if there was a scoring move directly after a tile is placed
     *
     * @param [game] The IndigoGame object in which the function will be implemented.
     */
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
        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame

        checkNotNull(currentGame)
        gameService.checkIfGameEnded()

        val player = currentGame.getActivePlayer()
        if(player.playHand.size == 0) return

        require(player.playerType == PlayerType.RANDOMAI)

        placeableTiles.shuffle()

        //Rotate the tile by a random amount
        val randomRotation = Random.nextInt(0, 6)
        for (i in 0 until randomRotation){
            playerService.rotateTile()
        }

        var selectedPos: Pair<Int,Int> ?= null
        var selectedTile: Tile ?
        while (placeableTiles.isNotEmpty()){
            selectedPos = placeableTiles.first()
            selectedTile = gameService.getTileFromAxialCoordinates(selectedPos.first, selectedPos.second)

            if(selectedTile is PathTile || selectedTile is TreasureTile || selectedTile is CenterTile){
                placeableTiles.removeFirst()
                continue
            }
            else if(gameService.isPlaceAble(selectedPos.first, selectedPos.second, player.playHand.first())){
                break
            }
            //If isPlaceable returns false for an empty position it means that the tile blocks an exit
            //Then a rotation will always solve it given the existing tile types
            else if(selectedTile is EmptyTile){
                playerService.rotateTile()
                continue
            }
            //Remove any positions which doesn't pass the other checks
            else{
                placeableTiles.removeFirst()
                continue
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

    private fun initializePlaceableTiles(){
        if (placeableTiles.isNotEmpty()){
            return
        }

        for (x in -4..4) {
            for (y in -4..4) {
                // Check if the conditions are met
                if ((x + y) in -4..4) {
                    placeableTiles.add(Pair(x,y))
                }
            }
        }
    }

    init {
        initializePlaceableTiles()
    }
}
