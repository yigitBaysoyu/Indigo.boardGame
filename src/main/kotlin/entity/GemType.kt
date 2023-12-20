package entity

/**
 * Enum to store the different Gem Types.
 * NONE is used when no gem is on a side on a tile.
 */
enum class GemType {
    SAPPHIRE,
    EMERALD,
    AMBER,
    NONE
    ;

    /**
     * provide an int to represent value of gem
     */
    fun toInt() = when(this) {
        SAPPHIRE -> 3
        EMERALD -> 2
        AMBER -> 1
        NONE -> 0
    }
}