#!/bin/bash
echo "Starting memory leak simulation..."
for i in {1..60}
do
  curl http://localhost:8080/memory/leak
  echo " - Request $i completed"
  sleep 1
done
echo "Load test finished"
