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
echo SCALADOC_LIBS_DIR=${SCALADOC_LIBS_DIR}
echo

#
# Override
#
cd ${SCALADOC_LIBS_DIR}
mvn org.apache.maven.plugins:maven-dependency-plugin:3.11.0:add --file=scaladoc_3-${SCALA_VERSION}.pom -Dgav=com.fasterxml.jackson.core:jackson-annotations:2.22 -Dmanaged
mvn org.apache.maven.plugins:maven-dependency-plugin:3.11.0:add --file=scaladoc_3-${SCALA_VERSION}.pom -Dgav=com.fasterxml.jackson.core:jackson-core:2.22.2 -Dmanaged
mvn org.apache.maven.plugins:maven-dependency-plugin:3.11.0:add --file=scaladoc_3-${SCALA_VERSION}.pom -Dgav=com.fasterxml.jackson.core:jackson-databind:2.22.2 -Dmanaged
mvn org.apache.maven.plugins:maven-dependency-plugin:3.11.0:add --file=scaladoc_3-${SCALA_VERSION}.pom -Dgav=com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.2 -Dmanaged
