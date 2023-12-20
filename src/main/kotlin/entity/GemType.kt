package entity

import kotlinx.serialization.Serializable

/**
 * Enum to store the different Gem Types.
 * NONE is used when no gem is on a side on a tile.
 */
@Serializable
enum class GemType {
    SAPPHIRE,
    EMERALD,
    AMBER,
    NONE
}