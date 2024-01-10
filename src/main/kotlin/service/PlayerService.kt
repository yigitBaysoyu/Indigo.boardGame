
package service

import entity.*

/**
 * Handles the functionality of the player moves during the game.
 * Actions of the PlayerService influence the game and thus can end it.
 *
 * @param[rootService] Reference to the RootService to enable access to all layers of the programm
 */
class PlayerService (private  val rootService: RootService) : AbstractRefreshingService(){

    /**
     * Funktion to rotate a tile.
     *
     * This funktion updates the rotationOffset and connections of the provided PathTile.
     * Each call to this method rotates the tile by 60 degrees clockwise.
     *
     * @param tile the PathTIle to be rotated
     */
    fun rotateTile(tile : PathTile){

        // map to store the new Connections
        val newConnections = mutableMapOf<Int, Int>()

        // update the rotationOffset of the tile 1 = 60 grad
        tile.rotationOffset = (tile.rotationOffset +1 ) % 6

        tile.connections.forEach { (key , value) ->
            // update the keys und the values 1 = 60 grad
            val newKey = (key + 1) % 6
            val newValue = (value + 1) % 6
            //update the connection of each key to the new value
            newConnections[newKey] = newValue

        }
        // set the connections to the tile connections
        tile.connections = newConnections

    }
}
