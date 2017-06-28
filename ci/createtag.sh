#!/bin/sh
VERSION=$1
echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

git config --global user.email "builds@travis-ci.com"
git config --global user.name "Travis CI"


ARTIFACTID=`sed -e "s/xmlns/ignore/" $POMFILE  | xmllint --xpath '/project/artifactId/text()' -`

export GIT_TAG=$TRAVIS_BRANCH-0.1.$TRAVIS_BUILD_NUMBER
git tag $GIT_TAG -a -m "Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"
git push -q https://$TAGPERM@github.com/RlonRyan/JBasicX --tags
ls -R