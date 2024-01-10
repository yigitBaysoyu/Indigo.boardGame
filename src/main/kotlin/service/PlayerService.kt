package service

import entity.*

/**
 * Handles the functionality of the player moves during the game.
 * Actions of the PlayerService influence the game and thus can end it.
 *
 * @param[rootService] Reference to the RootService to enable access to all layers of the programm
 */
class PlayerService (private  val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Function to rotate the tile in the current players hand.
     *
     * This function updates the rotationOffset and connections of the provided PathTile.
     * Each call to this method rotates the tile by 60 degrees clockwise.
     */
    fun rotateTile() {
        val game = rootService.currentGame
        checkNotNull(game) {"game is null"}

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

        onAllRefreshables { refreshAfterTileRotated() }
    }

    /**
     * Reverts the last move made in the game.
     *
     * This function checks if there are any moves to undo in the game's undo stack. If so, it retrieves the last move,
     * reverses the score changes recorded for each player, and resets gem movements to their original state.
     * In the case of a collision, it clears all gems from the end tile. Otherwise, it restores the gem to its start tile.
     * The function updates the active player ID and adds the reverted move to the redo stack.
     * If there are no moves left to undo, it prints a message indicating so.
     */
    fun undo() {
        val game = rootService.currentGame
        checkNotNull(game)
        if(game.undoStack.isEmpty()) return

        val lastTurn = game.undoStack.removeLast()

        //change the scores
        lastTurn.scoreChanges.forEachIndexed { index, score ->
            game.playerList[index].score -= score
        }

        //When the stones are moved back to their original position
        lastTurn.gemMovements.forEach { movement ->
            val startTileX = movement.startTile.xCoordinate
            val startTileY = movement.startTile.yCoordinate
            val endTileX = movement.endTile.xCoordinate
            val endTileY = movement.endTile.yCoordinate
            val positionOnStartTile = movement.positionOnStartTile
            val positionOnEndTile = movement.positionOnEndTile
            val gemType = movement.gemType

            val startTile = rootService.gameService.getTileFromAxialCoordinates(startTileX, startTileY)
            val endTile = rootService.gameService.getTileFromAxialCoordinates(endTileX, endTileY)

            when (startTile) {
                is PathTile -> startTile.gemPositions[positionOnStartTile] = gemType
                is CenterTile -> {
                    if(gemType == GemType.SAPPHIRE) startTile.availableGems.addFirst(gemType)
                    else startTile.availableGems.add(gemType)
                }
                is TreasureTile -> startTile.gemPositions[positionOnStartTile] = gemType
                else -> 1+1 // Placeholder, do nothing
            }

            if(!movement.didCollide) {
                when (endTile) {
                    is PathTile -> endTile.gemPositions[positionOnEndTile] = GemType.NONE
                    is GateTile -> endTile.gemsCollected.remove(movement.gemType)
                    is TreasureTile -> endTile.gemPositions[positionOnEndTile] = GemType.NONE
                    else -> 1+1 // Placeholder, do nothing
                }
            }
        }

        // If the player has tiles in hand, return the last tile to the draw pile
        val playerWhoPlacedTile = game.playerList[lastTurn.playerID]
        if (playerWhoPlacedTile.playHand.isNotEmpty()) {
            val tileToReturn = playerWhoPlacedTile.playHand.removeLast()
            game.drawPile.add(tileToReturn)
        }

        // Remove placed tile from board
        val newEmptyTile = EmptyTile(
            connections = mapOf(),
            rotationOffset = 0,
            xCoordinate = lastTurn.placedTile.xCoordinate,
            yCoordinate = lastTurn.placedTile.yCoordinate,
        )
        rootService.gameService.setTileFromAxialCoordinates(
            lastTurn.placedTile.xCoordinate,
            lastTurn.placedTile.yCoordinate,
            newEmptyTile
        )

        // Return placed tile to players hand
        playerWhoPlacedTile.playHand.add(lastTurn.placedTile)

        // Set active player
        game.activePlayerID = lastTurn.playerID

        game.redoStack.add(lastTurn)
        onAllRefreshables { refreshAfterUndo(lastTurn) }
    }

    fun placeTile(xCoordinate: Int, yCoordinate: Int){
        val game = rootService.currentGame
        checkNotNull(game)

        val tileFromPlayer = game.playerList[game.activePlayerID].playHand.first()
        val gemsOnTile = mutableListOf<GemType>()

        for (i in 0 .. 5) gemsOnTile.add(GemType.NONE)

        // new Tile because Coordinates are values
        val tileToBePlaced= PathTile(tileFromPlayer.connections,tileFromPlayer.rotationOffset,xCoordinate,yCoordinate,gemsOnTile)

        if(!rootService.gameService.isPlaceAble(xCoordinate,yCoordinate,tileToBePlaced)) return

        onAllRefreshables { refreshAfterTilePlaced(tileToBePlaced) }

        // placing the Tile in the GameLayout and moving the Gems
        game.gameLayout[xCoordinate+5][yCoordinate+5]=tileToBePlaced
        val turn=Turn(game.activePlayerID,mutableListOf(0,0,0),tileToBePlaced)
        rootService.gameService.moveGems(turn)

        // Updates the PlayHand for the current Player and then switches the Player
        game.playerList[game.activePlayerID].playHand[0] = game.drawPile.removeLast()
        game.activePlayerID= (game.activePlayerID +1) % game.playerList.size

        game.redoStack.clear()
        game.undoStack.add(turn)

        rootService.gameService.checkIfGameEnded()
    }
}