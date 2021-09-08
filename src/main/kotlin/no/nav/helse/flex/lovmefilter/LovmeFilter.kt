package no.nav.helse.flex.lovmefilter

import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Service

/**
 * Service med eneste funksjonalitet å filtrere og videresende sykepengesøknaded til et topic opprettet for Team LovMe.
 */
@Service
class LovmeFilter(
    private val lovmeFilterProducer: LovmeFilterKafkaProducer
) {
    fun sendLovmeSoknad(sykepengeSoknadDTO: SykepengesoknadDTO) {
        if (soknadSkalSendeTeamLovMe(sykepengeSoknadDTO)) {
            lovmeFilterProducer.produserMelding(sykepengeSoknadDTO.tilLovmeSoknadDTO())
        }
    }
}

/**
 * Funksjon med som avgjør om en sykepengesøknad skal videresende til Team LovMe.
 */
fun soknadSkalSendeTeamLovMe(sykepengeSoknadDTO: SykepengesoknadDTO) =
    sykepengeSoknadDTO.status == SoknadsstatusDTO.SENDT &&
        sykepengeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE &&
        sykepengeSoknadDTO.sendtNav != null
