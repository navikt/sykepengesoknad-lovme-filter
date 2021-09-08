package no.nav.helse.flex

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.awaitility.Awaitility
import java.time.Duration

/**
 * Extension brukt til kosumering av meldinger på Kafka topics i tester.
 */
fun <K, V> Consumer<K, V>.lyttPaaTopic(vararg topics: String) {
    if (this.subscription().isEmpty()) {
        this.subscribe(listOf(*topics))
    }
}

fun <K, V> Consumer<K, V>.ventPaaRecords(
    antall: Int,
    duration: Duration = Duration.ofMillis(1000),
): List<ConsumerRecord<K, V>> {
    val factory = if (antall == 0) {
        // Venter hele perioden for å verifisere at det ikke kommer inn meldinger.
        Awaitility.await().during(duration)
    } else {
        Awaitility.await().atMost(duration)
    }

    val alleRecords = ArrayList<ConsumerRecord<K, V>>()
    factory.until {
        alleRecords.addAll(this.hentRecords())
        alleRecords.size == antall
    }
    return alleRecords
}

fun <K, V> Consumer<K, V>.hentRecords(duration: Duration = Duration.ofMillis(100)): List<ConsumerRecord<K, V>> {
    return this.poll(duration).also {
        this.commitSync()
    }.iterator().asSequence().toList()
}
