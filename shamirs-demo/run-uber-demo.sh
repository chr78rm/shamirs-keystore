#!/bin/bash

if [ "x${JAVA_HOME}" == "x" ]
  then
    BIN_JAVA=$(which java)
  else
    BIN_JAVA=${JAVA_HOME}/bin/java
fi
${BIN_JAVA} -version

SHAMIRS_VERSION=1.3.1
${BIN_JAVA} -Djava.security.egd=file:/dev/urandom -jar target/shamirs-demo-${SHAMIRS_VERSION}.jar

