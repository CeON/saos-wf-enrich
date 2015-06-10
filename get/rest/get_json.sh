#!/bin/bash

START_DIR=`dirname $0`
CLOJURE_JAR="${START_DIR}"/../../TOOLS/clj/target/clj-standalone.jar

if [ ! -d out ]; then
    mkdir out
fi

JVM_OPTS="-Xmx2g -Djava.net.preferIPv6Addresses=true"
java ${JVM_OPTS} -ea -cp "$CLOJURE_JAR" clojure.main ./get_json.clj "${START_DIR}"/../../config
