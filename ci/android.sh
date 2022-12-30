#!/bin/bash
echo Setting up android build environment

echo
echo -----------------------------------------------------
echo ANDROID SETTINGS ::
echo SDK DIR :: $ANDROID_HOME
echo TARGET VERSION :: $TARGET_VERSION
echo BUILD_TOOLS :: $BUILD_TOOLS_VERSION
echo -----------------------------------------------------
echo

touch $HOME/.android/repositories.cfg
wget "https://dl.google.com/android/repository/commandlinetools-linux-9123335_latest.zip" -O commandlinetools.zip
unzip commandlinetools.zip -d $ANDROID_HOME/

yes | $ANDROID_HOME/cmdline-tools/bin/sdkmanager "platforms;android-${TARGET_VERSION}" --sdk_root=$ANDROID_HOME
yes | $ANDROID_HOME/cmdline-tools/bin/sdkmanager "build-tools;${BUILD_TOOLS_VERSION}" --sdk_root=$ANDROID_HOME

