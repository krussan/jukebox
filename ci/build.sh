#!/bin/bash
echo Initiating build ...

echo
echo -----------------------------------------------------
echo VERSION :: $JUKEBOX_VERSION
echo BASE_REF :: $GITHUB_BASE_REF
echo BRANCH :: $GITHUB_REF_NAME
echo EVENT :: $GITHUB_EVENT_NAME
echo -----------------------------------------------------
echo

if [[ "$GITHUB_BASE_REF" == "master" ]];then
   echo Checking that the resulting tag does not exist

   if git rev-parse -q --verify "refs/tags/$JUKEBOX_TAG" >/dev/null; then
      echo ERROR! Tag $JUKEBOX_VERSION exist. Please modify pom and commit.
      exit 1
   fi
fi

if [[ "$GITHUB_EVENT_NAME" == "push" ]] && [[ "$GITHUB_REF_NAME" == "master" ]];then
   echo Packaging new release ...
   ./gradlew build check connectedCheck assemble packageRelease archiveZip publishRelease
else
   echo Running test ...
   ./gradlew build check connectedCheck
fi

