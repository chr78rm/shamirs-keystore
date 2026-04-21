#!/bin/bash

#
# Terminate on error
#
set -o errexit

#
# Constants
#
source ./alt-build/common.sh
echo JAVA_HOME=${JAVA_HOME}
echo JAVA=${JAVA}
echo SCALADOC_LIBS_DIR=${SCALADOC_LIBS_DIR}
echo PROJECT_LIBS_DIR=${PROJECT_LIBS_DIR}
echo CLASSPATH_SEPARATOR=${CLASSPATH_SEPARATOR}
echo SHAMIR_VERSION=${SHAMIR_VERSION}
echo

#
# Scaladoc classpath and project classpath
#
echo Determining classpath of scaladoc ...
SCALADOC_CLASSPATH=$(find ${SCALADOC_LIBS_DIR} -name *.jar -type f -printf %p${CLASSPATH_SEPARATOR})
LENGTH=$(($(echo -n ${SCALADOC_CLASSPATH} | wc --chars)-1))
SCALADOC_CLASSPATH=${SCALADOC_CLASSPATH:0:${LENGTH}}
echo SCALADOC_CLASSPATH=${SCALADOC_CLASSPATH}
echo

echo Determining classpath of the project ...
PROJECT_CLASSPATH=$(find ${PROJECT_LIBS_DIR} -name *.jar -type f -printf %p${CLASSPATH_SEPARATOR})
LENGTH=$(($(echo -n ${PROJECT_CLASSPATH} | wc --chars)-1))
PROJECT_CLASSPATH=${PROJECT_CLASSPATH:0:${LENGTH}}
echo PROJECT_CLASSPATH=${PROJECT_CLASSPATH}
echo

#
# Tasty files
#
echo Determining the to be processed tasty files ...
SOURCES=$(find ./target/classes -name *.tasty -type f -printf "%p ")
echo SOURCES=${SOURCES}
echo

#
# Generating the scaladocs
#
echo Generating the scaladocs ...

mkdir --parents --verbose ./target/site/scaladocs

${JAVA} -Dpath.separator=${CLASSPATH_SEPARATOR} --class-path ${SCALADOC_CLASSPATH}${CLASSPATH_SEPARATOR}${PROJECT_CLASSPATH} -Dscala.expandjavacp=true -Dscala.usejavacp=true \
dotty.tools.scaladoc.Main -d ./target/site/scaladocs \
-no-link-warnings \
-doc-footer \
"Copyright © 2017, 2026, Christof Reichardt - Paul-Ehrlich-Weg 1 - D-63110 Rodgau" \
-doc-title \
"shamirs-secret-sharing 1.4.0 API" \
${SOURCES}

${JAR} --create --file=target/shamirs-secret-sharing-${SHAMIR_VERSION}-javadoc.jar -C ./target/site/scaladocs .
