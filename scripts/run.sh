#!/bin/bash
FILES=../raw_data/AndroidTotal/*
for f in $FILES
do
  if [[ $f != *"android--1"* ]]
  then
	for subf in $f/*
	do
	   if [[ $subf == *".apk"* ]]
   	   then
	      java -Xmx8g -jar RAT-TRAP.jar ../raw_data/AndroidTotal $f /classlist.txt ${subf/$f/""} ../raw_data/RAT-TRAP.txt
	   fi
        done
  fi
done
