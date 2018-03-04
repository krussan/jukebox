#!/bin/bash
TABLE=$1
MYSQL_HOST=$2
MYSQL_DB=$3
MYSQL_USER=$4
MYSQL_PASS=$5

echo "--------------------------------------"
echo "Importing table :: $TABLE"
echo "--------------------------------------"

mysql -h $MYSQL_HOST -u $MYSQL_USER --password=$MYSQL_PASS -D $MYSQL_DB -e "DELETE FROM $TABLE"

mkdir -p dump/data
cd dump/data
rm -f *
split -l 10 -a 4 -d ../$TABLE.sql
cd ../..

NRFILES=$(ls -la dump/data | wc -l)
C=0
for f in dump/data/*; do
  C=$(($C + 1))
  echo Importing $f [$C / $NRFILES]

  mysql -h $MYSQL_HOST -u $MYSQL_USER --password=$MYSQL_PASS -D $MYSQL_DB < $f
done
