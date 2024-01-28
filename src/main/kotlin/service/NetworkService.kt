package service
import entity.*
import view.*
import java.lang.IllegalStateException
import edu.udo.cs.sopra.ntf.*
import edu.udo.cs.sopra.ntf.Player

//import kotlinx.coroutines.runBlocking
//import kotlin.system.measureTimeMillis

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games.
 */

class NetworkService (private  val rootService: RootService) : AbstractRefreshingService() {


    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private var threePlayerVariant: Boolean = false

    companion object {

        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Indigo"

    }

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
    private var simulationSpeed : Double = 1.0
    var gameMode: GameMode = GameMode.TWO_NOT_SHARED_GATEWAYS

    val playerList: MutableList<Player> = mutableListOf()

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
        this.playerList.add(Player(hostPlayerName, color))
        this.gameMode =  gameMode

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

        updateConnectionState(ConnectionState.CONNECTED)
        val networkClient = checkNotNull(client){"No client connected."}
        networkClient.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
    }
    /**
     * set up the game using [GameService.startNewGame] and send the game init message
     * Called when ConnectionState.READY_FOR_GAME and the host wants to start the game.
     * when called: a new game will be created and a [GameInitMessage] will be sent to the server.
     */

    fun startNewHostedGame(selectedColors: MutableList<Int>) {

        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "currently not prepared to start a new hosted game." }

        // playerNames
        val players = playerList.map { entity.Player( name = it.name)}.toMutableList()
        for (i in players.indices) {
            players[i].color = selectedColors[i]
            playerList[i] = when(selectedColors[i]) {
                0 -> Player(playerList[i].name, PlayerColor.WHITE)
                1 -> Player(playerList[i].name, PlayerColor.RED)
                2 -> Player(playerList[i].name, PlayerColor.BLUE)
                else -> Player(playerList[i].name, PlayerColor.PURPLE)
            }
        }
        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(players,threePlayerVariant, simulationSpeed = simulationSpeed , isNetworkGame = true)

        // startGame from the gameService to start the game
        // send game init message to server

        sendGameInitMessage()

        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        println(currentGame.drawPile)

        val networkClient = checkNotNull(client){"No client connected."}

        for(player in players) {
            player.playHand.clear()
            player.playHand.add(currentGame.drawPile.removeFirst())
        }

        if(players[0].name == networkClient.playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        }

           // SEHR WICHTIG : von hier kann man die KI aktivieren ( es wurde nicht aktiviert WEIL KI Noch NICHT Fertig)
          // BITTE auch dasselbe aktivieren in  startNewJoinedGame aktivieren

        /*

        if ( connectionState == ConnectionState.PLAYING_MY_TURN ){

            when (client!!.playerType){

                PlayerType.SMARTAI -> {rootService.aiService.calculateNextTurn()}
                PlayerType.RANDOMAI -> {rootService.aiService.randomNextTurn()}
                else -> {}
            }
        }

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
        val initMessage = GameInitMessage(playerList, gameMode , formatedDrawPile)

        // send message
        val networkClient = checkNotNull(client){"No client connected."}
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

        val typeAsInt = when(type) {
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
            map[(splitLine[i].toInt()+5)%6] = (splitLine[i + 1].toInt()+5)%6
            map[(splitLine[i + 1].toInt()+5)%6] = (splitLine[i].toInt()+5)%6
        }

        val pathTile = PathTile(map, 0, 0, 0, mutableListOf(), typeAsInt)
        return pathTile
    }
    fun startNewJoinedGame(message: GameInitMessage) {

        // check if we are waiting for gameInitMessage. if not then there is no game to start
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not waiting for game init message. " }
        val networkClient = checkNotNull(client) {"Client not found"}

        val players = message.players
        val player = players.map { entity.Player( name = it.name)}.toMutableList()
        for (i in players.indices) {
            player[i].color = when(players[i].color) {
                PlayerColor.WHITE -> 0
                PlayerColor.RED -> 1
                PlayerColor.BLUE -> 2
                else -> 3
            }
        }

        // start new game and give the supply as a parameter.
        rootService.gameService.startNewGame(
            player,
            extractThreePlayerVariant(message),
            simulationSpeed = simulationSpeed,
            isNetworkGame = true,
        )
        val game = rootService.currentGame
        checkNotNull(game)
        val playerNames = players.map { it.name }
        val index = playerNames.indexOf(networkClient.playerName)

        game.drawPile = extractDrawPile(message)

        for (currentPlayer in player) {
            currentPlayer.playHand.clear()
            currentPlayer.playHand.add(game.drawPile.removeFirst())
        }

        if( index == 0) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        }


        /**
        // hier muss auch die Ki implementierung aktiveret werden



        if ( connectionState == ConnectionState.PLAYING_MY_TURN ){

        when (client!!.playerType){

        PlayerType.SMARTAI -> {
            val timeTaken = measureTimeMillis {
                runBlocking {
                    rootService.aiService.calculateNextTurn()
                }
            }
            println("Took : ${timeTaken/1000} sec")

        }
        PlayerType.RANDOMAI -> {rootService.aiService.randomNextTurn()}
        else -> {}
        }
        }
        */



    }
       private fun extractThreePlayerVariant(message: GameInitMessage): Boolean {
        val threePlayerVariant = when(message.gameMode) {
            GameMode.THREE_SHARED_GATEWAYS -> true
            GameMode.TWO_NOT_SHARED_GATEWAYS -> false
            GameMode.THREE_NOT_SHARED_GATEWAYS -> false
            GameMode.FOUR_SHARED_GATEWAYS -> false
        }

        return threePlayerVariant
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
            // update connection setate to connected.
            true
        } else {
            false
        }
    }


    /**
     * Sends every Network Player the [tilePlacedMessage] and sets the connection State
     * to Waiting_For_Opponents_Turn
     */
    fun sendPlaceTile (tilePlacedMessage : TilePlacedMessage) {
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }
        val networkClient = checkNotNull(client){"No client connected."}//
        networkClient.sendGameActionMessage(tilePlacedMessage)//
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)

    }

    /**
     * For Player that are waiting for their turn this function turns the [message]
     * into an actual player Move and places the Tile and updates the connection State
     */
    fun tilePlacedMessage(message: TilePlacedMessage ) {

        check(connectionState == ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        val rotationSteps = message.rotation

        // Rotate the tile the required number of times
        for (i in 1..rotationSteps) {
            rootService.playerService.rotateTile()
        }
        val game = rootService.currentGame
        checkNotNull(game)

        val networkClient = checkNotNull(client){"No client connected."}//
        rootService.playerService.placeTile(message.qcoordinate,message.rcoordinate)

        if (game.playerList[game.activePlayerID].name == networkClient.playerName){

            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }



    }

    /**
     * disconnects the client from the game Session
     */
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