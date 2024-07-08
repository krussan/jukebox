#!/bin/sh

openssl aes-256-cbc -d -k "$FILE_PASSWORD" -in ci/keystore2.enc -out ci/keystore2
openssl aes-256-cbc -d -k "$FILE_PASSWORD" -in ci/api2.enc -out ci/api.json
