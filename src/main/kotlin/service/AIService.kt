package service

import entity.*
import kotlin.random.Random

class AIService(private val rootService: RootService) {

    private val placeableTiles: MutableList<Pair<Int,Int>> = mutableListOf()

    fun randomNextTurn() {
        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        //check(gameService.isGameEnded())

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.RANDOMAI)

        placeableTiles.shuffle()

        //Rotate the tile by a random amount
        val randomRotation = Random.Default.nextInt(0, 6)
        for (i in 0 until randomRotation){
            playerService.rotateTile(player.playHand.first())
        }

        var selectedPos: Pair<Int,Int> ?= null
        var selectedTile: Tile

        while (placeableTiles.isNotEmpty()){
            selectedPos = placeableTiles.first()
            selectedTile = gameService.getTileFromAxialCoordinates(selectedPos.first, selectedPos.second)

            if(selectedTile is PathTile){
                placeableTiles.removeFirst()
                continue
            }
            else if(gameService.isPlaceable(selectedPos.first, selectedPos.second, player.playHand.first())){
                break
            }
            //If isPlaceable returns false for an empty position it means that the tile blocks a exit
            //Then a rotation will always solve it given the existing tile types
            else if(selectedTile is EmptyTile){
                playerService.rotateTile(player.playHand.first())
                continue
            }
        }

        //No possible move for the AI meaning game has ended
        if(placeableTiles.isEmpty()){
            //gameService.endGame()
            return
        }

        checkNotNull(selectedPos)
        gameService.setTileFromAxialCoordinates(selectedPos.first, selectedPos.second, player.playHand.first())
        placeableTiles.removeFirst()

        //TODO: Turn object is still missing, also no method which moves gems after move
        //TODO: Cant call gameService.placeTile() since private
        //TODO: Documentation and testing
        //

    }

    private fun initializePlaceableTiles(){
        if (placeableTiles.isNotEmpty()){
            return
        }

        for (x in -4..4) {
            for (y in -4..4) {
                // Check if the conditions are met
                if ((x + y) in -4..4) {
                    placeableTiles.add(Pair(x,y))
                }
            }
        }
    }

    init {
        initializePlaceableTiles()
    }

    private fun evaluatePosition(turn: Turn, aiPlayerID: Int): Int{
        val evaluation = 0
        //Evaluate how good the score is for us
        for(i in 0 until turn.scoreChanges.size){
            if(i == aiPlayerID){
                evaluation + turn.scoreChanges[i]
            }
            else{
                evaluation - turn.scoreChanges[i]
            }
        }

        //TODO: Distanz zu unseren Gate(Auch richtige Connection) optimal ist Maximale distanz für gegner minimale für uns
    }
}