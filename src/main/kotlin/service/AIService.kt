package service

import entity.PlayerType
import kotlin.random.Random

class AIService(private val rootService: RootService) {

    private val placeableTiles: MutableList<Pair<Int,Int>> = mutableListOf()

    fun randomNextTurn() {
        val gameService = rootService.gameService
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        val player = currentGame.getActivePlayer()
        require(player.playerType == PlayerType.RANDOMAI)

        require(placeableTiles.isNotEmpty())
        placeableTiles.shuffle()

        var selectedTile = placeableTiles.removeFirst()

        while (!gameService.isPlaceable(selectedTile.first, selectedTile.second, player.playHand)){
            selectedTile = placeableTiles.removeFirst()
        }

        val randomRotation = Random.Default.nextInt(0, 6)

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