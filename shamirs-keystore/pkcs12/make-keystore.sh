#!/bin/bash

KEYSTORE_FILE=my-keystore-1.p12
PASSWORD=Super-sicheres-Passwort
ALIAS=my-test-keypair

keytool -genkeypair -alias $ALIAS -keyalg EC -keysize 256 -sigalg SHA256withECDSA -dname "cn=Christof, L=Eschborn, ST=Hessen, c=DE, O=NGDA, OU=Software Development" \
-keypass $PASSWORD -validity 1825 -storetype pkcs12 -keystore $KEYSTORE_FILE -storepass $PASSWORD -v
