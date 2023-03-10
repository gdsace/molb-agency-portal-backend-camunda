set -euo pipefail

# detekt, ktlint, arch tests (temporarily disabled)
docker run --rm -v $(pwd):$(pwd) -w $(pwd) \
  -e G2B_NEXUS_USERNAME=$G2B_NEXUS_USERNAME \
  -e G2B_NEXUS_PASSWORD=$G2B_NEXUS_PASSWORD \
  -e G2B_NEXUS_HOST=$G2B_NEXUS_HOST \
  adoptopenjdk/openjdk11:jdk-11.0.15_10-alpine \
  ./gradlew detekt ktlintCheck --no-daemon

# Terraform lint
#docker build -t terraform_lint -f deploy/tests/Dockerfile deploy
#docker run --rm --entrypoint terraform terraform_lint fmt -check=true
#docker run --rm terraform_lint .
