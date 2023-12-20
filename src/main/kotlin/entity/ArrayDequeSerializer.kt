package entity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer

object ArrayDequeSerializer : KSerializer<ArrayDeque<Turn>> {
    private val delegateSerializer = ListSerializer(Turn.serializer())

    override val descriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArrayDeque<Turn>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ArrayDeque<Turn> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}


