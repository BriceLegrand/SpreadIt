#!/bin/sh
nohup java -jar spreadit-server-CloudSQL-1.4.jar > spreadit-log.txt 2> spreadit-log.txt < /dev/null &
PID=$!
echo $PID > pid.txt
