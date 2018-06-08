#! /bin/bash
jarsigner -sigalg SHA1withRSA -digestalg SHA1 -verbose -keystore wendy.keystore -storepass USCDING -signedjar $1 $1 wendy.keystore;
