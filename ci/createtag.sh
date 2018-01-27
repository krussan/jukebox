#!/bin/bash
echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

git config --global user.email "builds@travis-ci.com"
git config --global user.name "Travis CI"

echo "Version :: $JUKEBOX_VERSION"
echo "TRAVIS_BRANCH :: $TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST :: $TRAVIS_PULL_REQUEST"

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "master" ]];then
   git tag $JUKEBOX_TAG -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"
   git push -q ssh://git@github.com:/krussan/jukebox.git --tags
fi

