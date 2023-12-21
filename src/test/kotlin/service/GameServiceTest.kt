package service

import kotlin.test.*
import entity.*

class GameServiceTest {

    @Test
    fun testSetGate(){
        val game = RootService()
        val player = mutableListOf(Player("q",0,PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
        ,Player("q1",0,PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf()))
        game.gameService.startNewGame( player , false,1.0,false)
        //game.gameService.setGates(false)
        val player1 = mutableListOf(Player("q",0,PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
            ,Player("q1",0,PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf())
            ,Player("q2",0,PlayerType.LOCALPLAYER,0, 0, mutableListOf(), mutableListOf()))
        game.gameService.startNewGame( player1 , true,1.0,false)
        //game.gameService.setGates(true)
    }

}