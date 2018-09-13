#!/bin/bash
echo Initiating build of jukebox app ...

echo
echo -----------------------------------------------------
echo VERSION :: $JUKEBOX_VERSION
echo TRAVIS_PULL_REQUEST :: $TRAVIS_PULL_REQUEST
echo TRAVIS_BRANCH :: $TRAVIS_BRANCH
echo -----------------------------------------------------
echo

if [[ "$TRAVIS_BRANCH" == "master" ]];then
   echo Checking that the resulting tag does not exist

   if git rev-parse -q --verify "refs/tags/$JUKEBOX_TAG" >/dev/null; then
      echo ERROR! Tag $JUKEBOX_VERSION exist. Please modify pom and commit.
      exit 1
   fi
fi


if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && [[ "$TRAVIS_BRANCH" == "master" ]];then
   echo Packaging new release ...
   ./gradlew clean build check connectedCheck assemble packageRelease archiveZip publishRelease -Pversion=${JUKEBOX_VERSION} -PprotocExec=${TRAVIS_BUILD_DIR}/ci/protoc
else 
   echo Running test ...
   ./gradlew build check connectedCheck -PprotocExec=${TRAVIS_BUILD_DIR}/ci/protoc
fi

