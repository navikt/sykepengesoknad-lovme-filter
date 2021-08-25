package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Service

@Service
class LovMeFilterService {

    val log = logger()

    fun filtrerSoknad(soknadString: String) {
        val soknad = soknadString.tilSykepengesoknadDTO()
        log.info("Mottok søknad ${soknad.id} med status ${soknad.status}")
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}
