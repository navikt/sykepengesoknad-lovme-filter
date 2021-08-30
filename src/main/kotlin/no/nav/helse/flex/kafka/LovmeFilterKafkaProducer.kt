package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import no.nav.helse.flex.service.LovmeFilterDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.stereotype.Component

@Component
class LovmeFilterKafkaProducer(
    private val producer: KafkaProducer<String, LovmeFilterDTO>
) {

    val log = logger()

    fun produserMelding(melding: LovmeFilterDTO): RecordMetadata {
        try {
            return producer.send(
                ProducerRecord(LOVME_FILTER_TOPIC, melding.id, melding)
            ).get()
        } catch (e: Throwable) {
            log.warn("Uventet feil ved publisering av filtrert søknad ${melding.id} på topic $LOVME_FILTER_TOPIC", e)
            throw e
        }
    }
}
