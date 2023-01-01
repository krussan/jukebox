#!/bin/sh
FILE_PASSWORD=$1

find se.qxx.jukebox/src/test/resources -name '*.html' -exec openssl aes-256-cbc -e -k "$FILE_PASSWORD" -in {} -out {}.enc \;
