#!/bin/bash

if [ $# -ne 1 ]; then
    echo "No config file name given. Exiting..."
    exit 1
fi

START_DIR=`dirname ${0}`
CONFIG_FILE=${1}
CLOJURE_JAR="${START_DIR}"/../../TOOLS/clj/target/clj-standalone.jar

if [ ! -d out ]; then
    mkdir out
fi

JVM_OPTS="-Xmx2g -Djava.net.preferIPv6Addresses=true"
java ${JVM_OPTS} -ea -cp "$CLOJURE_JAR" clojure.main ./get_json.clj ${CONFIG_FILE}
