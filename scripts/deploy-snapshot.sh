#!/usr/bin/env bash
#
# Builds and uploads snapshot AARs to https://oss.sonatype.org/content/repositories/snapshots/com/mapzen/.
#

while read -r line || [[ -n "$line" ]]; do
  if [[ $line =~ .*version=.*-SNAPSHOT.* ]]
  then
    ./gradlew uploadArchives -PsonatypeUsername=$SONATYPE_NEXUS_SNAPSHOTS_USERNAME  \
        -PsonatypePassword=$SONATYPE_NEXUS_SNAPSHOTS_PASSWORD
    break
  fi
done < "gradle.properties"