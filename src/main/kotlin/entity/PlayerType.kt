package entity

enum class PlayerType {
    LOCALPLAYER,
    RANDOMAI,
    HARDAI,
    NETWORKPLAYER,
    ;

    override fun toString() = when(this) {
        PlayerType.LOCALPLAYER -> "L"
        PlayerType.RANDOMAI -> "rAI"
        PlayerType.HARDAI -> "hAI"
        PlayerType.NETWORKPLAYER -> "N"
    }
}