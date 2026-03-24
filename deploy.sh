#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

cd /projects/dinner-service

# Pull and check if anything changed
git fetch origin master
CHANGES=$(git log HEAD..origin/master --oneline)

if [ -n "$CHANGES" ]; then
    echo "[$(date)] Changes detected, pulling and building..."
    git pull origin master

    # Build fat JAR (skip tests for speed; CI should run tests separately)
    mvn package -DskipTests -q

    # Kill existing instance
    pkill -f "dinner-service.*\.jar" 2>/dev/null
    sleep 2

    # Start service in background
    JAR=$(ls target/dinner-service-*.jar | head -1)
    nohup java -jar "$JAR" > /projects/dinner-service/service.log 2>&1 &
    echo "[$(date)] Service restarted (PID $!)"
else
    echo "[$(date)] No changes."
fi
