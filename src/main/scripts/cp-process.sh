#!/bin/bash

id=$(ps aux | grep amap-cp-process-thread | grep -v grep | cut -d ' ' -f 6,7 | grep -oP '\d{3,6'})
if [ $id > 0 ]
then
echo 'kill id '$id
kill -9 $id
fi

java -cp ./lib  -jar ./cp-libs/amap-cp-process-thread-1.0-SNAPSHOT.jar > /dev/null 2>&1 &

echo $(ps aux | grep amap-cp-process-thread | grep -v grep | cut -d ' ' -f 6,7)

date


