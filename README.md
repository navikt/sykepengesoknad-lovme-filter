# sykepengesoknad-lovme-filter

Applikasjon som lytter på Kafka-topic for sykepengesøknader videresender en redusert versjon av meldigner med en gitt
status til en topic ment for Team LovMe.

## Rettigheter

Topic `flex.sykepengesoknad-lovme-filter` er definert i applikasjonen `sykepengesoknad-lovme-filter` (denne), og eies av
Team Flex. Følgende rettigheter er gitt:

```yaml
acl:
  - team: lovme
    application: medlemskap-sykepenger-listener
    access: read
```

## Funksjonalitet

Verdier fra en `SykepengesoknadDTO` brukees til å konstruere følgende LovmeSoknadDTO:

```kotlin
data class LovmeSoknadDTO(
    val id: String,
    val type: String,
    val status: String,
    val fnr: String,
    val korrigerer: String? = null,
    val startSyketilfelle: LocalDate? = null,
    val sendtNav: LocalDateTime? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
    // Kun True eller False hvis bruker har svar JA eller NEI.
    val arbeidUtenforNorge: Boolean? = null
)
```

Følgende regler gjelder:

1. Viderersending fra topic `flex.sykepengesoknad` til `flex.sykepengesoknad-lovme-filter` skjer kun
   hvis `status? er "SENDT" og `type` er "ARBEIDSTAKERE".`
2. Hvis søknaden korrigerer en tidligere søknad får den ny `id`, og feltet `korrigerer` vil inneholde verdien av den
   korrigerte søknaden.
3. Hvis bruker har svar `JA` på spørsmål om arbeid i utlandet, vil `arbeidUtenforNorge` være `true`. Ellers `false`.

