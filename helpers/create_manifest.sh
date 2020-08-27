#!/bin/sh

# Creates a list of dependencies of this project in the form
# service-<project>/<sub-project>:<dependency-with-version>

# set -x

# Change this accordingly for your project
PROJECT=policies
SUB_PROJECT=engine

#----------
mvn dependency:list | grep -v 'policies-engine' | grep -e ':compile$' -e ':runtime$' | sed -e 's/\[INFO\] *//' -e 's/:compile$//' -e 's/:runtime$//' | sed -e "s/^/service-${PROJECT}\/${SUB_PROJECT}:/" | sort | uniq > manifest.txt
