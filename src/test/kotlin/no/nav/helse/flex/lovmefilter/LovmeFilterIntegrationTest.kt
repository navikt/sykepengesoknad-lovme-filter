package no.nav.helse.flex.lovmefilter

import no.nav.helse.flex.AbstractContainerBaseTest
import no.nav.helse.flex.hentRecords
import no.nav.helse.flex.kafka.LOVME_FILTER_TOPIC
import no.nav.helse.flex.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.lyttPaaTopic
import no.nav.helse.flex.sykepengesoknad.kafka.*
import no.nav.helse.flex.ventPaaRecords
import org.amshove.kluent.shouldBeEmpty
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * Integrasjonstest som starter applikasjonen og en Kafka Docker container. Verifiserer at meldinger blir sendt,
 * mottatt, filtrert og videresendt som forventet.
 *
 * @see LovmeFilter
 */
class LovmeFilterIntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var lovmeFilterKafkaConsumer: Consumer<String, String>

    @Autowired
    private lateinit var sykepengeSoknadTestProducer: KafkaProducer<String, SykepengesoknadDTO>

    @Autowired
    private lateinit var lovmeFilter: LovmeFilter

    @BeforeAll
    fun subscribeTilLovmeFilterTopic() {
        lovmeFilterKafkaConsumer.lyttPaaTopic(LOVME_FILTER_TOPIC)
    }

    @BeforeEach
    @AfterEach
    fun sjekkAtTopicErTomt() {
        lovmeFilterKafkaConsumer.hentRecords().shouldBeEmpty()
    }

    @Test
    fun `Send søknad på sykepengesøknad topic og motta filtrerte søknad på LovMe topic`() {
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

    @Test
    fun `Kun søknader med type ARBEIDSTAKERE blir videresendt på LovMe topic`() {
        val sykepengeSoknader = listOf(
            templateDTO.copy(type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE),
            templateDTO.copy(type = SoknadstypeDTO.OPPHOLD_UTLAND),
            templateDTO.copy(type = SoknadstypeDTO.ARBEIDSTAKERE),
            templateDTO.copy(type = SoknadstypeDTO.ANNET_ARBEIDSFORHOLD),
            templateDTO.copy(type = SoknadstypeDTO.ARBEIDSLEDIG),
            templateDTO.copy(type = SoknadstypeDTO.BEHANDLINGSDAGER),
            templateDTO.copy(type = SoknadstypeDTO.REISETILSKUDD),
            templateDTO.copy(type = SoknadstypeDTO.GRADERT_REISETILSKUDD)
        )

        sykepengeSoknader.forEach { soknad ->
            lovmeFilter.sendLovmeSoknad(soknad)
        }

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.type).isEqualTo(SoknadstypeDTO.ARBEIDSTAKERE)
    }

    @Test
    fun `Kun søknader med status SENDT blir videresent til LovMe topic`() {
        val soknader = listOf(
            templateDTO.copy(status = SoknadsstatusDTO.SENDT),
            templateDTO.copy(status = SoknadsstatusDTO.KORRIGERT),
            templateDTO.copy(status = SoknadsstatusDTO.NY),
            templateDTO.copy(status = SoknadsstatusDTO.FREMTIDIG),
            templateDTO.copy(status = SoknadsstatusDTO.AVBRUTT),
            templateDTO.copy(status = SoknadsstatusDTO.SLETTET),
        )

        soknader.forEach { soknad ->
            lovmeFilter.sendLovmeSoknad(soknad)
        }

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.status).isEqualTo(SoknadsstatusDTO.SENDT)
    }

    @Test
    fun `Kun søknader send til NAV blir Videresend til LovMe topic`() {
        val soknader = listOf(
            templateDTO,
            templateDTO.copy(sendtNav = null),
        )

        soknader.forEach { soknad ->
            lovmeFilter.sendLovmeSoknad(soknad)
        }

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.sendtNav).isEqualTo(soknader[0].sendtNav)
    }

    @Test
    fun `Ettersendte søknader blir ikke videresent til LovMe topic`() {
        val soknad = templateDTO.copy(id = UUID.randomUUID().toString(), ettersending = true)
        lovmeFilter.sendLovmeSoknad(soknad)

        lovmeFilterKafkaConsumer.ventPaaRecords(0)
    }
}
