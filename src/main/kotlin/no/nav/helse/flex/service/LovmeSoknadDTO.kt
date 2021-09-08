package no.nav.helse.flex.service

import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO for filterte sykepengesøknader sendt til Team LovMe og tilhørende extensions.
 */
data class LovmeSoknadDTO(
    val id: String,
    // DTO med type fra ekstern DTO, men DTO blir serialisert til JSON, og blir ikke eksponert ut av prosjektet.
    val type: SoknadstypeDTO,
    // DTO med type fra ekstern DTO, men DTO blir serialisert til JSON, og blir ikke eksponert ut av prosjektet.
    val status: SoknadsstatusDTO,
    val fnr: String,
    val korrigerer: String? = null,
    val startSyketilfelle: LocalDate? = null,
    val sendtNav: LocalDateTime? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
    // Kun True eller False hvis bruker har svar JA eller NEI.
    val arbeidUtenforNorge: Boolean? = null
)
