#!/bin/bash

START_DIR=$(dirname $0)
cd ${START_DIR}/put/rest
rm -f out/put_json.log

../../TOOLS/bob/sbin/bob bu
