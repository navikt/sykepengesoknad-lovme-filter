package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import no.nav.helse.flex.service.LovmeFilterService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SykepengesoknadKafkaListener(
    private val lovMeFilterService: LovmeFilterService,
) {

    private val log = logger()

    @KafkaListener(topics = [FLEX_SYKEPENGESOKNAD_TOPIC])
    fun listen(consumerRecord: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            log.debug("Mottok melding med key: {}", consumerRecord.key())
            lovMeFilterService.filtrerLovmeSoknad(consumerRecord.value())
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error(
                "Feil ved mottak av record med " +
                    "key: ${consumerRecord.key()}, " +
                    "offset: ${consumerRecord.offset()}, " +
                    "partition: ${consumerRecord.partition()}.",
                e
            )
            throw e
        }
    }
}
