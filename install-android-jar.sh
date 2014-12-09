#!/bin/sh
#
# This script installs the Android SDK jar in your local Maven repository.
#
# Usage:
#   install-android-jar.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.
#  3. You have installed the Android Support (compatibility) libraries from the SDK installer.
#
# Adapted from https://github.com/robolectric/robolectric/blob/master/scripts/install-support-jar.sh

jarLocation="$ANDROID_HOME/platforms/android-19/android.jar"
if [ ! -f "$jarLocation" ]; then
  echo "android-19 artifact not found!";
  exit 1;
fi

echo "Installing android:android from $jarLocation"
mvn -q install:install-file -DgroupId=android -DartifactId=android \
  -Dversion=4.4.2_r3 -Dpackaging=jar -Dfile="$jarLocation"

echo "Done!"
