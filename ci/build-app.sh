#!/bin/bash
echo Initiating build of jukebox app ...

echo
echo -----------------------------------------------------
echo VERSION :: $JUKEBOX_VERSION
echo TRAVIS_PULL_REQUEST :: $TRAVIS_PULL_REQUEST
echo TRAVIS_BRANCH :: $TRAVIS_BRANCH
echo -----------------------------------------------------
echo

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "master" ]];then
   echo Packaging new release ...
   ./gradlew clean build connectedCheck packageRelease -Pversion=${JUKEBOX_VERSION}
else 
   echo Running test ...
   ./gradlew build connectedCheck
fi

