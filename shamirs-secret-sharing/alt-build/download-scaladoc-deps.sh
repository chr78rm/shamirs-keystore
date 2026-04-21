#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo SCALADOC_3_JAR=${SCALADOC_3_JAR}
echo SCALADOC_3_POM=${SCALADOC_3_POM}
echo SCALADOC_LIBS_DIR=${SCALADOC_LIBS_DIR}
echo SCALADOC_3_JAR=${SCALADOC_3_JAR}
echo SCALADOC_3_POM=${SCALADOC_3_POM}
echo SCALADOC_3_JAR_CHECKSUM=${SCALADOC_3_JAR_CHECKSUM}
echo SCALADOC_3_POM_CHECKSUM=${SCALADOC_3_POM_CHECKSUM}
echo

#
# Download
#
mkdir --parents --verbose ${SCALADOC_LIBS_DIR}
if [[ ! -f "${SCALADOC_LIBS_DIR}/scaladoc_3-${SCALA_VERSION}.jar" ]]
then
  echo scaladoc_3-${SCALA_VERSION}.jar is missing, downloading it ...
  curl --remote-name --output-dir ${SCALADOC_LIBS_DIR} ${SCALADOC_3_JAR} && sha1sum ${SCALADOC_LIBS_DIR}/scaladoc_3-${SCALA_VERSION}.jar | grep ${SCALADOC_3_JAR_CHECKSUM}
else
  echo scaladoc_3-${SCALA_VERSION}.jar already exists in destination.
fi
echo
if [[ ! -f "${SCALADOC_LIBS_DIR}/scaladoc_3-${SCALA_VERSION}.pom" ]]
then
  echo scaladoc_3-${SCALA_VERSION}.pom is missing, downloading it ...
  curl --remote-name --output-dir ${SCALADOC_LIBS_DIR} ${SCALADOC_3_POM} && sha1sum ${SCALADOC_LIBS_DIR}/scaladoc_3-${SCALA_VERSION}.pom | grep ${SCALADOC_3_POM_CHECKSUM}
else
  echo scaladoc_3-${SCALA_VERSION}.pom already exists in destination.
fi
echo

#
# Copy dependencies
#
cd ${SCALADOC_LIBS_DIR}
mvn --file=scaladoc_3-${SCALA_VERSION}.pom -DoutputDirectory=. dependency:copy-dependencies
