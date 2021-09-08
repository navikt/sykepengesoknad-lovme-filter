package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import no.nav.helse.flex.lovmefilter.LovmeSoknadDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.stereotype.Component

/**
 * Klasse for å videresende filtrerte sykepengesøknader på utgående topic.
 *
 * @see LOVME_FILTER_TOPIC
 */
@Component
class LovmeFilterKafkaProducer(
    private val producer: KafkaProducer<String, LovmeSoknadDTO>
) {

    val log = logger()

    fun produserMelding(lovmeSoknadDTO: LovmeSoknadDTO): RecordMetadata {
        try {
            val recordMetadata = producer.send(
                ProducerRecord(LOVME_FILTER_TOPIC, lovmeSoknadDTO.id, lovmeSoknadDTO)
            ).get()

            log.info(
                "Sendt filtrert sykepengesøknad med [id=${lovmeSoknadDTO.id}], [status=${lovmeSoknadDTO.status}] " +
                    "og [type=${lovmeSoknadDTO.type}] til [topic=$LOVME_FILTER_TOPIC]."
            )
            return recordMetadata
        } catch (e: Throwable) {
            log.error(
                "Feil ved sending av filtrert søknad med [id=${lovmeSoknadDTO.id}] til [topic=$LOVME_FILTER_TOPIC].",
                e
            )
            throw e
        }
    }
}
