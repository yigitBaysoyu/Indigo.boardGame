package service
import entity.*
import service.message.*
import service.message.Player
import view.*
import java.lang.IllegalStateException
import service.GameService


/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games.
 */

class NetworkService (private  val rootService: RootService) : AbstractRefreshingService() {


    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    lateinit var playersList: MutableList<entity.Player>
    var threePlayerVariant: Boolean = false

    companion object {

        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Indigo"

    }

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
    var activePlayer: entity.Player? = rootService.currentGame?.getActivePlayer()
    val activePlayerName: String = activePlayer?.name.toString()
    var playersNameList: MutableList<String>? = null
    var simulationSpeed : Double = 0.0
    lateinit var playersListOnline : MutableList <service.message.Player>
    lateinit var gameMode: service.message.GameMode



    /**
     * Connects to server and creates a new game session.
     *
     * @param secret Server secret.
     * @param hostPlayerName Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */

    fun hostGame(secret: String,
                 sessionID: String?,
                 hostPlayerName: String,
                 simulationSpeed : Double,
                 gameMode: GameMode) {

        if (!connect(secret, hostPlayerName,PlayerType.NETWORKPLAYER)) {
            error("Connection failed")
        }else{
            print("Connection success!")
        }

        this.gameMode = gameMode

        this.simulationSpeed = setSimulationSpeed(speed = simulationSpeed)

        // set attributes. and add the host player name to the list
        this.playersNameList = mutableListOf(hostPlayerName)
        playersListOnline.add(Player(hostPlayerName, PlayerColor.RED))


        updateConnectionState(ConnectionState.CONNECTED)

        // create new game
        if (sessionID.isNullOrBlank())
            client?.createGame(GAME_ID, "Welcome!")
        else
            client?.createGame(GAME_ID, sessionID, "Welcome!")

        // update the connectionState after creating the game
        updateConnectionState(ConnectionState.HOST_WAITING_FOR_CONFIRMATION)
    }


    /**
     * Connect to server and join a game session as guest player.
     *
     * @param secret Server secret.
     * @param guestName name of the player wants to join
     * @param guestPlayerType player type of the player wants to join
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame (secret: String, sessionID: String, guestName: String,guestPlayerType:PlayerType) {

        if (!connect(secret, guestName, guestPlayerType))
            error("Connection failed")

        updateConnectionState(ConnectionState.CONNECTED)
        client?.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
    }





    fun startNewHostedGame() {

    }



    fun startNewJoinedGame(message: GameInitMessage) {

    }




    /**
     * creates a client and connects it with the server.
     * @param secret Server secret.
     * @param name of the player.
     * @param playerType playerType of the player
     * @return true when the client connected successfully, false when not
     * @throws IllegalStateException when ConnectionState is not [ConnectionState.DISCONNECTED]
     * @throws IllegalArgumentException when secret or name is blank
     */
    private fun connect(secret: String, name: String, playerType:PlayerType): Boolean {

        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(name.isNotBlank()) { "player name must be given" }

        val newClient =
            NetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this,
                playerType = playerType
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }




    fun sendPlaceTile (TilePlacedMessage : service.message.TilePlacedMessage) {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }
        client?.sendGameActionMessage(TilePlacedMessage)

    }


    fun tilePlacedMessage(message: TilePlacedMessage ,sender:String) {

        rootService.playerService.placeTile(message.rCoordinate,message.qCoordinate)

    }


    fun disconnect() {

        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }



    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    private fun setSimulationSpeed(speed: Double) :Double  {
        val game = rootService.currentGame
        checkNotNull(game)

        var newSpeed = speed
        if(newSpeed < 1) newSpeed = 1.0
        if(newSpeed > 100) newSpeed = 100.0
        game.simulationSpeed = newSpeed

        onAllRefreshables { refreshAfterSimulationSpeedChange(newSpeed) }
        return newSpeed

    }

}

