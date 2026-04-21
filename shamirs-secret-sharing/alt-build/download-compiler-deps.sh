#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo SCALA_VERSION=${SCALA_VERSION}
echo SCALA_MAVEN_REPO=${SCALA_MAVEN_REPO}
echo SCALA3_COMPILER_JAR=${SCALA3_COMPILER_JAR}
echo SCALA3_COMPILER_POM=${SCALA3_COMPILER_POM}
echo SCALA3_COMPILER_JAR_CHECKSUM=${SCALA3_COMPILER_JAR_CHECKSUM}
echo SCALA3_COMPILER_POM_CHECKSUM=${SCALA3_COMPILER_POM_CHECKSUM}
echo COMPILER_LIBS_DIR=${COMPILER_LIBS_DIR}
echo

#
# Download
#
mkdir --parents --verbose ${COMPILER_LIBS_DIR}
if [[ ! -f "${SCRIPT_DIR}/compiler-libs/scala3-compiler_3-${SCALA_VERSION}.jar" ]]
then
  echo scala3-compiler_3-${SCALA_VERSION}.jar is missing, downloading it ...
  curl --remote-name --output-dir ${COMPILER_LIBS_DIR} ${SCALA3_COMPILER_JAR} && sha1sum ${COMPILER_LIBS_DIR}/scala3-compiler_3-${SCALA_VERSION}.jar | grep ${SCALA3_COMPILER_JAR_CHECKSUM}
else
  echo scala3-compiler_3-${SCALA_VERSION}.jar already exists in destination.
fi
echo
if [[ ! -f "${SCRIPT_DIR}/compiler-libs/scala3-compiler_3-${SCALA_VERSION}.pom" ]]
then
  echo scala3-compiler_3-${SCALA_VERSION}.pom is missing, downloading it ...
  curl --remote-name --output-dir ${COMPILER_LIBS_DIR} ${SCALA3_COMPILER_POM} && sha1sum ${COMPILER_LIBS_DIR}/scala3-compiler_3-${SCALA_VERSION}.pom | grep ${SCALA3_COMPILER_POM_CHECKSUM}
else
  echo scala3-compiler_3-${SCALA_VERSION}.pom already exists in destination.
fi
echo

#
# Copy dependencies
#
cd ${COMPILER_LIBS_DIR}
mvn --file=scala3-compiler_3-${SCALA_VERSION}.pom -DoutputDirectory=. dependency:copy-dependencies
