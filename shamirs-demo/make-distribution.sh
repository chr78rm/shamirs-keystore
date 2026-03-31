#!/bin/bash

DIST=./target/dist
VERSION=$1
NAME=$2

echo "Creating distribution shamirs-demo-${VERSION} ..."

echo "Making directories as needed ..."
mkdir --parents ${DIST}/shamirs-demo/log ${DIST}/shamirs-demo/target/lib ${DIST}/shamirs-demo/workspace ${DIST}/shamirs-demo/src

echo "Copying sources ..."
cp --recursive src/main ${DIST}/shamirs-demo/src

echo "Copying binaries ..."
cp ./target/${NAME}-${VERSION}.jar ${DIST}/shamirs-demo/target
cp ./target/lib/*.jar ${DIST}/shamirs-demo/target/lib

echo "Creating launch scripts ..."
cp ./run-demo.sh ${DIST}/shamirs-demo/run-shamirs-demo.sh
cp ./run-demo.bat ${DIST}/shamirs-demo/run-shamirs-demo.bat
cp ./winpty-demo.sh ${DIST}/shamirs-demo/winpty-shamirs-demo.sh
