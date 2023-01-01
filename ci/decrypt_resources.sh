#!/bin/sh
DIRNAME=$( dirname -- "$0"; )
find se.qxx.jukebox/src/test/resources -name '*.enc' -exec $DIRNAME/decrypt.sh {} \;
