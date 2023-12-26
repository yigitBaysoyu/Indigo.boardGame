package service

import kotlin.test.*
import entity.*

/**
 * This test class verifies the functionality of the `rotateTile` method in the game service.
 * It includes tests to checks if a rotated is correct.
 */
class RotateTileTest {


    /**
     * Tests the functionality of the `rotateTile` method.
     * This test ensures that the method correctly rotates tiles by the specified angle = 60 grad.
     * It covers various scenarios to check if the rotation results align with expected outcomes,
     * including changes in tile connections and rotation offsets.
     */
    @Test
    fun TestRotateTile() {
        val game = RootService()
        val tile = PathTile(
            mapOf(0 to 1, 2 to 3, 4 to 5), 0, 1, 1,
            mutableListOf()
        )

        game.playerService.rotateTile(tile)
        assertEquals(tile.rotationOffset , 1)
        assertEquals(tile.connections, mapOf(5 to 0, 1 to 2, 3 to 4 ))

        val tile1 = PathTile(
            mapOf(0 to 2, 4 to 3, 1 to 5), 0, 1, 1,
            mutableListOf()
        )

        game.playerService.rotateTile(tile1)
        assertEquals(tile1.rotationOffset , 1)
        assertEquals(tile1.connections, mapOf(1 to 3, 5 to 4, 2 to 0 ))

        game.playerService.rotateTile(tile1)
        assertEquals(tile1.rotationOffset , 2)
        assertEquals(tile1.connections, mapOf(2 to 4, 0 to 5, 3 to 1 ))

        repeat(3){
            game.playerService.rotateTile(tile1)
        }
        assertEquals(tile1.rotationOffset , 5)
        assertEquals(tile1.connections, mapOf(5 to 1, 3 to 2, 0 to 4 ))

        game.playerService.rotateTile(tile1)
        assertEquals(tile1.rotationOffset , 0)
        assertEquals(tile1.connections, mapOf(0 to 2, 4 to 3, 1 to 5 ))


    }
}