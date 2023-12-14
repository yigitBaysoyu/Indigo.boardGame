package service

import view.Refreshable



/**
 * Abstract service class that handles multiples [Refreshable]s (usually UI elements, such as
 * specialized [tools.aqua.bgw.core.BoardGameScene] classes/instances) which are notified
 * of changes to refresh via the [onAllRefreshables] method.
 *
 */
abstract class AbstractRefreshingService {

    val refreshables = mutableListOf<Refreshable>()

    /**
     * adds a [Refreshable] to the list that gets called
     * whenever [onAllRefreshables] is used.
     */
    fun addRefreshable(newRefreshable : Refreshable) {
        refreshables += newRefreshable
    }

    fun onAllRefreshables(method: Refreshable.() -> Unit) =
        refreshables.forEach { it.method() }


}