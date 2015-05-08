#!/bin/bash

cd TOOLS/bob
lein do clean, uberjar
cd ../clj/
./bld.sh
