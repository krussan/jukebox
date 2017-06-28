#!/bin/sh
for f in $(find -name 'lint*.xml'); do 
  echo --------------------------
  echo --- $f
  echo --------------------------
  cat $f
done

