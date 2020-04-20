#!/bin/bash

export KEYSTORE_FILE=my-keystore-1.p12
export PASSWORD=Super-sicheres-Passwort

./add-key-pair.sh
./add-certificates.sh
./add-secret-key.sh
