#!/bin/sh

openssl aes-256-cbc -d -k "$FILE_PASSWORD" -in ci/keystore.enc -out ci/keystore
openssl aes-256-cbc -d -k "$TRAVIS_IDENTITY" -in ci/travis.enc -out ~/.ssh/id_rsa
openssl aes-256-cbc -d -k "$TRAVIS_IDENTITY" -in ci/api.enc -out ci/api.json

chmod 600 ~/.ssh/id_rsa
