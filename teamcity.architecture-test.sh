set -euo pipefail

# architecture tests
docker run --rm -v $(pwd):$(pwd) -w $(pwd) \
  -e G2B_NEXUS_USERNAME=$G2B_NEXUS_USERNAME \
  -e G2B_NEXUS_PASSWORD=$G2B_NEXUS_PASSWORD \
  -e G2B_NEXUS_HOST=$G2B_NEXUS_HOST \
  -v /var/run/docker.sock:/var/run/docker.sock openjdk:8-jdk-alpine \
  ./gradlew :runArchitectureTests --no-daemon
