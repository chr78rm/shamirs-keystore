#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo PROJECT_DIR=${PROJECT_DIR}
echo JAVA_HOME=${JAVA_HOME}
echo JAVA=${JAVA}
echo JAR=${JAR}
echo COMPILER_LIBS_DIR=${COMPILER_LIBS_DIR}
echo PROJECT_LIBS_DIR=${PROJECT_LIBS_DIR}
echo CLASSPATH_SEPARATOR=${CLASSPATH_SEPARATOR}
echo JAVA_OUTPUT_VERSION=${JAVA_OUTPUT_VERSION}
echo SHAMIR_VERSION=${SHAMIR_VERSION}
echo

#
# Copy dependencies
#
mvn dependency:copy-dependencies -DoutputDirectory=${PROJECT_LIBS_DIR} -DincludeScope=runtime

#
# Compiler and project classpaths
#
echo
echo Determining classpath of the scala compiler ...
COMPILER_CLASSPATH=$(find ./${COMPILER_LIBS_DIR} -name *.jar -type f -printf %p${CLASSPATH_SEPARATOR})
LENGTH=$(($(echo -n ${COMPILER_CLASSPATH} | wc --chars)-1))
COMPILER_CLASSPATH=${COMPILER_CLASSPATH:0:${LENGTH}}
echo COMPILER_CLASSPATH=${COMPILER_CLASSPATH}
echo

echo Determining classpath of the project ...
PROJECT_CLASSPATH=$(find ./${PROJECT_LIBS_DIR} -name *.jar -type f -printf %p${CLASSPATH_SEPARATOR})
LENGTH=$(($(echo -n ${PROJECT_CLASSPATH} | wc --chars)-1))
PROJECT_CLASSPATH=${PROJECT_CLASSPATH:0:${LENGTH}}
echo PROJECT_CLASSPATH=${PROJECT_CLASSPATH}
echo

#
# Source files
#
echo Determining the to be compiled source files ...
SOURCES=$(find ./src/main -name *.scala -type f -printf "%p ")
echo SOURCES=${SOURCES}
echo

#
# Compile main classes
#
echo Compiling main classes ...

mkdir --parents --verbose ./target/classes

${JAVA} -Dpath.separator=${CLASSPATH_SEPARATOR} --class-path ${COMPILER_CLASSPATH} -Dscala.expandjavacp=true -Dscala.usejavacp=true \
dotty.tools.MainGenericCompiler -d ./target/classes \
-classpath \
${PROJECT_CLASSPATH} \
-deprecation \
-verbose \
-encoding \
utf-8 \
-java-output-version \
${JAVA_OUTPUT_VERSION} \
${SOURCES}

#
# Creating jar-archive
#
echo Creating jar-archive ...
${JAR} --create --file=target/shamirs-secret-sharing-${SHAMIR_VERSION}.jar -C ./target/classes/ .
