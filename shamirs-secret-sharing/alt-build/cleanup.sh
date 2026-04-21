#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo COMPILER_LIBS_DIR=${COMPILER_LIBS_DIR}
echo PROJECT_LIBS_DIR=${PROJECT_LIBS_DIR}
echo SCALADOC_LIBS_DIR=${SCALADOC_LIBS_DIR}
echo

#
# Clean up
#
echo Cleaning up ...
rm --force --verbose ${COMPILER_LIBS_DIR}/*.jar ${COMPILER_LIBS_DIR}/scala3-compiler_3-${SCALA_VERSION}.pom
if [[ -d ${COMPILER_LIBS_DIR} ]]
then
  rmdir --verbose ${COMPILER_LIBS_DIR}
  echo
fi
rm --force --verbose ${PROJECT_LIBS_DIR}/*.jar
if [[ -d ${PROJECT_LIBS_DIR} ]]
then
  rmdir --verbose ${PROJECT_LIBS_DIR}
  echo
fi
rm --force --verbose ${SCALADOC_LIBS_DIR}/*.jar ${SCALADOC_LIBS_DIR}/scaladoc_3-${SCALA_VERSION}.pom
if [[ -d ${SCALADOC_LIBS_DIR} ]]
then
  rmdir --verbose ${SCALADOC_LIBS_DIR}
  echo
fi
