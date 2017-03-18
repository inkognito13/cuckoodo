#!/bin/bash

cd /code

mvn package
JAVA_BIN='/usr/lib/jvm/java-8-openjdk-amd64/bin/java'
$JAVA_BIN -jar target/cuckoodobot-jar-with-dependencies.jar

