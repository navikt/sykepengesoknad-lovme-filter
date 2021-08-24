echo "Bygger Docker image sykepengesoknad-lovme-filter:latest"

./gradlew bootJar

docker build . -t sykepengesoknad-lovme-filter:latest
