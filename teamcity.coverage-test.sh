set -euo pipefail

docker run --rm -v $(pwd):$(pwd) -w $(pwd) \
  -e G2B_NEXUS_USERNAME=$G2B_NEXUS_USERNAME \
  -e G2B_NEXUS_PASSWORD=$G2B_NEXUS_PASSWORD \
  -e G2B_NEXUS_HOST=$G2B_NEXUS_HOST \
  openjdk:8-jdk-alpine \
  ./gradlew :jacocoTestCoverageVerification --no-daemon
