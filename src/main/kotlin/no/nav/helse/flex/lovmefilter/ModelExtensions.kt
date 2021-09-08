package no.nav.helse.flex.lovmefilter

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.felles.SykepengesoknadDTO

fun String.tilSykepengeSoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)

fun String.tilLovmeSoknadDTO(): LovmeSoknadDTO = objectMapper.readValue(this)

/**
 * Extension for SykepengesoknadDTO som mapper til LovmeSoknadDTO, inkludert logikk for hvordan
 * brukerspørsmål om ARBEID_UTENFOR_NORGE skal håndteres.
 *
 * @see LovmeFilter
 */
fun SykepengesoknadDTO.tilLovmeSoknadDTO(): LovmeSoknadDTO {
    // Hent svar på brukerspørsmål om arbeid utenfor Norge. Det skal være bare ett svar spørsmålet, så det
    // første elementet kan brukes hvis det finnes.
    val sporsmalDTO = this.sporsmal?.first { dto -> dto.tag.equals("ARBEID_UTENFOR_NORGE") }
    val arbeidUtenforNorge = when (sporsmalDTO?.svar?.get(0)?.verdi) {
        "JA" -> true
        "NEI" -> false
        else -> null
    }

    // Status og Type er typer som tilhører SykepengesoknadDTO, men de blir serialisert til JSON og ikke eksponert
    // utenfor prosjektet.
    return LovmeSoknadDTO(
        this.id,
        this.type,
        this.status,
        this.fnr,
        this.korrigerer,
        this.startSyketilfelle,
        this.sendtNav,
        this.fom,
        this.tom,
        arbeidUtenforNorge
    )
}
