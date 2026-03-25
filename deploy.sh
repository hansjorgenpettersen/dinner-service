#!/bin/bash
set -e

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

cd /projects/dinner-service

git fetch origin master
CHANGES=$(git log HEAD..origin/master --oneline)

if [ -n "$CHANGES" ]; then
    echo "[$(date)] Changes detected, pulling and building..."
    git pull origin master

    # Rebuild frontend
    cd frontend && ./node_modules/.bin/vite build && cd ..

    # Build fat JAR — skip test compilation entirely
    mvn clean package -Dmaven.test.skip=true -q

    # Restart service
    pkill -f "dinner-service.*\.jar" 2>/dev/null || true
    sleep 2
    JAR=$(ls target/dinner-service-*.jar | head -1)
    nohup java -jar "$JAR" --spring.config.additional-location=file:/projects/dinner-service/application-local.properties > /projects/dinner-service/service.log 2>&1 &
    echo "[$(date)] Service restarted (PID $!)"
else
    echo "[$(date)] No changes."
fi
