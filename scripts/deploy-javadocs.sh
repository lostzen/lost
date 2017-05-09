#!/usr/bin/env bash
#
# generates javadocs and pushes them to gh-pages branch of LOST
#

git config --global user.email "accounts+mapnerd@mapzen.com"
git config --global user.name "Mapzen Developer"
version=$(./gradlew -q printVersion)
mkdir gh-pages
git clone -b gh-pages git@github.com:mapzen/LOST.git gh-pages
./gradlew javadocs
cd gh-pages
git commit -am "javadocs for $version"
git push origin gh-pages
cd ..
rm -rf gh-pages