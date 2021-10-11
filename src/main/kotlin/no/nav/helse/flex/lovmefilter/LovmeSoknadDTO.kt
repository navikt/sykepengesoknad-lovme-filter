package no.nav.helse.flex.lovmefilter

import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO for filterte sykepengesøknader sendt til Team LovMe og tilhørende extensions.
 */
data class LovmeSoknadDTO(
    val id: String,
    // DTO fra https://github.com/navikt/syfokafka, men blir serialisert til JSON sånn at den
    // ikke blir ikke eksponert ut av prosjektet.
    val type: SoknadstypeDTO,
    // DTO fra https://github.com/navikt/syfokafka, men blir serialisert til JSON sånn at den
    // ikke blir ikke eksponert ut av prosjektet.
    val status: SoknadsstatusDTO,
    val fnr: String,
    val korrigerer: String? = null,
    val startSyketilfelle: LocalDate,
    val sendtNav: LocalDateTime,
    val fom: LocalDate,
    val tom: LocalDate,
    // Kun True eller False hvis bruker har svar JA eller NEI.
    val arbeidUtenforNorge: Boolean? = null
)
