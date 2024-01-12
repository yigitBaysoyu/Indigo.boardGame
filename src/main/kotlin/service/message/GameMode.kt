package service.message

/**
 * The distinct Modes in which the Game can be played regarding the assigning of Gates between the several
 * [Player] objects.
 */
enum class GameMode {
    TWO_NOT_SHARED_GATEWAYS,
    THREE_SHARED_GATEWAYS,
    THREE_NOT_SHARED_GATEWAYS,
    FOUR_SHARED_GATEWAYS
}