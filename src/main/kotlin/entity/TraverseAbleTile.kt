package entity

/**
 * Interface representing the ability of a [Tile]
 * to be traversable by gems, thus having [gemPositions]
 */
interface TraverseAbleTile {
    val gemPositions: MutableList<GemType>
}