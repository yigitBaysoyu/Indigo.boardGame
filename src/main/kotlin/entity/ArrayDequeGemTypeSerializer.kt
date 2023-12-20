package entity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer


object ArrayDequeGemTypeSerializer : KSerializer<ArrayDeque<GemType>> {
    private val delegateSerializer = ListSerializer(GemType.serializer())

    override val descriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArrayDeque<GemType>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ArrayDeque<GemType> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}
