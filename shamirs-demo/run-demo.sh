#!/bin/bash

MAVEN_REPO=$HOME/.m2/repository/
VERSION=1.0.0-beta
echo Using "${MAVEN_REPO}" and "${VERSION}" ...

echo
if [ "x${JAVA_HOME}" == "x" ]
  then
    BIN_JAVA=$(which java)
  else
    BIN_JAVA=${JAVA_HOME}/bin/java
fi
${BIN_JAVA} -version

${BIN_JAVA} -Djava.security.egd=file:/dev/urandom -cp target/shamirs-demo-${VERSION}.jar:\
"${MAVEN_REPO}"/de/christofreichardt/tracelogger/1.8.0/tracelogger-1.8.0.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-keystore/${VERSION}/shamirs-keystore-${VERSION}.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-secret-sharing/${VERSION}/shamirs-secret-sharing-${VERSION}.jar:\
"${MAVEN_REPO}"/org/scala-lang/scala-library/2.13.2/scala-library-2.13.2.jar:\
"${MAVEN_REPO}"/org/jboss/resteasy/resteasy-json-p-provider/resteasy-json-p-provider-4.5.3.Final.jar:\
"${MAVEN_REPO}"/org/glassfish/jakarta.json/1.1.6/jakarta.json-1.1.6.jar:\
"${MAVEN_REPO}"/org/jboss/logging/jboss-logging/3.3.2.Final/jboss-logging-3.3.2.Final.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcpkix-jdk15on/1.65/bcpkix-jdk15on-1.65.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcprov-jdk15on/1.65/bcprov-jdk15on-1.65.jar:\
 de.christofreichardt.jca.shamirsdemo.App
