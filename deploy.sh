#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

cd /projects/dinner-service

# Pull and check if anything changed
git fetch origin master
CHANGES=$(git log HEAD..origin/master --oneline)

if [ -n "$CHANGES" ]; then
    echo "[$(date)] Changes detected, pulling and restarting..."
    git pull origin master

    # Kill existing instance
    pkill -f "spring-boot:run" 2>/dev/null
    sleep 2

    # Start service in background
    nohup mvn spring-boot:run > /projects/dinner-service/service.log 2>&1 &
    echo "[$(date)] Service restarted (PID $!)"
else
    echo "[$(date)] No changes."
fi
