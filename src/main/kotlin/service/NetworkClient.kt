package service

import edu.udo.cs.sopra.ntf.*
import entity.PlayerType
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.*


/**
 * [BoardGameClient] implementation for network communication.
 * @param playerName name of the client
 * @param networkService the [NetworkService] to potentially forward received messages to.
 * @param secret A secret key for secure communication.
 */

class NetworkClient (playerName: String, host: String, secret: String, val networkService: NetworkService):
    BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null
    var playerType: PlayerType = PlayerType.LOCALPLAYER

    /**
     * Handles a [CreateGameResponse] received from the server. It waits for the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS] to handle network issues.
     * @throws IllegalStateException if the status is not success or if not currently awaiting a game creation response.
     */

    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.HOST_WAITING_FOR_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            if (response.status == CreateGameResponseStatus.SUCCESS) {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                sessionID = response.sessionID
                networkService.onAllRefreshables { sessionID?.let { refreshAfterSessionIDReceived(it) } }
            } else {
                if(response.status.toString() == "SESSION_WITH_ID_ALREADY_EXISTS") {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterSessionIDAlreadyExists() }
                } else {
                    disconnectAndError(response.status)
                }
            }
        }
    }


    /**
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            if (response.status == JoinGameResponseStatus.SUCCESS) {
                sessionID = response.sessionID
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
            } else {
                if(response.status.toString() == "INVALID_SESSION_ID") {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterSessionIDIsInvalid() }
                } else if(response.status.toString() == "PLAYER_NAME_ALREADY_TAKEN") {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterNameAlreadyTaken() }
                } else {
                    disconnectAndError(response.status)
                }
            }
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server. When number of players is
     * greater than 2, the connectionState in the netWorkService will be updated to [ConnectionState.WAITING_FOR_OPPONENTS_TURN ]
     * @throws IllegalStateException if not currently expecting any guests to join.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            val playerNames = networkService.ntfPlayerList.map { player -> player.name}.toMutableList()
            val isNameNotUnique = playerNames.contains(notification.sender)

            if (isNameNotUnique) {
                disconnectAndError("Player names are not unique!")
            }

            val maxPlayers = when (networkService.gameMode) {
                GameMode.TWO_NOT_SHARED_GATEWAYS -> 2
                GameMode.THREE_SHARED_GATEWAYS, GameMode.THREE_NOT_SHARED_GATEWAYS -> 3
                GameMode.FOUR_SHARED_GATEWAYS -> 4
            }

            if(playerNames.size < maxPlayers ) {
                playerNames.add(notification.sender)
                val newGuest = Player(notification.sender, PlayerColor.WHITE)
                networkService.ntfPlayerList.add(newGuest)
            } else {
                error("maximum number of players has been reached.")
            }

            networkService.onAllRefreshables { refreshAfterPlayerJoined(notification.sender) }

            if (playerNames.size == maxPlayers){
                // when lobby is full enable startButton
                networkService.onAllRefreshables { refreshAfterLastPlayerJoined() }
            }
        }
    }

    /**
     * Handle a [GameInitMessage] sent by the server.
     * @throws IllegalStateException when the player is not waiting for [GameInitMessage]
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: GameInitMessage, sender: String) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_INIT )
        { "Not waiting for initMessage." }

        BoardGameApplication.runOnGUIThread {
            networkService.startNewJoinedGame(message = message, playerType = playerType)
        }

    }

    /**
     * Is called when a networkPlayer placed a tile.
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onPlaceTileReceived(message: TilePlacedMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.tilePlacedMessage(message)
        }
    }

    /**
     * Is called when a player leaves.
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        BoardGameApplication.runOnGUIThread {
            networkService.ntfPlayerList.removeIf { it.name == notification.sender }
            networkService.onAllRefreshables { refreshAfterPlayerLeft(notification.sender) }
        }
    }

    /**
     * disconnects the client
     * @param message error message
     * @throws IllegalStateException with message
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

}