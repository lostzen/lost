#!/usr/bin/env bash
#
# Builds and uploads snapshot AARs to https://oss.sonatype.org/content/repositories/snapshots/com/mapzen/.
#
version=$(./gradlew -q printVersion)
if [[ $version =~ .*-SNAPSHOT.* ]]
then
  ./gradlew uploadArchives -PsonatypeUsername=$SONATYPE_NEXUS_SNAPSHOTS_USERNAME  \
      -PsonatypePassword=$SONATYPE_NEXUS_SNAPSHOTS_PASSWORD
  break
fi
