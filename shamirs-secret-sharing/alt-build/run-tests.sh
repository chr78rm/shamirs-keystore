#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo PROJECT_LIBS_DIR=${PROJECT_LIBS_DIR}
echo CLASSPATH_SEPARATOR=${CLASSPATH_SEPARATOR}
echo SHAMIR_VERSION=${SHAMIR_VERSION}
echo

#
# Compiler and project classpaths
#
echo Determining classpath of the project ...
PROJECT_CLASSPATH=$(find ./${PROJECT_LIBS_DIR} -name *.jar -type f -printf %p${CLASSPATH_SEPARATOR})
PROJECT_CLASSPATH=${PROJECT_CLASSPATH}./target/shamirs-secret-sharing-${SHAMIR_VERSION}.jar
echo PROJECT_CLASSPATH=${PROJECT_CLASSPATH}
echo

#
# Run tests
#
java ${SUN_MISC_UNSAFE_OPT} -Dpath.separator=${CLASSPATH_SEPARATOR} --class-path ${PROJECT_CLASSPATH} org.scalatest.tools.Runner -R target/test-classes \
-o -s de.christofreichardt.scala.shamir.ShamirSuites
