package no.nav.helse.flex.lovmefilter

import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import java.time.LocalDate
import java.time.LocalDateTime

val templateDTO = SykepengesoknadDTO(
    id = "4d4e41de-5c19-4e2d-b408-b809c37e6cfa",
    type = SoknadstypeDTO.ARBEIDSTAKERE,
    status = SoknadsstatusDTO.SENDT,
    fnr = "01010112345",
    sporsmal = listOf(),
    startSyketilfelle = LocalDate.of(2021, 1, 1),
    fom = LocalDate.of(2021, 1, 1),
    tom = LocalDate.of(2021, 1, 31),
    sendtNav = LocalDateTime.of(2021, 1, 2, 12, 0),
    ettersending = false
)
