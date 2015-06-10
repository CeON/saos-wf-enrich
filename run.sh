#!/bin/bash

if [ ! -f config ]; then
    echo "[LOG] Failed reading parameters file \"config\". Exiting..."
    exit 1
fi

date +"[LOG] %Y-%m-%d %H:%M Started analysis."

START_DIR=$(dirname $0)

cd ${START_DIR}


# This defines numProc variable
numProc=1
eval `grep -e "^\s*numProc" config`

echo ${numProc}
exit 0

rm -f bob.log get.log

cd get/rest
rm -rf out
./get_json.sh > ../../get.log 2>&1

if [ $? == 0 ]; then
    date +"[LOG] %Y-%m-%d %H:%M Success fetching data."
    cd ../../
    TOOLS/bob/sbin/bob cl -f > bob.log 2>&1
    TOOLS/bob/sbin/bob bu -c${numProc} >> bob.log 2>&1

    if [ $? == 0 ]; then
        date +"[LOG] %Y-%m-%d %H:%M Success processing data."
    else
        date +"[LOG] %Y-%m-%d %H:%M Failed processing data."
    fi
else
    date +"[LOG] %Y-%m-%d %H:%M Failed fetching data."
    cd ../../
fi
