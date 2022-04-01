#!/bin/bash

if [ "x${JAVA_HOME}" == "x" ]
  then
    BIN_JAVA=$(which java)
  else
    BIN_JAVA=${JAVA_HOME}/bin/java
fi
${BIN_JAVA} -version

java -Djava.security.egd=file:/dev/urandom -jar target/shamirs-demo-1.2.0.jar

