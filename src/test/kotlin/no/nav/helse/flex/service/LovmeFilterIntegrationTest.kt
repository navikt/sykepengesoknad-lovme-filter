package no.nav.helse.flex.service

import no.nav.helse.flex.AbstractContainerBaseTest
import no.nav.helse.flex.hentRecords
import no.nav.helse.flex.kafka.LOVME_FILTER_TOPIC
import no.nav.helse.flex.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.lyttPaaTopic
import no.nav.helse.flex.ventPaaRecords
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.amshove.kluent.shouldBeEmpty
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integrasjonstest som starter applikasjonen og en Kafka Docker container. Verifiserer at meldinger blir mottatt,
 * filtrert og videresendt som forventet.
 *
 * @see LovmeFilterService
 */
@Testcontainers
class LovmeFilterIntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var lovmeFilterKafkaConsumer: Consumer<String, String>

    @Autowired
    private lateinit var sykepengeSoknadTestProducer: KafkaProducer<String, SykepengesoknadDTO>

    @BeforeAll
    fun subscribeTilLovmeFilterTopic() {
        lovmeFilterKafkaConsumer.lyttPaaTopic(LOVME_FILTER_TOPIC)
        lovmeFilterKafkaConsumer.hentRecords().shouldBeEmpty()
    }

    @Test
    fun testAtSykepengesoknadFiltreresOgVideresendes() {
        val sykepengesoknadString = readResouceFile("/sykepengesoknad.json")
        val sykepengesoknadDTO = sykepengesoknadString.tilSykepengeSoknadDTO()

        sykepengeSoknadTestProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                sykepengesoknadDTO.id,
                sykepengesoknadDTO
            )
        )

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.id).isEqualTo(sykepengesoknadDTO.id)
        assertThat(lovmeSoknadDTO.type).isEqualTo(sykepengesoknadDTO.type)
        assertThat(lovmeSoknadDTO.status).isEqualTo(sykepengesoknadDTO.status)
        assertThat(lovmeSoknadDTO.fnr).isEqualTo(sykepengesoknadDTO.fnr)
        assertThat(lovmeSoknadDTO.startSyketilfelle).isEqualTo(sykepengesoknadDTO.startSyketilfelle)
        assertThat(lovmeSoknadDTO.sendtNav).isEqualTo(sykepengesoknadDTO.sendtNav)
        assertThat(lovmeSoknadDTO.fom).isEqualTo(sykepengesoknadDTO.fom)
        assertThat(lovmeSoknadDTO.tom).isEqualTo(sykepengesoknadDTO.tom)
        assertThat(lovmeSoknadDTO.korrigerer).isEqualTo(sykepengesoknadDTO.korrigerer)
        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isTrue
    }

    private fun readResouceFile(fileName: String) = this::class.java.getResource(fileName).readText(Charsets.UTF_8)
}
