#!/bin/bash

DIST=./target/dist
VERSION=$1
NAME=$2

echo "Creating distribution shamirs-demo-${VERSION} ..."

echo "Making directories as needed ..."
mkdir --parents ${DIST}/shamirs-demo
mkdir --parents ${DIST}/shamirs-demo/log
mkdir --parents ${DIST}/shamirs-demo/target
mkdir --parents ${DIST}/shamirs-demo/workspace
mkdir --parents ${DIST}/shamirs-demo/src

echo "Copying sources ..."
cp --recursive src/main ${DIST}/shamirs-demo/src

echo "Copying binary ..."
cp ./target/${NAME}-${VERSION}.jar ${DIST}/shamirs-demo/target

echo "Making launch scripts ..."
# bash launch script
echo '#!/bin/bash' > ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo 'if [ "x${JAVA_HOME}" == "x" ]' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '  then' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '    BIN_JAVA=$(which java)' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '  else' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '    BIN_JAVA=${JAVA_HOME}/bin/java' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo 'fi' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '${BIN_JAVA} -version' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo 'SHAMIRS_VERSION='${VERSION} >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
echo '${BIN_JAVA} -Djava.security.egd=file:/dev/urandom -jar target/shamirs-demo-${SHAMIRS_VERSION}.jar' >> ${DIST}/shamirs-demo/run-shamirs-demo.sh
# winpty wrapper
echo '#!/bin/bash' > ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo '' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo 'winpty sh -i -c ./run-shamirs-demo.sh' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
# cmd launch script
echo '@echo off' > ${DIST}/shamirs-demo/run-shamirs-demo.bat
echo 'java -version' >> ${DIST}/shamirs-demo/run-shamirs-demo.bat
echo 'set SHAMIRS_VERSION='${VERSION} >> ${DIST}/shamirs-demo/run-shamirs-demo.bat
echo 'java -Dde.christofreichardt.jca.shamirsdemo.console.echo=ON -jar target/shamirs-demo-%SHAMIRS_VERSION%.jar' >> ${DIST}/shamirs-demo/run-shamirs-demo.bat
