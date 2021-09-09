package no.nav.helse.flex.lovmefilter

import no.nav.helse.flex.kafka.LOVME_FILTER_TOPIC
import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.helse.flex.logger
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

    val log = logger()

    fun sendLovmeSoknad(sykepengeSoknadDTO: SykepengesoknadDTO) {
        if (soknadSkalSendeTeamLovMe(sykepengeSoknadDTO)) {
            val lovmeSoknadDTO = sykepengeSoknadDTO.tilLovmeSoknadDTO()
            lovmeFilterProducer.produserMelding(lovmeSoknadDTO)

            log.info(
                "Sendt filtrert sykepengesøknad med" +
                    "[id=${lovmeSoknadDTO.id}], " +
                    "[status=${lovmeSoknadDTO.status}], " +
                    "[type=${lovmeSoknadDTO.type}] og " +
                    "[sendtNav=${lovmeSoknadDTO.sendtNav}] til " +
                    "[topic=$LOVME_FILTER_TOPIC]."
            )
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
