#!/bin/sh

openssl aes-256-cbc -d -k "$FILE_PASSWORD" -in ci/keystore.enc -out ci/keystore
openssl aes-256-cbc -d -k "$TRAVIS_IDENTITY" -in ci/travis.enc -out ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa
