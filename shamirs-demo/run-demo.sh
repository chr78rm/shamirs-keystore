#!/bin/bash

MAVEN_REPO=/d/m2-repo
VERSION=1.3.1
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
"${MAVEN_REPO}"/de/christofreichardt/tracelogger/1.10.0/tracelogger-1.10.0.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-keystore/${VERSION}/shamirs-keystore-${VERSION}.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-secret-sharing/${VERSION}/shamirs-secret-sharing-${VERSION}.jar:\
"${MAVEN_REPO}"/org/scala-lang/scala-library/2.13.12/scala-library-2.13.12.jar:\
"${MAVEN_REPO}"/org/scala-lang/scala3-library_3/3.3.3/scala3-library_3-3.3.3.jar:\
"${MAVEN_REPO}"/jakarta/json/jakarta.json-api/2.1.3/jakarta.json-api-2.1.3.jar:\
"${MAVEN_REPO}"/org/eclipse/parsson/jakarta.json/1.1.5/jakarta.json-1.1.5.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcpkix-jdk18on/1.77/bcpkix-jdk18on-1.77.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcprov-jdk18on/1.77/bcprov-jdk18on-1.77.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcutil-jdk18on/1.77/bcutil-jdk18on-1.77.jar:\
 de.christofreichardt.jca.shamirsdemo.App
