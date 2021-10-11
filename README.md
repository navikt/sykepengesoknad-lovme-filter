# sykepengesoknad-lovme-filter

Applikasjon som lytter på Kafka-topic for sykepengesøknader. Søknader med gitt status og type blir videresendt i en redusert på et nytt topic som applikasjonen `medlemskap-sykepenger-listener`, eid av Team LovMe, lytter på.

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

Verdier fra en `SykepengesoknadDTO` brukes til å konstruere følgende LovmeSoknadDTO:

```kotlin
data class LovmeSoknadDTO(
   val id: String,
   val type: SoknadstypeDTO,
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
```
`SoknadstypeDTO` og `SoknadsstatusDTO` ligger i [https://github.com/navikt/syfokafka](https://github.com/navikt/syfokafka) og ser sånn ut:

SoknadstypeDTO:

```kotlin
enum class SoknadstypeDTO {
    SELVSTENDIGE_OG_FRILANSERE,
    OPPHOLD_UTLAND,
    ARBEIDSTAKERE,
    ANNET_ARBEIDSFORHOLD,
    ARBEIDSLEDIG,
    BEHANDLINGSDAGER,
    REISETILSKUDD,
    GRADERT_REISETILSKUDD,
}
```

SoknadstypeDTO:

```kotlin
enum class SoknadsstatusDTO {
    NY,
    SENDT,
    FREMTIDIG,
    KORRIGERT,
    AVBRUTT,
    SLETTET
}
```

Eksempel på melding som JSON: 

```json
{
  "id" : "8f0b25ab-0fad-497d-960a-94370345d269",
  "type" : "ARBEIDSTAKERE",
  "status" : "SENDT",
  "fnr" : "01010112345",
  "korrigerer" : null,
  "startSyketilfelle" : "2021-09-01",
  "sendtNav" : "2021-10-11T15:35:53.471168",
  "fom" : "2021-09-01",
  "tom" : "2021-09-30",
  "arbeidUtenforNorge" : false
}
```


## Videresendingsregler

1. Viderersending fra topic `flex.sykepengesoknad` til `flex.sykepengesoknad-lovme-filter` skjer kun
   hvis:
   1. `status` er `SoknadsstatusDTO.SENDT`
   2. `type` er `SoknadstypeDTO.ARBEIDSTAKERE`
   3. `sendtNav` er satt
2. Hvis søknaden korrigerer en tidligere søknad får den ny `id`, men feltet `korrigerer` vil inneholde verdien av den
   opprinnelige søknaden.
3. Hvis bruker har svar `JA` på spørsmål om arbeid i utlandet, vil `arbeidUtenforNorge` være `true`. `NEI` gir  `false`, 
   og verdien er `null` hvis spørsmålet ikke er besvart.

