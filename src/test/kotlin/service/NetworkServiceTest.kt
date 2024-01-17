package service

import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import edu.udo.cs.sopra.ntf.*

/**
 * Testfälle für die Klasse NetWorkService
 */
class NetworkServiceTest {


    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService

    companion object {
        const val NETWORK_SECRET = "game23d"
    }


    /**
     * Initialisieren Sie beide Verbindungen und starten Sie das Spiel, damit die Spieler beider Spiele
     * (dargestellt durch [rootServiceHost] und [rootServiceGuest]) in ihren Zügen sind.
     */
    private fun initConnections() {

        rootServiceHost = RootService()
        rootServiceGuest = RootService()
        val rootService = RootService()
        val gameService = rootService.gameService



        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "199914","ahm", color = PlayerColor.BLUE ,GameMode.TWO_NOT_SHARED_GATEWAYS)

        assert(rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUESTS)){
             error("Nach dem Warten nicht im Zustand angekommen")
        }

        val hostClient = rootServiceHost.networkService.client
        assertNotNull(hostClient)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, hostClient.sessionID!!,"mo",PlayerType.NETWORKPLAYER)

        assert(rootServiceGuest.waitForState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
                || rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)){
            error("Nach dem Warten nicht im Zustand angekommen")
        }

        val guestClient = rootServiceGuest.networkService.client
        assertNotNull(guestClient)
        println(rootServiceGuest.networkService.playersList)
        // danach muss nach mindestens einem Spielerbeitritt der Verbindungsstatus des Hosts auf READY_FOR_GAME sein
        assert(rootServiceHost.waitForState(ConnectionState.READY_FOR_GAME)){
            error("Nach dem Warten nicht im Zustand angekommen")
        }

    }

    /**
     * Test für Host und Beitritt zum neuen Spiel
     */
    @Test
     fun testHostAndJoinGame() {


        initConnections()

        rootServiceHost.networkService.startNewHostedGame()


        val currentGameHost = rootServiceHost.currentGame
        assertNotNull(currentGameHost)



        val currentPlayer = currentGameHost.activePlayerID

        val hostClient = rootServiceHost.networkService.client!!



        if (currentGameHost.playerList[currentPlayer].name == hostClient.playerName){

            assert(rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)){
                error("connectionState of the host must be PLAYING_TURN")
            }

            assert(rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)){
                error("connectionState of the guest  must be WAITING_FOR_TURN")
            }

        }else{

            // the host is not the currentPlayer
            assert(rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)){
                error("connectionState of the guest must be PLAYING_TURN")
            }

            assert(rootServiceHost.waitForState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)){
                error("connectionState of the Host must be WAITING_FOR_TURN")
            }
            // retrieve currentGame of the guest
            val currentGameGuest = rootServiceGuest.currentGame
            assertNotNull(currentGameGuest)



        }


    }

 private fun RootService.waitForState(state: ConnectionState, timeout: Int = 5000):Boolean {
     var timePassed = 0
     while (timePassed < timeout) {
         if (networkService.connectionState == state)
             return true
         else {
             Thread.sleep(100)
             timePassed += 100
         }
     }
     return false
 }
}
