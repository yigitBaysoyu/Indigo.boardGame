package entity

import kotlinx.serialization.Serializable

/**
 * Enum to represent the different types of Players.
 */
@Serializable
enum class PlayerType {
    LOCALPLAYER,
    NETWORKPLAYER,
    RANDOMAI,
    SMARTAI
}