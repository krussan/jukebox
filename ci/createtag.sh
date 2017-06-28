#!/bin/sh
echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

git config --global user.email "builds@travis-ci.com"
git config --global user.name "Travis CI"

export JUKEBOX_VERSION=`cd se.qxx.jukebox && mvn help:evaluate -Dexpression=project.version | grep -e '^[^\[]'`
export GIT_TAG=v$JUKEBOX_VERSION

git tag $GIT_TAG -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"
git push -q ssh://git@github.com:/krussan/jukebox.git --tags
