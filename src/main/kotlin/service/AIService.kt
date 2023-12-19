package service

import entity.EmptyTile
import entity.PathTile
import entity.PlayerType
import entity.Tile
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
            playerService.rotateTile(player.playHand)
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
            else if(gameService.isPlaceable(selectedPos.first, selectedPos.second, player.playHand)){
                break
            }
            //If isPlaceable returns false for an empty position it means that the tile blocks a exit
            //Then a rotation will always solve it given the existing tile types
            else if(selectedTile is EmptyTile){
                playerService.rotateTile(player.playHand)
                continue
            }
        }

        //No possible move for the AI meaning game has ended
        if(placeableTiles.isEmpty()){
            gameService.endGame()
            return
        }

        checkNotNull(selectedPos)
        gameService.setTileFromAxialCoordinates(selectedPos.first, selectedPos.second, player.playHand)
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
}