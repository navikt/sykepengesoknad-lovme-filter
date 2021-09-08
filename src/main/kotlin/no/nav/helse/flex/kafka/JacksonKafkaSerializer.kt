package no.nav.helse.flex.kafka

import no.nav.helse.flex.objectMapper
import org.apache.kafka.common.serialization.Serializer

/**
 * Klasse bruk til serialisering av DTO-klasser til JSON før sending på Kafka topics.
 *
 * @see KafkaConfig
 */
class JacksonKafkaSerializer<T : Any> : Serializer<T> {

    override fun serialize(topic: String?, data: T): ByteArray = objectMapper.writeValueAsBytes(data)
}
