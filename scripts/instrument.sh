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
	      rm -r $f/opt 2> /dev/null
	      rm -r $f/unopt 2> /dev/null
	      java -jar InstrumentOpt.jar ../raw_data/AndroidApp $f /classlist.txt ${subf/$f/""} ./output.txt
	      java -jar InstrumentTimeStamp.jar ../raw_data/AndroidApp $f /classlist.txt ${subf/$f/""} ./output.txt
	      ./sign.sh $f/opt/*apk
	      ./sign.sh $f/unopt/*apk
	   fi
        done
  fi
done
