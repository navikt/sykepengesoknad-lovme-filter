package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import no.nav.helse.flex.lovmefilter.LovmeFilter
import no.nav.helse.flex.lovmefilter.tilSykepengeSoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * Klasse for å lytte på topic for inkommende sykepengesøknader.
 *
 * @see SYKEPENGESOKNAD_TOPIC
 */
@Component
class SykepengesoknadKafkaListener(
    private val lovMeFilter: LovmeFilter,
) {

    private val log = logger()

    @KafkaListener(topics = [SYKEPENGESOKNAD_TOPIC])
    fun listen(consumerRecord: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            lovMeFilter.sendLovmeSoknad(consumerRecord.value().tilSykepengeSoknadDTO())
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error(
                "Feil ved mottak av record med [key=${consumerRecord.key()}, [offset=${consumerRecord.offset()}] og " +
                    "[partition=${consumerRecord.partition()}].",
                e
            )
            throw e
        }
    }
}
