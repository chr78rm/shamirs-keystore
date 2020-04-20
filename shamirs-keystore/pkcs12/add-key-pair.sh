#!/bin/bash

ALIAS=my-test-keypair

keytool -genkeypair -alias $ALIAS -keyalg EC -keysize 256 -sigalg SHA256withECDSA -dname "cn=Christof, L=Rodgau, ST=Hessen, c=DE" \
-keypass $PASSWORD -validity 1825 -storetype pkcs12 -keystore $KEYSTORE_FILE -storepass $PASSWORD -v
