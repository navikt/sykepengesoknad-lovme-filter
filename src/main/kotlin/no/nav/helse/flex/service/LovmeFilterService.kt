package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Service

@Service
class LovmeFilterService(
    private val lovmeFilterProducer: LovmeFilterKafkaProducer
) {

    val log = logger()

    fun filtrerLovmeSoknad(sykepengesoknadString: String) {
        val sykepengesoknadDTO = sykepengesoknadString.tilSykepengesoknadDTO()
        log.debug("Mottok søknad ${sykepengesoknadDTO.id} med status ${sykepengesoknadDTO.status}")
        val toLovmeFilterDTO = sykepengesoknadDTO.toLovmeFilterDTO()
        lovmeFilterProducer.produserMelding(toLovmeFilterDTO)
        log.debug("Videresendt filtrert sykepengsoknad med id ${toLovmeFilterDTO.id}")
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
    fun SykepengesoknadDTO.toLovmeFilterDTO(): LovmeFilterDTO = LovmeFilterDTO(this.id, this.fnr)
}

data class LovmeFilterDTO(
    val id: String,
    val fnr: String
)
