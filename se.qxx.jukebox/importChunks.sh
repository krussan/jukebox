#!/bin/bash
MYSQL_HOST=$1
MYSQL_DB=$2
MYSQL_USER=$3
MYSQL_PASS=$4

NRFILES=$(ls -la data/ | wc -l)
C=0

for f in data/*; do
   C=$(($C + 1))
   echo Importing $f [$C / $NRFILES]

   mysql -u $MYSQL_USER --password=$MYSQL_PASS -h $MYSQL_HOST -D $MYSQL_DB < $f
done
