package service

import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponse


/**
 * Enum to distinguish the different states that occur in networked games, in particular
 * during connection and game setup. Used in [NetworkService].
 */
enum class ConnectionState {

    /**
     * no connection active. initial state at the start of the program and after
     * an active connection was closed.
     */
    DISCONNECTED,

    /**
     * connected to server, but no game started or joined yet
     */
    CONNECTED,

    /**
     * hostGame request sent to server. waiting for confirmation (i.e. [CreateGameResponse])
     */
    HOST_WAITING_FOR_CONFIRMATION,

    /**
     * host game started. waiting for guest player to join
     */
    WAITING_FOR_GUESTS,

    /**
     * joinGame request sent to server. waiting for confirmation (i.e. [JoinGameResponse])
     */

    GUEST_WAITING_FOR_CONFIRMATION,

    /**
     * joined game as a guest and waiting for host to send init message (i.e. [IndigoGameInitMessage])
     */
    WAITING_FOR_INIT,

    /**
     * Game is running. It is my turn. (and potentially the opponent's as well,
     * as in games like War both players are active at once)
     */
    PLAYING_MY_TURN,

    /**
     * Game is running. I did my turn. Waiting for opponent to send their turn.
     */
    WAITING_FOR_OPPONENTS_TURN,

    /**
    *init received ready to start playing
    */
    READY_FOR_GAME,
     /**
    update connection state after game was initialized
     */
    GAME_STARTED
}
