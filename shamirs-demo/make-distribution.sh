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

echo "Creating launch scripts ..."
cp ./run-uber-demo.sh ${DIST}/shamirs-demo/run-shamirs-demo.sh
cp ./run-uber-demo.bat ${DIST}/shamirs-demo/run-shamirs-demo.bat
# winpty wrapper
echo '#!/bin/bash' > ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo '' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo '# you must quote the arguments which are passed through, like:' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo '# ./winpty-shamirs-demo.sh "--no-bulk --jline --echo"' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo '' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
echo 'winpty sh -i -c "./run-shamirs-demo.sh $@"' >> ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
