package no.nav.helse.flex.service

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Tester som verifiserer at regler for filtrering og videresending av sykepengesøknader til Team LovMe fungerer som
 * forventet.
 *
 * @see LovmeFilterService
 */
class LovmeFilterServiceTest {

    private val lovmeKafkaProducer: LovmeFilterKafkaProducer = mock()
    private val lovmeFilterService = LovmeFilterService(lovmeKafkaProducer)

    @Test
    fun sendLovemeSoknad_kunSoknaderMedStatusSendt_blirVideresendt() {
        val soknader = listOf(
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.KORRIGERT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.NY, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.FREMTIDIG, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.AVBRUTT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SLETTET, FNR)
        )

        soknader.forEach { soknad ->
            lovmeFilterService.sendLovmeSoknad(soknad.tilString())
        }

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            val sendteSoknader = allValues
            assertThat(sendteSoknader).hasSize(1)
            assertThat(sendteSoknader.first { soknad -> soknad.status == SoknadsstatusDTO.SENDT }).isNotNull
        }
    }

    @Test
    fun sendLovemeSoknad_kunSoknaderMed_typeArbeidstakere_blirVideresendt() {
        val soknader = listOf(
            SykepengesoknadDTO(ID, SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.OPPHOLD_UTLAND, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSTAKERE, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ANNET_ARBEIDSFORHOLD, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.ARBEIDSLEDIG, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.BEHANDLINGSDAGER, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.REISETILSKUDD, SoknadsstatusDTO.SENDT, FNR),
            SykepengesoknadDTO(ID, SoknadstypeDTO.GRADERT_REISETILSKUDD, SoknadsstatusDTO.SENDT, FNR),
        )

        soknader.forEach { soknad ->
            lovmeFilterService.sendLovmeSoknad(soknad.tilString())
        }

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            val sendteSoknader = allValues
            assertThat(sendteSoknader).hasSize(1)
            assertThat(sendteSoknader.first { soknad -> soknad.type == SoknadstypeDTO.ARBEIDSTAKERE }).isNotNull
        }
    }

    @Test
    fun sendLovemeSoknad_brukersposmaalArbeidUtland_svarJa_mappetTilTrue() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("JA"))))
        )

        lovmeFilterService.sendLovmeSoknad(sykepengesoknadDTO.tilString())

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            assertThat(firstValue.arbeidUtenforNorge).isEqualTo(true)
        }
    }

    @Test
    fun sendLovemeSoknad_brukersposmaalArbeidUtland_svarNei_mappetTilFalse() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("NEI"))))
        )

        lovmeFilterService.sendLovmeSoknad(sykepengesoknadDTO.tilString())

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            assertThat(firstValue.arbeidUtenforNorge).isEqualTo(false)
        }
    }

    @Test
    fun sendLovemeSoknad_brukersposmaalArbeidUtland_manglerSvar_mappetTilFalse() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR
        )

        lovmeFilterService.sendLovmeSoknad(sykepengesoknadDTO.tilString())

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            assertThat(firstValue.arbeidUtenforNorge).isEqualTo(false)
        }
    }

    @Test
    fun sendLovemeSoknad_brukersposmaalArbeidUtland_nullverdi_mappetTilFalse() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = null))
        )

        lovmeFilterService.sendLovmeSoknad(sykepengesoknadDTO.tilString())

        argumentCaptor<LovmeSoknadDTO>().apply {
            verify(lovmeKafkaProducer).produserMelding(capture())
            assertThat(firstValue.arbeidUtenforNorge).isEqualTo(false)
        }
    }
}

private const val ID = "4d4e41de-5c19-4e2d-b408-b809c37e6cfa"
private const val FNR = "01010112345"
