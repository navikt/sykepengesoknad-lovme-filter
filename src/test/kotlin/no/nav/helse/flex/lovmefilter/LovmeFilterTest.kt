package no.nav.helse.flex.lovmefilter

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
 * @see LovmeFilter
 */
class LovmeFilterTest {

    @Test
    fun `Svar JA på brukerspørsmål om arbeidet i utlandet blir mappet til TRUE`() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("JA"))))
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isEqualTo(true)
    }

    @Test
    fun `Svar NEI på brukerspørsmål om arbeidet i utlandet blir mappet til FALSE`() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("NEI"))))
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isEqualTo(false)
    }

    @Test
    fun `Brukerspørsmål om arbeidet i utlandet er ikke besvart returnerer NULL`() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = null))
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isNull()
    }

    @Test
    fun `Brukerspørsmål om arbeidet i utlandet returnerer NULL hvis spørsmålet ikke finnes`() {
        val sykepengesoknadDTO = SykepengesoknadDTO(
            ID,
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            FNR,
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ANNET", svar = listOf(SvarDTO("NEI"))))
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isNull()
    }
}

private const val ID = "4d4e41de-5c19-4e2d-b408-b809c37e6cfa"
private const val FNR = "01010112345"
