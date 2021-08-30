package no.nav.helse.flex.service

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import no.nav.helse.flex.kafka.LovmeFilterKafkaProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LovMeFilterServiceTest {

    private val lovmeProducer: LovmeFilterKafkaProducer = mock()
    private val lovmeFilterService = LovmeFilterService(lovmeProducer)

    @Test
    fun lovmeFiltrerSoknad() {
        val sykepengesoknadString = readResouceFile("/sykepengesoknad.json")

        lovmeFilterService.filtrerLovmeSoknad(sykepengesoknadString)

        argumentCaptor<LovmeFilterDTO>().apply {
            verify(lovmeProducer).produserMelding(capture())
            val (id, fnr) = firstValue
            assertThat(id).isEqualTo("4d4e41de-5c19-4e2d-b408-b809c37e6cfa")
            assertThat(fnr).isEqualTo("01010112345")
        }
    }

    private fun readResouceFile(fileName: String) = this::class.java.getResource(fileName).readText(Charsets.UTF_8)
}
