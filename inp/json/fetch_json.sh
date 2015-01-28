#!/bin/bash

START_DIR=`dirname $0`
CLOJURE_JAR="${START_DIR}"/../../TOOLS/clj/target/clj-standalone.jar

if [ ! -d out ]; then
    mkdir out
fi

JVM_OPTS=-Xmx2g
java ${JVM_OPTS} -ea -cp "$CLOJURE_JAR" clojure.main ./fetch_json.clj
