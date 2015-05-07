#!/bin/bash

cd TOOLS/bob
lein uberjar
cd ../clj/
./bld.sh
