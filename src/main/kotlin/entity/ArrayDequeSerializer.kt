package entity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer

/**
 * A serializer for the `ArrayDeque<Turn>` data type.
 */
object ArrayDequeSerializer : KSerializer<ArrayDeque<Turn>> {
    private val delegateSerializer = ListSerializer(Turn.serializer())

    override val descriptor = delegateSerializer.descriptor

    /**
     * Serializes an `ArrayDeque<Turn>` object to JSON by converting it to a list.
     *
     * @param encoder The encoder used for serialization.
     * @param value The `ArrayDeque<Turn>` to serialize.
     */
    override fun serialize(encoder: Encoder, value: ArrayDeque<Turn>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    /**
     * Deserializes a JSON into an `ArrayDeque<Turn>` object.
     *
     * @param decoder The decoder used for deserialization.
     * @return The deserialized `ArrayDeque<Turn>`.
     */
    override fun deserialize(decoder: Decoder): ArrayDeque<Turn> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}


