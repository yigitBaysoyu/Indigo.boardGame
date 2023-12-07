package entity

abstract class Tile(
    val connections: List<Pair<Int, Int>>,
    val rotationOffset: Int,
) {
    var position: Pair<Int, Int>? = null
}