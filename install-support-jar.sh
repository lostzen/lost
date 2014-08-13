#!/bin/sh
#
# This script installs the Android Support v4 jar in your local Maven repository.
#
# Usage:
#   install-support-jar.sh
version=19.0.1

echo "Downloading support_r19.0.1.zip"
wget https://dl-ssl.google.com/android/repository/support_r$version.zip
unzip support_r$version.zip
mv support support_r$version

echo "Installing com.android.support:support-v4:$version"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=support-v4 \
  -Dversion=$version -Dpackaging=jar -Dfile=support_r$version/v4/android-support-v4.jar

echo "Deleting file support_r$version.zip"
rm support_r$version.zip

echo "Deleting folder support_r$version"
rm -rf support_r$version

echo "Done!"
