package no.nav.helse.flex.lovmefilter

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.*

fun String.tilSykepengeSoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)

fun String.tilLovmeSoknadDTO(): LovmeSoknadDTO = objectMapper.readValue(this)

/**
 * Extension for SykepengesoknadDTO som mapper til LovmeSoknadDTO, inkludert logikk for hvordan
 * brukerspørsmål om ARBEID_UTENFOR_NORGE skal håndteres.
 *
 * @see LovmeFilter
 */
fun SykepengesoknadDTO.tilLovmeSoknadDTO(): LovmeSoknadDTO {
    // Hent svar på brukerspørsmål om arbeid utenfor Norge.
    val sporsmalDTO = this.sporsmal?.firstOrNull() { dto -> dto.tag.equals("ARBEID_UTENFOR_NORGE") }
    val arbeidUtenforNorge = when (sporsmalDTO?.svar?.firstOrNull()?.verdi) {
        "JA" -> true
        "NEI" -> false
        else -> null
    }

    // Status og Type er typer som tilhører SykepengesoknadDTO, men de blir serialisert til JSON og ikke eksponert
    // utenfor prosjektet.
    return LovmeSoknadDTO(
        id = this.id,
        type = this.type,
        status = this.status,
        fnr = this.fnr,
        korrigerer = this.korrigerer,
        // Dato for start av syketilfelle kan aldri være null.
        startSyketilfelle = this.startSyketilfelle!!,
        // Vi vet sendtNav ikke er null siden den er brukt i filtrering.
        sendtNav = this.sendtNav!!,
        // Det er kun utlandssøknad som ikke har fom og tom satt.
        fom = this.fom!!,
        tom = this.tom!!,
        arbeidUtenforNorge = arbeidUtenforNorge
    )
}
