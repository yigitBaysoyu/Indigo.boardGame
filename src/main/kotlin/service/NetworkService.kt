package service
import entity.*
import view.*
import java.lang.IllegalStateException
import edu.udo.cs.sopra.ntf.*
import edu.udo.cs.sopra.ntf.Player

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games.
 */

class NetworkService (private  val rootService: RootService) : AbstractRefreshingService() {
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Indigo"

        /** Password for the server */
        const val SERVER_SECRET = "game23d"
    }

    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    val ntfPlayerList: MutableList<Player> = mutableListOf()

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
    var gameMode: GameMode = GameMode.TWO_NOT_SHARED_GATEWAYS



    /**
     * Connects to server and creates a new game session.
     * @param hostPlayerName Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(sessionID: String?, hostPlayerName: String, gameMode: GameMode) {
        if (!connect(hostPlayerName)) {
            error("Connection failed")
        }
        this.ntfPlayerList.add(Player(hostPlayerName, PlayerColor.WHITE))
        this.gameMode = gameMode

        val networkClient = checkNotNull(client) { "No client connected." }

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
     * @param guestName name of the player wants to join
     * @param sessionID identifier of the joined session (as defined by host on create)
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(sessionID: String, guestName: String, playerType: PlayerType) {
        if (!connect(guestName)) {
            error("Connection failed")
        }

        val networkClient = checkNotNull(client){"No client connected."}
        networkClient.playerType = playerType
        networkClient.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
    }

    /**
     * set up the game using [GameService.startNewGame] and send the game init message
     * Called when  [ConnectionState.WAITING_FOR_GUESTS] and the host wants to start the game.
     * when called: a new game will be created and a [GameInitMessage] will be sent to the server.
     */
    fun startNewHostedGame(selectedColors: MutableList<Int>, hostType: PlayerType, randomOrder: Boolean = false) {
        if(connectionState != ConnectionState.WAITING_FOR_GUESTS) return
        val client = checkNotNull(client) { "no active client" }

        // create player objects
        val indigoPlayers = ntfPlayerList.map { entity.Player(name = it.name)}.toMutableList()
        for (i in indigoPlayers.indices) {
            // Set colors
            indigoPlayers[i].color = selectedColors[i]

            // Set playerTypes
            if(indigoPlayers[i].name == client.playerName) {
                indigoPlayers[i].playerType = hostType
            } else {
                indigoPlayers[i].playerType = PlayerType.NETWORKPLAYER
            }
        }

        if(randomOrder) indigoPlayers.shuffle()

        ntfPlayerList.clear()
        for(i in indigoPlayers.indices) {
            val ntfColor = when(indigoPlayers[i].color) {
                0 -> PlayerColor.WHITE
                1 -> PlayerColor.RED
                2 -> PlayerColor.BLUE
                else -> PlayerColor.PURPLE
            }
            val ntfName = indigoPlayers[i].name
            ntfPlayerList.add(Player(ntfName, ntfColor))
        }

        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(
            players = indigoPlayers,
            threePlayerVariant = gameMode == GameMode.THREE_SHARED_GATEWAYS,
            isNetworkGame = true,
            sendGameInitMessage = true
        )
    }

    /**
     * This methode sends [GameInitMessage] to server
     * @throws IllegalStateException when players is not yet initialized
     */
    fun sendGameInitMessage(playerList: MutableList<entity.Player>) {
        val game = checkNotNull(rootService.currentGame) { "Game not found" }
        val drawPile = game.drawPile

        val formattedDrawPile = drawPile.map{
            when (it.type) {
                0 -> TileType.TYPE_0
                1 -> TileType.TYPE_1
                2 -> TileType.TYPE_2
                3 -> TileType.TYPE_3
                else -> TileType.TYPE_4
            }
        }.toMutableList()

        val playerListForMessage = playerList.map { Player(it.name, when(it.color) {
            0 -> PlayerColor.WHITE
            1 -> PlayerColor.RED
            2 -> PlayerColor.BLUE
            else -> PlayerColor.PURPLE
        }) }

        // create game GameInitMessage
        val initMessage = GameInitMessage(playerListForMessage, gameMode , formattedDrawPile)

        // send message
        val networkClient = checkNotNull(client) { "No client connected." }
        networkClient.sendGameActionMessage(initMessage)
    }

    private fun extractDrawPile(message: GameInitMessage): MutableList<PathTile> {
        val drawPile = mutableListOf<PathTile>()
        for(tileType in message.tileList) {
            drawPile.add(tileTypeToPathTile(tileType))
        }
        return drawPile
    }
    private fun tileTypeToPathTile(type: TileType): PathTile {
        val lines = rootService.gameService.loadTilesCsv()

        val typeAsInt = when (type) {
            TileType.TYPE_0 -> 0
            TileType.TYPE_1 -> 1
            TileType.TYPE_2 -> 2
            TileType.TYPE_3 -> 3
            TileType.TYPE_4 -> 4
        }

        val splitLine = lines[typeAsInt].split(";")
        val map: MutableMap<Int, Int> = mutableMapOf()

        //Create connections map going both ways
        for (i in 2 until splitLine.size step 2) {
            map[(splitLine[i].toInt() + 5) % 6] = (splitLine[i + 1].toInt() + 5) % 6
            map[(splitLine[i + 1].toInt() + 5) % 6] = (splitLine[i].toInt() + 5) % 6
        }

        return PathTile(map, 0, 0, 0, mutableListOf(), typeAsInt)
    }
    fun startNewJoinedGame(message: GameInitMessage, playerType: PlayerType) {
        // check if we are waiting for gameInitMessage. if not then there is no game to start
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not waiting for game init message. " }
        val networkClient = checkNotNull(client) {"Client not found"}

        val indigoPlayers = message.players.map { entity.Player( name = it.name)}.toMutableList()
        for (i in indigoPlayers.indices) {
            indigoPlayers[i].color = when(message.players[i].color) {
                PlayerColor.WHITE -> 0
                PlayerColor.RED -> 1
                PlayerColor.BLUE -> 2
                else -> 3
            }

            if(indigoPlayers[i].name == networkClient.playerName) {
                indigoPlayers[i].playerType = playerType
            } else {
                indigoPlayers[i].playerType = PlayerType.NETWORKPLAYER
            }
        }

        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(
            players = indigoPlayers,
            threePlayerVariant = message.gameMode == GameMode.THREE_SHARED_GATEWAYS,
            isNetworkGame = true,
        )

        val game = checkNotNull(rootService.currentGame) { "no active game" }
        val playerNames = indigoPlayers.map { it.name }
        val index = playerNames.indexOf(networkClient.playerName)

        game.drawPile = extractDrawPile(message)

        for(player in game.playerList) {
            player.playHand.clear()
            player.playHand.add(game.drawPile.removeFirst())
        }

        if(index == 0) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        }
    }

    /**
     * creates a client and connects it with the server.
     * @param name of the player.
     * @return true when the client connected successfully, false when not
     * @throws IllegalStateException when ConnectionState is not [ConnectionState.DISCONNECTED]
     * @throws IllegalArgumentException when secret or name is blank
     */
    private fun connect(name: String): Boolean {
        if(connectionState != ConnectionState.DISCONNECTED) {
            disconnect()
            client = null
        }

        require(name.isNotBlank()) { "player name must be given" }

        val newClient = NetworkClient(
            playerName = name,
            host = SERVER_ADDRESS,
            secret = SERVER_SECRET,
            networkService = this,
        )

        val success = newClient.connect()
        if(success) {
            this.client = newClient
            updateConnectionState(ConnectionState.CONNECTED)
        }
        return success
    }


    fun sendPlaceTile (tilePlacedMessage : TilePlacedMessage) {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }
        val networkClient = checkNotNull(client) { "No client connected." }
        networkClient.sendGameActionMessage(tilePlacedMessage)//
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
    }


    fun tilePlacedMessage(message: TilePlacedMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENTS_TURN)

        // Rotate the tile the required number of times
        repeat(message.rotation) {
            rootService.playerService.rotateTile(suppressRefresh = true)
        }

        rootService.playerService.placeTile(message.qcoordinate,message.rcoordinate)
    }

    /**
     * Disconnects the client
     */
    fun disconnect() {
        ntfPlayerList.clear()
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