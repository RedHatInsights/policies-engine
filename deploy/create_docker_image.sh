#!/bin/sh

JAR=../target/engine*-runner.jar

# Build the app when needed
if [ ! -e $JAR ]
then
  cd ..
  mvn package -DskipTests
  cd -
fi

# Now assemble stuff in tmp
if [ ! -e tmp ]
then
  mkdir tmp
fi
cp -R ../target/lib tmp/
cp $JAR tmp/

# And build the container image
docker build -t cp-engine:latest -f Dockerfile tmp
