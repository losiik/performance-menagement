#!/bin/bash

echo "=== Database Benchmark: PostgreSQL vs MongoDB ==="
echo ""

# Функция для красивого вывода
measure() {
    echo "Testing: $1"
    response=$(curl -s $2)
    echo "$response" | jq '.'
    echo ""
}

# Очистка
echo "Clearing databases..."
curl -s -X DELETE http://localhost:8080/api/benchmark/postgres/clear
curl -s -X DELETE http://localhost:8080/api/benchmark/mongo/clear
echo ""

# 1. INSERT ONE
echo "=== 1. INSERT ONE RECORD ==="
measure "PostgreSQL" "http://localhost:8080/api/benchmark/postgres/insert-one" -X POST
measure "MongoDB" "http://localhost:8080/api/benchmark/mongo/insert-one" -X POST

# 2. BATCH INSERT
echo "=== 2. BATCH INSERT (1000 records) ==="
measure "PostgreSQL" "http://localhost:8080/api/benchmark/postgres/insert-batch?count=1000" -X POST
measure "MongoDB" "http://localhost:8080/api/benchmark/mongo/insert-batch?count=1000" -X POST

# 3. READ BY ID
echo "=== 3. READ BY ID ==="
measure "PostgreSQL" "http://localhost:8080/api/benchmark/postgres/read-one/1"
measure "MongoDB" "http://localhost:8080/api/benchmark/mongo/read-one/1"

# 4. FILTER
echo "=== 4. FILTER BY COMPLETED STATUS ==="
measure "PostgreSQL" "http://localhost:8080/api/benchmark/postgres/filter-completed"
measure "MongoDB" "http://localhost:8080/api/benchmark/mongo/filter-completed"

echo "=== 5. FILTER LAST WEEK ==="
measure "PostgreSQL" "http://localhost:8080/api/benchmark/postgres/filter-week"
measure "MongoDB" "http://localhost:8080/api/benchmark/mongo/filter-week"

echo "Benchmark completed!"
