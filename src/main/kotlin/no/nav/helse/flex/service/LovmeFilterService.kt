package no.nav.helse.flex.service

import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Service

/**
 * Service hvis eneste rolle er å filtrere og videresende sykepengesøknaded til et topic opprettet for Team LovMe.
 */
@Service
class LovmeFilterService(
    private val lovmeFilterProducer: LovmeFilterKafkaProducer
) {
    fun sendLovmeSoknad(sykepengeSoknadString: String) {
        val sykepengeSoknadDTO = sykepengeSoknadString.tilSykepengeSoknadDTO()

        if (soknadSkalVideresendes(sykepengeSoknadDTO)) {
            lovmeFilterProducer.produserMelding(sykepengeSoknadDTO.tilLovmeSoknadDTO())
        }
    }

    private fun soknadSkalVideresendes(sykepengeSoknadDTO: SykepengesoknadDTO) =
        sykepengeSoknadDTO.status == SoknadsstatusDTO.SENDT && sykepengeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE

    private fun SykepengesoknadDTO.tilLovmeSoknadDTO(): LovmeSoknadDTO {
        // Hent svar på brukerspørsmål om arbeid utenfor Norge. Det skal være bare ett svar spørsmålet, så det
        // første elementet kan brukes hvis det finnes.
        val sporsmalDTO = this.sporsmal?.first { dto -> dto.tag.equals("ARBEID_UTENFOR_NORGE") }
        val arbeidUtenforNorge = when (sporsmalDTO?.svar?.get(0)?.verdi) {
            "JA" -> true
            "NEI" -> false
            else -> false
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
}
