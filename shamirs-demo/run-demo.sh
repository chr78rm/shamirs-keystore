#!/bin/bash

set -o errexit # terminate on error
ARGS=$* # all parameter

MAVEN_REPO=/c/Users/Developer/.m2/repository
VERSION=1.4.0
echo Using "${MAVEN_REPO}" and "${VERSION}" ...

# defaults
ECHO_SWITCH=OFF
BULK_SWITCH=ON
CONSOLE_SWITCH=base

# evaluate parameter
for ARG in ${ARGS}
do
  if [[ "${ARG}" == "--echo" ]]
  then
    ECHO_SWITCH=ON
  fi
  if [[ "${ARG}" == "--no-bulk" ]]
  then
    BULK_SWITCH=OFF
  fi
  if [[ "${ARG}" == "--jline" ]]
  then
    CONSOLE_SWITCH=jline
  fi
done
echo -e "\necho = ${ECHO_SWITCH}"
echo -e "bulk = ${BULK_SWITCH}"
echo -e "console = ${CONSOLE_SWITCH}\n"
if [[ "${CONSOLE_SWITCH}" == "base" ]]
then
  CONSOLE_PROPERTY=-Djdk.console=java.base
fi

# checkout JAVA_HOME
if [ "x${JAVA_HOME}" == "x" ]
  then
    BIN_JAVA=$(which java)
  else
    BIN_JAVA=${JAVA_HOME}/bin/java
fi
${BIN_JAVA} -version

${BIN_JAVA} -Djava.security.egd=file:/dev/urandom \
-Dde.christofreichardt.jca.shamirsdemo.console.echo=${ECHO_SWITCH} \
${CONSOLE_PROPERTY} \
-Dde.christofreichardt.jca.shamirsdemo.console.bulk=${BULK_SWITCH} \
-cp target/shamirs-demo-${VERSION}.jar:\
"${MAVEN_REPO}"/de/christofreichardt/tracelogger/1.10.0/tracelogger-1.10.0.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-keystore/${VERSION}/shamirs-keystore-${VERSION}.jar:\
"${MAVEN_REPO}"/de/christofreichardt/shamirs-secret-sharing/${VERSION}/shamirs-secret-sharing-${VERSION}.jar:\
"${MAVEN_REPO}"/org/scala-lang/scala-library/2.13.12/scala-library-2.13.12.jar:\
"${MAVEN_REPO}"/org/scala-lang/scala3-library_3/3.3.3/scala3-library_3-3.3.3.jar:\
"${MAVEN_REPO}"/jakarta/json/jakarta.json-api/2.1.3/jakarta.json-api-2.1.3.jar:\
"${MAVEN_REPO}"/org/eclipse/parsson/jakarta.json/1.1.5/jakarta.json-1.1.5.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcpkix-jdk18on/1.78.1/bcpkix-jdk18on-1.78.1.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcprov-jdk18on/1.78.1/bcprov-jdk18on-1.78.1.jar:\
"${MAVEN_REPO}"/org/bouncycastle/bcutil-jdk18on/1.78.1/bcutil-jdk18on-1.78.1.jar\
 de.christofreichardt.jca.shamirsdemo.App
