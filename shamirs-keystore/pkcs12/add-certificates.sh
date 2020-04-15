#!/bin/bash

KEYSTORE_FILE=my-keystore-1.p12
PASSWORD=Super-sicheres-Passwort

keytool -importcert -alias digicert -file DigiCert-SHA2-Secure-Server-CA.pem -keystore $KEYSTORE_FILE -storepass $PASSWORD \
-storetype pkcs12 -v -trustcacerts

keytool -importcert -alias oracle -file www-ww-oracle-com.pem -keystore $KEYSTORE_FILE -storepass $PASSWORD \
-storetype pkcs12 -v -trustcacerts

