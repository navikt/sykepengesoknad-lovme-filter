package no.nav.helse.flex.service

import no.nav.helse.flex.AbstractContainerBaseTest
import no.nav.helse.flex.hentRecords
import no.nav.helse.flex.kafka.LOVME_FILTER_TOPIC
import no.nav.helse.flex.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.lyttPaaTopic
import no.nav.helse.flex.ventPaaRecords
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.amshove.kluent.shouldBeEmpty
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integrasjonstest som starter applikasjonen og en Kafka Docker container. Verifiserer at meldinger blir sendt,
 * mottatt, filtrert og videresendt som forventet.
 *
 * @see LovmeFilterService
 */
class LovmeFilterIntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var lovmeFilterKafkaConsumer: Consumer<String, String>

    @Autowired
    private lateinit var sykepengeSoknadTestProducer: KafkaProducer<String, SykepengesoknadDTO>

    @Autowired
    private lateinit var lovmeFilterService: LovmeFilterService

    @BeforeAll
    fun subscribeTilLovmeFilterTopic() {
        lovmeFilterKafkaConsumer.lyttPaaTopic(LOVME_FILTER_TOPIC)
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
    fun `Videresend kun søknader med type ARBEIDSTAKERE til LovMe topic`() {
        val sykepengeSoknader = listOf(
            SykepengesoknadDTO(ID, SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.OPPHOLD_UTLAND, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ANNET_ARBEIDSFORHOLD, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSLEDIG, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.BEHANDLINGSDAGER, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.REISETILSKUDD, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.GRADERT_REISETILSKUDD, SoknadsstatusDTO.SENDT, FNR),
        )

        sykepengeSoknader.forEach { soknad ->
            lovmeFilterService.sendLovmeSoknad(soknad)
        }

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.type).isEqualTo(SoknadstypeDTO.ARBEIDSTAKERE)
    }

    @Test
    fun `Videresend kun søknader med status SENDT til lovme topic`() {
        val soknader = listOf(
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.KORRIGERT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.NY, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.FREMTIDIG, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.AVBRUTT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SLETTET, FNR)
        )

        soknader.forEach { soknad ->
            lovmeFilterService.sendLovmeSoknad(soknad)
        }

        val consumerRecord = lovmeFilterKafkaConsumer.ventPaaRecords(antall = 1).first()
        val lovmeSoknadDTO = consumerRecord.value().tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.status).isEqualTo(SoknadsstatusDTO.SENDT)
    }
}

private const val ID = "4d4e41de-5c19-4e2d-b408-b809c37e6cfa"
private const val FNR = "01010112345"