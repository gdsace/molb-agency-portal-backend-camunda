#!/usr/bin/env sh

echo "SPRING_PROFILES_ACTIVE ===== $MOLB_SPRING_PROFILES_ACTIVE"
echo "IMAGE_TAG ===== $IMAGE_TAG"

exec /bin/sh -c "java -Dspring.profiles.active=${MOLB_SPRING_PROFILES_ACTIVE} -Djava.security.egd=file:/dev/./urandom -Ddiagnostic.version=${IMAGE_TAG} -jar app.jar"
