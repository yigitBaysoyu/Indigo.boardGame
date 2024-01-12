package service

import entity.*
import kotlin.random.Random

class AIService(private val rootService: RootService) {

    private val placeableTiles: MutableList<Pair<Int,Int>> = mutableListOf()

    /**
     * Function to decide on a random move for the [PlayerType.RANDOMAI]
     * Also keeps track of the already placed tiles each time it is called,
     * in [placeableTiles]
     *
     * @throws [IllegalArgumentException] if the active player is not of
     * [PlayerType.RANDOMAI]
     */
    fun randomNextTurn() {
        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        gameService.checkIfGameEnded()

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.RANDOMAI)

        placeableTiles.shuffle()

        //Rotate the tile by a random amount
        val randomRotation = Random.Default.nextInt(0, 6)
        for (i in 0 until randomRotation){
            playerService.rotateTile()
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
            else if(gameService.isPlaceAble(selectedPos.first, selectedPos.second, player.playHand.first())){
                break
            }
            //If isPlaceable returns false for an empty position it means that the tile blocks a exit
            //Then a rotation will always solve it given the existing tile types
            else if(selectedTile is EmptyTile){
                playerService.rotateTile()
                continue
            }
        }

        if(placeableTiles.isEmpty()){
            gameService.checkIfGameEnded()
            return
        }
        checkNotNull(selectedPos)
        placeableTiles.removeFirst()

        //Turn object is created in placeTile
        playerService.placeTile(selectedPos.first, selectedPos.second)
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