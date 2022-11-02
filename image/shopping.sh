#!/bin/sh

echo "Entrypoint script started"

echo "App directory:"
ls -ld $(find /app)

exec java \
         -classpath /app/classpath/*:/app/libs/*:/app/resources:/app/classes \
         -server \
         -Xms256m \
         -Xmx256m \
         -XX:+UnlockExperimentalVMOptions \
         -XX:+UseZGC \
         -XX:+HeapDumpOnOutOfMemoryError \
         -XX:-OmitStackTraceInFastThrow \
         -XX:+ExitOnOutOfMemoryError \
         -Djava.net.preferIPv4Stack=true \
         -Dspring.config.location=classpath:/ \
         ${EXTRA_JAVA_OPTS:-} \
         @/app/jib-main-class-file

echo "Entrypoint script done"

