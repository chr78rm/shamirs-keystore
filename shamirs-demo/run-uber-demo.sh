#!/bin/bash

set -o errexit # terminate on error
ARGS=$* # all parameter

# defaults
ECHO_SWITCH=OFF
BULK_SWITCH=ON
CONSOLE_SWITCH=jline
SUN_MISC_UNSAFE=ON

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
  if [[ "${ARG}" == "--base" ]]
  then
    CONSOLE_SWITCH=base
  fi
  if [[ "${ARG}" == "--sun-misc-unsafe" ]]
  then
    SUN_MISC_UNSAFE=OFF
  fi
done
echo -e "\necho = ${ECHO_SWITCH}"
echo -e "bulk = ${BULK_SWITCH}"
echo -e "console = ${CONSOLE_SWITCH}"
echo -e "sun_misc_unsafe = ${SUN_MISC_UNSAFE}\n"
if [[ "${CONSOLE_SWITCH}" == "base" ]]
then
  CONSOLE_PROPERTY=-Djdk.console=java.base
fi
if [[ "${SUN_MISC_UNSAFE}" == "OFF" ]]
then
  SUN_MISC_UNSAFE_OPTION=--sun-misc-unsafe-memory-access=allow
fi

# checkout JAVA_HOME
if [ "x${JAVA_HOME}" == "x" ]
  then
    BIN_JAVA=$(which java)
  else
    BIN_JAVA=${JAVA_HOME}/bin/java
fi
${BIN_JAVA} -version

SHAMIRS_VERSION=1.3.3
${BIN_JAVA} -Djava.security.egd=file:/dev/urandom \
  -Dde.christofreichardt.jca.shamirsdemo.console.echo=${ECHO_SWITCH} \
  ${CONSOLE_PROPERTY} \
  -Dde.christofreichardt.jca.shamirsdemo.console.bulk=${BULK_SWITCH} \
  ${SUN_MISC_UNSAFE_OPTION} \
  -jar target/shamirs-demo-${SHAMIRS_VERSION}.jar
