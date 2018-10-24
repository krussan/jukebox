#!/bin/bash
FILE=$1
MYSQL_HOST=$2
MYSQL_DB=$3
MYSQL_USER=$4
MYSQL_PASS=$5
EXPORT_BLOB_DATA=$6

if [ "$EXPORT_BLOB_DATA" == "" ];then EXPORT_BLOB_DATA=1; fi

if [ "$FILE" != "" ] && [ "$MYSQL_USER" != "" ] && [ "$MYSQL_PASS" != "" ] && [ "$MYSQL_DB" != "" ] && [ "$MYSQL_HOST" != "" ]; then
   mkdir -p dump
   rm dump/*.sql

   sqlite3 $FILE ".tables" > dump/tables.sql

   for tbl in `cat dump/tables.sql | tr -s ' ' | cut -d ' ' --output-delimiter=$'\n' -f 1- | tr -s '\n\n'`; do
      if [[ "$tbl" == "BlobData" && "$EXPORT_BLOB_DATA" == "1" ]] || [[ "$tbl" != "BlobData" ]]; then
         echo "Exporting table :: $tbl"
         sqlite3 $FILE ".dump $tbl" > dump/$tbl.sql

         echo "Replacing statements"
         sed -i -e "s/^CREATE\s*TABLE.*//gi" dump/$tbl.sql
         sed -i -e "s/^PRAGMA.*//gi" dump/$tbl.sql
         sed -i -e "s/^BEGIN\s*TRANSACTION.*//gi" dump/$tbl.sql 
         sed -i -e "s/^COMMIT.*//gi" dump/$tbl.sql
         sed -i -e "s/\"$tbl\"/$tbl/gi" dump/$tbl.sql

         echo "Importing table :: $tbl"
         mysql -h $MYSQL_HOST -u $MYSQL_USER --password=$MYSQL_PASS -D $MYSQL_DB -e "DELETE FROM $tbl"
         mysql -h $MYSQL_HOST -u $MYSQL_USER --password=$MYSQL_PASS -D $MYSQL_DB < dump/$tbl.sql
      fi
   done
else
  echo 
  echo migrateMysql \<sqlite_file\> \<host\> \<database\> \<user\> \<pass\>
  echo
fi;

