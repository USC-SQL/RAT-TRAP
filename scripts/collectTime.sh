#!/bin/bash
FILES=../raw_data/AndroidApp/*
for f in $FILES
do
  if [[ $f != *"android--1"* ]]
  then
	for subf in $f/*
	do
	   if [[ $subf == *".apk"* ]]
   	   then
	      for i in {1..15}
	      do
	     	 rm -r $f/opt 2> /dev/null
	      	java -jar Time.jar ../raw_data/AndroidApp $f /classlist.txt ${subf/$f/""} ../raw_data/AnalysisTime.txt
	      done
	   fi
        done
  fi
done
