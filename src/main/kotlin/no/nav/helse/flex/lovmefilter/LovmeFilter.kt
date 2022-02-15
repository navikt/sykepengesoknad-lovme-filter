package no.nav.helse.flex.lovmefilter

import no.nav.helse.flex.kafka.LOVME_FILTER_TOPIC
import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.*
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
        if (soknadSkalSendesTeamLovMe(sykepengeSoknadDTO)) {
            val lovmeSoknadDTO = sykepengeSoknadDTO.tilLovmeSoknadDTO()
            lovmeFilterProducer.produserMelding(lovmeSoknadDTO)

            log.info(
                "Sendt filtrert sykepengesøknad med: " +
                    "[id=${lovmeSoknadDTO.id}], " +
                    "[status=${lovmeSoknadDTO.status}] og " +
                    "[type=${lovmeSoknadDTO.type}] til " +
                    "[topic=$LOVME_FILTER_TOPIC]."
            )
        }
    }
}

/**
 * Funksjon med som avgjør om en sykepengesøknad skal videresende til Team LovMe.
 */
fun soknadSkalSendesTeamLovMe(sykepengeSoknadDTO: SykepengesoknadDTO) =
    sykepengeSoknadDTO.status == SoknadsstatusDTO.SENDT &&
        sykepengeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE &&
        sykepengeSoknadDTO.sendtNav != null && !sykepengeSoknadDTO.ettersending
