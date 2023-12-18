package service

import kotlin.test.*
import entity.*
import java.lang.IllegalStateException

/**
 * Contains all test cases for [GameService.checkIfValidAxialCoordinates], [GameService.getTileFromAxialCoordinates]
 * and [GameService.setTileFromAxialCoordinates]
 */
class AxialCoordinatesTest {

    private lateinit var rootService: RootService

    /**
     * Executed before all test methods in this class. Resets rootService.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * Tests [GameService.checkIfValidAxialCoordinates] for valid Coordinates
     */
    @Test
    fun testCheckIfValidAxialCoordinates1() {
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(0, 0))

        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-4, 0))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-5, 0))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(4, 0))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(5, 0))

        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(0, -4))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(0, -5))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(0, 4))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(0, 5))

        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(4, -4))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(5, -5))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-4, 4))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-5, 5))

        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-1, -2))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-3, -2))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(2, 1))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-2, 4))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-3, 5))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(-2, 5))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(5, -3))
        assertTrue(rootService.gameService.checkIfValidAxialCoordinates(5, -2))
    }

    /**
     * Tests [GameService.checkIfValidAxialCoordinates] for invalid Coordinates
     */
    @Test
    fun testCheckIfValidAxialCoordinates2() {
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-10, -10))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-10, 10))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(10, -10))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(10, 10))

        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-10, 0))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(10, 0))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(0, -10))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(0, 10))

        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-3, -3))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(6, -3))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-6, 4))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(4, -6))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(3, 3))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-6, 6))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(2, 4))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(2, 5))

        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-3, -4))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(7, -3))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-6, 5))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(4, -7))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(4, 4))
        assertFalse(rootService.gameService.checkIfValidAxialCoordinates(-3, -4))
    }

    /**
     * Tests [GameService.getTileFromAxialCoordinates] with no game currently running
     */
    @Test
    fun testGetTileFromAxialCoordinates1() {
        assertFailsWith<IllegalStateException>("No or wrong Exception thrown.") {
            rootService.gameService.getTileFromAxialCoordinates(0, 0)
        }
    }

    /**
     * Tests [GameService.getTileFromAxialCoordinates] with valid and invalid coordinates
     */
    @Test
    fun testGetTileFromAxialCoordinates2() {
        val testGameLayout: MutableList<MutableList<Tile>> = mutableListOf()

        for(i in 0 ..10) {
            testGameLayout.add(mutableListOf())
            for(j in 0..10) testGameLayout[i].add(InvisibleTile())
        }

        val testGame = IndigoGame(
            0, 1.0, false,
            ArrayDeque<Turn>(), ArrayDeque<Turn>(),
            mutableListOf(), mutableListOf(), mutableListOf(),
            testGameLayout
        )

        val testTile = EmptyTile(mapOf(), 0, 0, 0)
        testGame.gameLayout[5][5] = testTile

        rootService.currentGame = testGame

        // Testing with invalid coordinates
        assertFailsWith<IndexOutOfBoundsException>("No or wrong Exception thrown.") {
            rootService.gameService.getTileFromAxialCoordinates(7, 7)
        }

        // Testing with valid coordinates
        assertEquals(testTile, rootService.gameService.getTileFromAxialCoordinates(0, 0))
    }



    /**
     * Tests [GameService.setTileFromAxialCoordinates] with no game currently running
     */
    @Test
    fun testSetTileFromAxialCoordinates1() {
        val testTile = EmptyTile(mapOf(), 0, 0, 0)
        assertFailsWith<IllegalStateException>("No or wrong Exception thrown.") {
            rootService.gameService.setTileFromAxialCoordinates(0, 0, testTile)
        }
    }

    /**
     * Tests [GameService.setTileFromAxialCoordinates] with valid and invalid coordinates
     */
    @Test
    fun testSetTileFromAxialCoordinates2() {
        val testGameLayout: MutableList<MutableList<Tile>> = mutableListOf()

        for(i in 0 ..10) {
            testGameLayout.add(mutableListOf())
            for(j in 0..10) testGameLayout[i].add(InvisibleTile())
        }

        val testGame = IndigoGame(
            0, 1.0, false,
            ArrayDeque<Turn>(), ArrayDeque<Turn>(),
            mutableListOf(), mutableListOf(), mutableListOf(),
            testGameLayout
        )

        rootService.currentGame = testGame

        // Testing with invalid coordinates
        assertFailsWith<IndexOutOfBoundsException>("No or wrong Exception thrown.") {
            rootService.gameService.getTileFromAxialCoordinates(7, 7)
        }

        // Testing with valid coordinates
        val testTile = EmptyTile(mapOf(), 0, 0, 0)
        rootService.gameService.setTileFromAxialCoordinates(0, 0, testTile)
        assertEquals(testTile, testGame.gameLayout[5][5])
    }
}