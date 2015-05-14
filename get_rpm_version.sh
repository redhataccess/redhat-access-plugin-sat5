#!/bin/bash
VERSION_LINE=`cat redhat-access-plugin-sat5.spec | grep "%define version"` 
COUNT=0
for word in $VERSION_LINE
do
  if [ $COUNT = 2 ]; then
    VERSION=$word
  fi
  let COUNT=COUNT+1
done 
echo $VERSION
