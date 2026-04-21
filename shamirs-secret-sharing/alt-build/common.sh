#!/bin/bash

echo "==> Calling script is $0 ..."
echo

#
# Project directory
#
readonly SCRIPT_DIR=$(dirname $(realpath $0))
readonly PROJECT_DIR=$(dirname ${SCRIPT_DIR})

#
# Constants
#
readonly SCALA_VERSION=3.8.3
readonly SHAMIR_VERSION=1.4.0

readonly SCALA_MAVEN_REPO=https://repo1.maven.org/maven2/org/scala-lang

readonly SCALA3_COMPILER_JAR=${SCALA_MAVEN_REPO}/scala3-compiler_3/${SCALA_VERSION}/scala3-compiler_3-${SCALA_VERSION}.jar
readonly SCALA3_COMPILER_POM=${SCALA_MAVEN_REPO}/scala3-compiler_3/${SCALA_VERSION}/scala3-compiler_3-${SCALA_VERSION}.pom
readonly SCALADOC_3_JAR=${SCALA_MAVEN_REPO}/scaladoc_3/${SCALA_VERSION}/scaladoc_3-${SCALA_VERSION}.jar
readonly SCALADOC_3_POM=${SCALA_MAVEN_REPO}/scaladoc_3/${SCALA_VERSION}/scaladoc_3-${SCALA_VERSION}.pom

readonly SCALA3_COMPILER_JAR_CHECKSUM=fa1729a7600649732c4ea66e43b66d50f107a090
readonly SCALA3_COMPILER_POM_CHECKSUM=29bf23aaea17acfe906bf7af509318d2d85a755c
readonly SCALADOC_3_JAR_CHECKSUM=b3bb34ed04efc94061bb1a6df70481dd18445a10
readonly SCALADOC_3_POM_CHECKSUM=a19008185ca13da71b9f61952f1177f25bfefa73

readonly COMPILER_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/compiler-libs)
readonly PROJECT_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/project-libs)
readonly SCALADOC_LIBS_DIR=$(realpath --relative-to=${PROJECT_DIR} ${SCRIPT_DIR}/scaladoc-libs)

readonly JAVA_HOME=${HOME}/Java/openjdk-26/bin
readonly JAVA=${JAVA_HOME}/java
readonly JAR=${JAVA_HOME}/jar
readonly JAVA_OUTPUT_VERSION=17

readonly CLASSPATH_SEPARATOR=:
readonly SUN_MISC_UNSAFE_OPT=--sun-misc-unsafe-memory-access=warn
