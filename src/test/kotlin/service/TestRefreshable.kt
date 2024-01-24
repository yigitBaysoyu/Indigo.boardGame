package service

import entity.GemMovement
import entity.PathTile
import entity.Turn
import view.Refreshable

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 */

class TestRefreshable : Refreshable {
    var refreshAfterStartNewGameCalled: Boolean = false
        private set

    var refreshAfterTileRotatedCalled: Boolean = false
        private set

    var refreshAfterTilePlacedCalled: Boolean = false
        private set

    var refreshAfterGemMovedCalled: Boolean = false
        private set

    var refreshAfterEndGameCalled: Boolean = false
        private set

    var refreshAfterUndoCalled: Boolean = false
        private set

    var refreshAfterRedoCalled: Boolean = false
        private set

    var refreshAfterLoadGameCalled: Boolean = false
        private set

    var refreshAfterGameJoinedCalled: Boolean = false
        private set

    var refreshAfterPlayerJoinedCalled: Boolean = false
        private set

    var refreshAfterPlayerLeftCalled: Boolean = false
        private set

    var refreshAfterSimulationSpeedChangeCalled: Boolean = false
        private set


    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartNewGameCalled = false
        refreshAfterTileRotatedCalled = false
        refreshAfterTilePlacedCalled = false
        refreshAfterGemMovedCalled = false
        refreshAfterEndGameCalled = false
        refreshAfterUndoCalled = false
        refreshAfterRedoCalled = false
        refreshAfterGameJoinedCalled = false
        refreshAfterPlayerJoinedCalled = false
        refreshAfterLoadGameCalled = false
        refreshAfterPlayerLeftCalled = false
        refreshAfterSimulationSpeedChangeCalled = false

    }

    override fun refreshAfterStartNewGame() {
        refreshAfterStartNewGameCalled = true
    }

    override fun refreshAfterTileRotated() {
        refreshAfterTileRotatedCalled = true

    }

    override fun refreshAfterTilePlaced(turn: Turn) {
        refreshAfterTilePlacedCalled = true

    }

    override fun refreshAfterGemMoved(movement: GemMovement) {
        refreshAfterGemMovedCalled = true

    }

    override fun refreshAfterEndGame() {
        refreshAfterEndGameCalled = true

    }

    override fun refreshAfterUndo(turn: Turn) {
        refreshAfterUndoCalled = true

    }

    override fun refreshAfterRedo(turn: Turn) {
        refreshAfterRedoCalled = true

    }

    override fun refreshAfterLoadGame() {
        refreshAfterLoadGameCalled = true

    }

    override fun refreshAfterGameJoined() {
        refreshAfterGameJoinedCalled = true

    }

    override fun refreshAfterPlayerJoined(name: String) {
        refreshAfterPlayerJoinedCalled = true

    }

    override fun refreshAfterPlayerLeft(color: Int) {
        refreshAfterPlayerLeftCalled = true

    }

    override fun refreshAfterSimulationSpeedChange(speed: Double) {
        refreshAfterSimulationSpeedChangeCalled = true

    }
}

