#!/usr/bin/env bash
#
# runs the gradle release plugin
#

if ([ -z $RELEASE_VERSION_NUMBER ] || [ -z $NEW_VERSION_NUMBER ]); then
  echo "RELEASE_VERSION_NUMBER or NEW_VERSION_NUMBER not specified, skipping release."
else
  echo "Releasing"
  git config --global user.email "accounts+mapnerd@mapzen.com"
  git config --global user.name "Mapzen Developer"
  ./gradlew release -Prelease.useAutomaticVersion=true \
    -Prelease.releaseVersion=$RELEASE_VERSION_NUMBER \
    -Prelease.newVersion=$NEW_VERSION_NUMBER
fi