package no.nav.helse.flex

import no.nav.helse.flex.kafka.JacksonKafkaSerializer
import no.nav.helse.flex.kafka.KafkaConfig
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaTestConfig(private val kafkaConfig: KafkaConfig) {

    @Bean
    fun kafkaConsumer() = KafkaConsumer<String, String>(
        mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to "testing-group-id",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ) + kafkaConfig.commonConfig()
    )

    @Bean
    fun sykepengeSoknadTestProducer() = KafkaProducer<String, SykepengesoknadDTO>(
        mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonKafkaSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 100
        ) + kafkaConfig.commonConfig()
    )
}
