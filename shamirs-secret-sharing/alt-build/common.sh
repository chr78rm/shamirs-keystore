#!/bin/bash

echo
echo "==> Calling script is $0 ..."
echo

#
# Project directory
#
readonly SCRIPT_DIR=$(dirname $(realpath $0))
readonly PROJECT_DIR=$(dirname ${SCRIPT_DIR})

#
# Defaults
#
if [[ -z "${SCALA_VERSION}" ]]
then
  readonly SCALA_VERSION=3.9.0
fi
if [[ -z "${SHAMIR_VERSION}" ]]
then
  readonly SHAMIR_VERSION=1.4.1
fi


#
# Constants
#
readonly SCALA_MAVEN_REPO=https://repo1.maven.org/maven2/org/scala-lang

readonly SCALA3_COMPILER_JAR=${SCALA_MAVEN_REPO}/scala3-compiler_3/${SCALA_VERSION}/scala3-compiler_3-${SCALA_VERSION}.jar
readonly SCALA3_COMPILER_POM=${SCALA_MAVEN_REPO}/scala3-compiler_3/${SCALA_VERSION}/scala3-compiler_3-${SCALA_VERSION}.pom
readonly SCALADOC_3_JAR=${SCALA_MAVEN_REPO}/scaladoc_3/${SCALA_VERSION}/scaladoc_3-${SCALA_VERSION}.jar
readonly SCALADOC_3_POM=${SCALA_MAVEN_REPO}/scaladoc_3/${SCALA_VERSION}/scaladoc_3-${SCALA_VERSION}.pom

readonly SCALA3_COMPILER_JAR_CHECKSUM=f0c9c4f2ad340ce7408064ad6b0eb7fa9a9b8733
readonly SCALA3_COMPILER_POM_CHECKSUM=365db6bc99de00f01e75175e48bda7a583639aad
readonly SCALADOC_3_JAR_CHECKSUM=6c466bd870da77e8ae8bf5946a01b5aecdf642c2
readonly SCALADOC_3_POM_CHECKSUM=ad7f6b2832e24fe75bf46ba6cee15c9575bad8c3

readonly COMPILER_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/compiler-libs)
readonly PROJECT_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/project-libs)
readonly SCALADOC_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/scaladoc-libs)

readonly JAVA_HOME=${HOME}/Java/openjdk-27/bin
readonly JAVA=${JAVA_HOME}/java
readonly JAR=${JAVA_HOME}/jar
readonly JAVA_OUTPUT_VERSION=17

readonly CLASSPATH_SEPARATOR=:
readonly SUN_MISC_UNSAFE_OPT=--sun-misc-unsafe-memory-access=warn
