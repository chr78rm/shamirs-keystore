#!/bin/bash

keytool -genseckey -alias my-aes-key -keypass $PASSWORD -keyalg AES -keysize 256 -keystore $KEYSTORE_FILE -storepass $PASSWORD -storetype pkcs12 -v

