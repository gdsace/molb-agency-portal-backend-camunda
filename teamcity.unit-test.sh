set -euo pipefail

# unit tests
docker run --rm -v $(pwd):$(pwd) -w $(pwd) \
  -e G2B_NEXUS_USERNAME=$G2B_NEXUS_USERNAME \
  -e G2B_NEXUS_PASSWORD=$G2B_NEXUS_PASSWORD \
  -e G2B_NEXUS_HOST=$G2B_NEXUS_HOST \
  adoptopenjdk/openjdk11:jdk-11.0.15_10-alpine \
  ./gradlew runUnitTests :jacocoTestCoverageVerification --no-daemon
