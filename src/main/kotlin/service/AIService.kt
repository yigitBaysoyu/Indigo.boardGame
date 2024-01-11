package service

import entity.PlayerType

class AIService(private val rootService: RootService) {

    private val placeableTiles: MutableList<Pair<Int,Int>> = mutableListOf()

    fun calculateNextTurn(){
        val gameService = rootService.gameService
        val playerService = rootService.playerService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.SMARTAI)


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

