#!/bin/bash
echo Initiating build ...
echo Checking version ...
JUKEBOX_VERSION=`cd ${TRAVIS_BUILD_DIR}/se.qxx.jukebox && mvn help:evaluate -Dexpression=project.version | grep -e '^[^\[]'`

echo
echo -----------------------------------------------------
echo VERSION :: $JUKEBOX_VERSION
echo TRAVIS_PULL_REQUEST :: $TRAVIS_PULL_REQUEST
echo TRAVIS_BRANCH :: $TRAVIS_BRANCH
echo -----------------------------------------------------
echo

if [[ "$TRAVIS_BRANCH" == "master" ]];then
   echo Checking that the resulting tag does not exist

   if git rev-parse -q --verify "refs/tags/v$JUKEBOX_VERSION" >/dev/null; then
      echo ERROR! Tag $JUKEBOX_VERSION exist. Please modify pom and commit.
      exit 1
   fi
fi

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "master" ]];then
   echo Packaging new release ...
   mvn clean package -B
else 
   echo Running test ...
   mvn test -B
fi

