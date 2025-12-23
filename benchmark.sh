#!/bin/bash

echo "=== Cache Benchmark ==="
echo ""

# Тест 1: Первый вызов (cache miss)
echo "Test 1: First call (cache miss)"
time curl -s http://localhost:8080/api/tasks > /dev/null
echo ""

# Тест 2: Второй вызов (cache hit)
echo "Test 2: Second call (cache hit)"
time curl -s http://localhost:8080/api/tasks > /dev/null
echo ""

# Тест 3: После обновления (cache evict)
echo "Test 3: Update task (cache eviction)"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"title":"Updated","description":"Test","completed":true}' \
  http://localhost:8080/api/tasks/1 > /dev/null
echo ""

# Тест 4: После инвалидации (cache miss)
echo "Test 4: After eviction (cache miss again)"
time curl -s http://localhost:8080/api/tasks > /dev/null
echo ""

# Нагрузка 100 запросов
echo "Load test: 100 requests"
time for i in {1..100}; do
  curl -s http://localhost:8080/api/tasks > /dev/null
done
echo ""
