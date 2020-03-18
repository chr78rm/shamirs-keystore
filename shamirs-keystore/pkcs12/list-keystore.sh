#!/bin/bash

KEYSTORE_FILE=my-keystore-1.p12
STORE_PASSWORD=Super-sicheres-Passwort

keytool -list -keystore $KEYSTORE_FILE -storepass $STORE_PASSWORD -storetype pkcs12 -v
