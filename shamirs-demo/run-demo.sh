#!/bin/bash

MAVEN_REPO=$HOME/.m2/repository/
echo Using ${MAVEN_REPO} ...
java -cp target/shamirs-demo-0.0.1-SNAPSHOT.jar:\
${MAVEN_REPO}/de/christofreichardt/tracelogger/1.8.0/tracelogger-1.8.0.jar:\
${MAVEN_REPO}/de/christofreichardt/shamirs-keystore/0.0.1-SNAPSHOT/shamirs-keystore-0.0.1-SNAPSHOT.jar:\
${MAVEN_REPO}/de/christofreichardt/shamirs-secret-sharing/0.0.1-SNAPSHOT/shamirs-secret-sharing-0.0.1-SNAPSHOT.jar:\
${MAVEN_REPO}/org/scala-lang/scala-library/2.13.1/scala-library-2.13.1.jar:\
${MAVEN_REPO}/javax/json/javax.json-api/1.1.4/javax.json-api-1.1.4.jar:\
${MAVEN_REPO}/org/glassfish/javax.json/1.1.4/javax.json-1.1.4.jar de.christofreichardt.jca.shamirsdemo.App