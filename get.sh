#!/bin/bash

CONFIG_FILE=config

START_DIR=$(dirname $0)
cd ${START_DIR}/get/rest

time -p ./get_json.sh ../../${CONFIG_FILE}
