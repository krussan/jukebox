#!/bin/bash
SQLITE=sqlite3
DB=$1
TABLE=$2

echo "-- $TABLE:";
COLS=`"$SQLITE" "$DB" "pragma table_info($TABLE)" | cut -d'|' -f2 `
COLS_CS=`echo \\\`$COLS\\\` | sed 's/ /\\\`,\\\`/g'`

if [ "$TABLE" != "BlobData" ]; then
    echo -e ".mode insert\nselect $COLS_CS from $TABLE;\n" | \
       "$SQLITE" "$DB" | \
       sed "s/^INSERT INTO table/INSERT INTO $TABLE ($COLS_CS)/" | \
       perl -ne 's/\n//g; print;' | \
       perl -ne 's/INSERT\s*INTO/\nINSERT INTO/g;print;'
else
    echo -e ".mode insert\nselect $COLS_CS from $TABLE;\n" | \
       "$SQLITE" "$DB" | \
       sed "s/^INSERT INTO table/INSERT INTO $TABLE ($COLS_CS)/"
fi
