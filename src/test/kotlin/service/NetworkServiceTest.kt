package service

import entity.Player
import entity.PlayerType
import kotlin.test.assertNotNull
import edu.udo.cs.sopra.ntf.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlin.test.assertNull
/**
 * Testfälle für die Klasse NetWorkService
 */
class NetworkServiceTest {


    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService
    private lateinit var rootServiceGuest2: RootService

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
        rootServiceGuest2 = RootService()




        rootServiceHost.networkService.hostGame(NETWORK_SECRET,  generateRandomNumberAsString(),"ahmad", color = PlayerColor.WHITE ,GameMode.THREE_NOT_SHARED_GATEWAYS)

        assert(rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUESTS)){
            error("Nach dem Warten nicht im Zustand angekommen")
        }

        val hostClient = rootServiceHost.networkService.client
        assertNotNull(hostClient)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, hostClient.sessionID!!,"mohmed",PlayerType.NETWORKPLAYER)
        assert(rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)){
            error("Nach dem Warten nicht im Zustand angekommen")
        }



        rootServiceGuest2.networkService.joinGame(NETWORK_SECRET, hostClient.sessionID!!,"Alex",PlayerType.NETWORKPLAYER)

        assert(rootServiceGuest2.waitForState(ConnectionState.WAITING_FOR_INIT)){
            error("Nach dem Warten nicht im Zustand angekommen")
        }

        assert(rootServiceGuest2.waitForState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)) {
            error("connectionState of the host must be PLAYING_TURN")
        }


    }

    /**
     * Test für Host und Beitritt zum neuen Spiel
     */
    @Test
    fun testHostAndJoinGame() {


        initConnections()












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

    fun generateRandomNumberAsString(): String {
        // Define the range for the random number.
        val lowerBound = 2001
        val upperBound = Int.MAX_VALUE

        // Generate a random number within the defined range and convert it to a String.
        return Random.nextInt(lowerBound, upperBound).toString()
    }



