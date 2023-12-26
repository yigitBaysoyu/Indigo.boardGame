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
     * Funktion to rotate a tile.
     *
     * This funktion updates the rotationOffset and connections of the provided PathTile.
     * Each call to this method rotates the tile by 60 degrees clockwise.
     *
     * @param tile the PathTIle to be rotated
     */
    fun rotateTile(tile: PathTile) {

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
        if (game.undoStack.isNotEmpty()) {
            val lastTurn = game.undoStack.removeLast()
            //change the score
            lastTurn.scoreChanges.forEachIndexed { index, score ->
                game.playerList[index].score -= score
            }
            //When the stones are moved back to their original position
            lastTurn.gemMovements.forEach { movement ->
                val endTile = game.gameLayout[movement.endTile.xCoordinate][movement.endTile.yCoordinate]
                val startTile = game.gameLayout[movement.startTile.xCoordinate][movement.startTile.yCoordinate]

                when (endTile) {
                    is PathTile -> endTile.gemPositions.remove(movement.gemType)
                    is GateTile -> endTile.gemsCollected.remove(movement.gemType)
                    is CenterTile -> { endTile.availableGems.remove(movement.gemType) }
                    is TreasureTile ->{endTile.gemPositions.remove(movement.gemType)}
                }

                when (startTile) {
                    is PathTile -> startTile.gemPositions.add(movement.gemType)
                    is GateTile -> startTile.gemsCollected.add(movement.gemType)
                    is CenterTile -> startTile.availableGems.add(movement.gemType)
                    is TreasureTile ->{ startTile.gemPositions.add(movement.gemType)}
            }
            }
            // Retrieve the player who made the last move
            val playerWhoPlacedTile = game.playerList[lastTurn.playerID]
            // If the player has tiles in hand, return the last tile to the draw pile
            if (playerWhoPlacedTile.playHand.isNotEmpty()) {
                val tileToReturn = playerWhoPlacedTile.playHand.removeLast()
                game.drawPile.add(tileToReturn)
            }

            game.activePlayerID = lastTurn.playerID
            game.redoStack.addFirst(lastTurn)
            onAllRefreshables { refreshAfterUndo(lastTurn) }
        } else {
            println("No moves to undo.")
        }
    }
}