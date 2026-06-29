#!/bin/bash
pkill -f "java -jar" 2>/dev/null || true
sleep 2
nohup java -jar /home/ubuntu/app/app.jar > /home/ubuntu/app/app.log 2>&1 &
echo "PID: $!"
echo "Started"
