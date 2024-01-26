import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A serializer for the ArrayDeque<Pair<Pair<Int, Int>, Int>> data type.
 */
object PairDequeSerializer : KSerializer<ArrayDeque<Pair<Pair<Int, Int>, Int>>> {
    private val delegateSerializer = ListSerializer(
        PairSerializer(PairSerializer(Int.serializer(), Int.serializer()), Int.serializer())
    )

    override val descriptor = delegateSerializer.descriptor

    /**
     * Serializes an ArrayDeque<Pair<Pair<Int, Int>, Int>> object to JSON by converting it to a list.
     *
     * @param encoder The encoder used for serialization.
     * @param value The ArrayDeque<Turn> to serialize.
     */
    override fun serialize(encoder: Encoder, value: ArrayDeque<Pair<Pair<Int, Int>, Int>>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    /**
     * Deserializes a JSON into an ArrayDeque<Pair<Pair<Int, Int>, Int>> object.
     *
     * @param decoder The decoder used for deserialization.
     * @return The deserialized ArrayDeque<Turn>.
     */
    override fun deserialize(decoder: Decoder): ArrayDeque<Pair<Pair<Int, Int>, Int>> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}