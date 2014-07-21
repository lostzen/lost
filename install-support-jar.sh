#!/bin/sh
#
# This script installs the Android Support v4 jar in your local Maven repository.
#
# Usage:
#   install-support-jar.sh

echo "Downloading support_r19.0.1.zip"
wget https://dl-ssl.google.com/android/repository/support_r19.0.1.zip
unzip support_r19.0.1.zip
mv support support_r19.0.1

echo "Installing com.android.support:support-v4:19.0.1"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=support-v4 \
  -Dversion=19.0.1 -Dpackaging=jar -Dfile=support_r19.0.1/v4/android-support-v4.jar

echo "Deleting file support_r19.0.1.zip"
rm support_r19.0.1.zip

echo "Deleting folder support_r19.0.1"
rm -rf support_r19.0.1

echo "Done!"
