#!/usr/bin/env bash
#
# Builds, signs, and uploads release AARs to https://oss.sonatype.org/#stagingRepositories.
#

echo -e "machine github.com\n  login $GITHUB_USERNAME\n  password $GITHUB_PASSWORD" >> ~/.netrc
git clone https://github.com/mapzen/android-config.git
./gradlew uploadArchives -PsonatypeUsername="$SONATYPE_NEXUS_SNAPSHOTS_USERNAME" \
    -PsonatypePassword="$SONATYPE_NEXUS_SNAPSHOTS_PASSWORD" \
    -Psigning.keyId="$SIGNING_KEY_ID" \
    -Psigning.password="$SIGNING_PASSWORD" \
    -Psigning.secretKeyRingFile="$SIGNING_SECRET_KEY_RING_FILE"
