package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Data class representing the [ExampleMessage]
 * @attribute [foo] example [String] attribute
 * @attribute [bar] example list of [Int]
 */
@GameActionClass
data class ExampleMessage(
    val foo: Int,
    val bar: List<Example>
) : GameAction()