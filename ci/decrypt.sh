#!/bin/sh
FILE=$1
DECRYPTED_FILE="${FILE%.*}"

echo $DECRYPTED_FILE
openssl aes-256-cbc -d -k "$FILE_PASSWORD" -in $FILE -out $DECRYPTED_FILE
