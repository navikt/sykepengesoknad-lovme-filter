package no.nav.helse.flex.lovmefilter

import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
        val sykepengesoknadDTO = templateDTO.copy(
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("JA")))),
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isEqualTo(true)
    }

    @Test
    fun `Svar NEI på brukerspørsmål om arbeidet i utlandet blir mappet til FALSE`() {
        val sykepengesoknadDTO = templateDTO.copy(
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO("NEI")))),
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isEqualTo(false)
    }

    @Test
    fun `Brukerspørsmål om arbeidet i utlandet er ikke besvart returnerer NULL`() {
        val sykepengesoknadDTO = templateDTO.copy(
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = listOf(SvarDTO(null)))),
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isNull()
    }

    @Test
    fun `Brukerspørsmål om arbeidet i utlandet ikke er besvart i automatisk innsendt søknad returnerer NULL`() {
        val sykepengesoknadDTO = templateDTO.copy(
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ARBEID_UTENFOR_NORGE", svar = emptyList())),
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isNull()
    }

    @Test
    fun `Brukerspørsmål om arbeidet i utlandet returnerer NULL hvis spørsmålet ikke finnes`() {
        val sykepengesoknadDTO = templateDTO.copy(
            sporsmal = listOf(SporsmalDTO(id = "1", tag = "ANNET", svar = listOf(SvarDTO(null)))),
        )

        val lovmeSoknadDTO = sykepengesoknadDTO.tilLovmeSoknadDTO()

        assertThat(lovmeSoknadDTO.arbeidUtenforNorge).isNull()
    }

    @Test
    fun `Mapping til LovemeSoknadDTO kaster NullPointerException hvis startSyketilfelle ikke er satt`() {
        val sykepengesoknadDTO = templateDTO.copy(
            startSyketilfelle = null,
        )

        assertThatThrownBy {
            sykepengesoknadDTO.tilLovmeSoknadDTO()
        }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `Mapping til LovemeSoknadDTO kaster NullPointerException hvis fom ikke er satt`() {
        val sykepengesoknadDTO = templateDTO.copy(
            fom = null,
        )

        assertThatThrownBy {
            sykepengesoknadDTO.tilLovmeSoknadDTO()
        }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `Mapping til LovemeSoknadDTO kaster NullPointerException hvis tom ikke er satt`() {
        val sykepengesoknadDTO = templateDTO.copy(
            tom = null,
        )

        assertThatThrownBy {
            sykepengesoknadDTO.tilLovmeSoknadDTO()
        }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `Mapping til LovemeSoknadDTO kaster NullPointerException hvis sendtNav ikke er satt`() {
        val sykepengesoknadDTO = templateDTO.copy(
            sendtNav = null,
        )

        assertThatThrownBy {
            sykepengesoknadDTO.tilLovmeSoknadDTO()
        }.isInstanceOf(NullPointerException::class.java)
    }
}
