package entity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer

/**
 * A serializer for the ArrayDeque<GemType> data type.
 */
object ArrayDequeGemTypeSerializer : KSerializer<ArrayDeque<GemType>> {
    private val delegateSerializer = ListSerializer(GemType.serializer())

    override val descriptor = delegateSerializer.descriptor

    /**
     * Serializes an ArrayDeque<GemType> object to JSON by converting it to a list.
     *
     * @param encoder The encoder used for serialization.
     * @param value The `ArrayDeque<GemType>` to serialize.
     */
    override fun serialize(encoder: Encoder, value: ArrayDeque<GemType>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    /**
     * Deserializes a JSON into an ArrayDeque<GemType> object.
     *
     * @param decoder The decoder used for deserialization.
     * @return The deserialized ArrayDeque<GemType>.
     */
    override fun deserialize(decoder: Decoder): ArrayDeque<GemType> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}
