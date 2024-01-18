package service
import entity.*
import tools.aqua.bgw.net.common.Message
import view.*
import java.lang.IllegalStateException
import edu.udo.cs.sopra.ntf.*
/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games.
 */

class NetworkService (private  val rootService: RootService) : AbstractRefreshingService() {


    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    var playersList: MutableList<String> = mutableListOf()

    var threePlayerVariant: Boolean = false

    companion object {

        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Indigo"

    }

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
    var simulationSpeed : Double = 1.0
    var gameMode: GameMode = GameMode.TWO_NOT_SHARED_GATEWAYS

    val players_list: MutableList<edu.udo.cs.sopra.ntf.Player> = mutableListOf()

    /**
     * Connects to server and creates a new game session.
     * @param secret Server secret.
     * @param hostPlayerName Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */


    fun hostGame(secret: String ,sessionID: String?, hostPlayerName: String, color: PlayerColor,  gameMode: GameMode) {
        if (!connect(secret, hostPlayerName,PlayerType.NETWORKPLAYER)) {
            error("Connection failed")
        }
        this.playersList.add(hostPlayerName)
        this.gameMode =  gameMode

        val newPlayer = edu.udo.cs.sopra.ntf.Player(hostPlayerName, color)
        players_list.add(newPlayer)

        // updateConnectionState(ConnectionState.CONNECTED) add in the method connect.
        val networkClient = checkNotNull(client){"No client connected."}

        if (sessionID.isNullOrBlank()) {
            networkClient.createGame(GAME_ID, "Welcome!")
        } else {
            networkClient.createGame(GAME_ID, sessionID, "Welcome!")
        }
        updateConnectionState(ConnectionState.HOST_WAITING_FOR_CONFIRMATION)
    }


    /**
     * Connect to server and join a game session as guest player.
     *
     * @param secret Server secret.
     * @param guestName name of the player wants to join
     * @param guestPlayerType player type of the player wants to join
     * @param sessionID identifier of the joined session (as defined by host on create)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(secret: String, sessionID: String,  guestName: String, guestPlayerType:PlayerType) {

        if (!connect(secret, guestName,guestPlayerType)) {
            error("Connection failed")
        }

        // updateConnectionState(ConnectionState.CONNECTED) moved to the methode connect

        val networkClient = checkNotNull(client){"No client connected."}
        networkClient.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
    }
    /**
     * set up the game using [GameService.startNewGame] and send the game init message
     * Called when  [ConnectionState.READY_FOR_GAME] and the host wants to start the game.
     * when called: a new game will be created and a [GameInitMessage] will be sent to the server.
     */

    fun startNewHostedGame() {

        check(connectionState == ConnectionState.READY_FOR_GAME)
        { "currently not prepared to start a new hosted game." }
        val players = this.playersList

        // playerNames
        val player = players.map { entity.Player( name = it)}.toMutableList()

        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(player,threePlayerVariant, simulationSpeed = simulationSpeed , isNetworkGame = true)

        // startGame from the gameService to start the game
        // send game init message to server

        sendGameInitMessage()

        val currentGame = rootService.currentGame
        checkNotNull(currentGame) { "game should not be null right after starting it." }

        val networkClient = checkNotNull(client){"No client connected."}

        for(player in players) {
            currentGame.playerList[currentGame.activePlayerID].playHand.clear()
            currentGame.playerList[currentGame.activePlayerID].playHand.add(currentGame.drawPile.removeLast())
        }

        if (currentGame.playerList[currentGame.activePlayerID].name == networkClient.playerName)
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)

        /**
        // hier kommt die Ki implementierung
         */

    }
    /**
    * This methode sends [GameInitMessage] to server
    * @throws IllegalStateException when players is not yet initialized
    */

    private fun sendGameInitMessage(){
        val game = checkNotNull(rootService.currentGame) {"Game not found"}
        val drawPile = game.drawPile
        // check not null supply.

        val formatedDrawPile = drawPile.map{
                it ->
            when (it.type) {
                0 -> {
                    TileType.TYPE_0
                }

                1 -> {
                    TileType.TYPE_1
                }

                2 -> {
                    TileType.TYPE_2
                }

                3 -> {
                    TileType.TYPE_3
                }

                else -> {
                    TileType.TYPE_4
                }
            }
        }.toMutableList()

        // create game GameInitMessage
        val initMessage = edu.udo.cs.sopra.ntf.GameInitMessage(players_list, gameMode , formatedDrawPile)

        // send message
        val networkClient = checkNotNull(client){"No client connected."}
        networkClient.sendGameActionMessage(initMessage)

        updateConnectionState(ConnectionState.GAME_STARTED)
    }


    fun startNewJoinedGame(message: edu.udo.cs.sopra.ntf.GameInitMessage) {

        // check if we are waiting for gameInitMessage. if not then there is no game to start
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not waiting for game init message. " }

        val networkClient = checkNotNull(client){"No client connected."}

        val playerTypes:MutableList<PlayerType> = mutableListOf()
        for (playerInfo in message.players){
            if (playerInfo.name == networkClient.playerName)
                playerTypes.add(networkClient.playerType)
            else
                playerTypes.add(PlayerType.NETWORKPLAYER)
        }

        val players:MutableList<String> = mutableListOf()
        message.players.forEach { players.add(it.name) }


        val player = players.map { entity.Player( name = it)}.toMutableList()

        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(player,threePlayerVariant, simulationSpeed = simulationSpeed , isNetworkGame = true)

        // update connection state after game was initialized

        updateConnectionState(ConnectionState.GAME_STARTED)


        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        {"game is not yet initialized!"}


        if (currentGame.playerList[currentGame.activePlayerID].name == networkClient.playerName)            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        else
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)

        /**
        // hier kommt die Ki implementierung
         */


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
            updateConnectionState(ConnectionState.CONNECTED)
            // update connection setate to connected.
            true
        } else {
            false
        }
    }


    fun sendPlaceTile (TilePlacedMessage : TilePlacedMessage) {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }
        val networkClient = checkNotNull(client){"No client connected."}//
        client?.sendGameActionMessage(TilePlacedMessage)//
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)

    }


    fun tilePlacedMessage(message: TilePlacedMessage ,sender:String) {
        val rotationSteps = message.rotation / 60

        // Rotate the tile the required number of times
        for (i in 1..rotationSteps) {
            rootService.playerService.rotateTile()
        }
        rootService.playerService.placeTile(message.rcoordinate,message.qcoordinate)

    }


    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }


    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshables via [Refreshable.refreshConnectionState]
     */

    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }


}