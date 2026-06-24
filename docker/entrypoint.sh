#!/bin/sh

# Start the first process
caddy run --config /etc/caddy/Caddyfile --adapter caddyfile&
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start caddy: $status"
  exit $status
fi

# Start the second process
java -jar -Dspring.profiles.active=default /app.jar&
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start bot: $status"
  exit $status
fi

while sleep 60; do
  echo -n ""
done
