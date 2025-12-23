.PHONY: help up down restart logs status clean up-db down-db up-monitoring down-monitoring up-redis down-redis

PROJECT_NAME := performance-management

help:
	@echo Available commands:
	@echo   up              - Запустить всю инфраструктуру
	@echo   down            - Остановить всю инфраструктуру
	@echo   restart         - Перезапустить всю инфраструктуру
	@echo   up-db           - Запустить только базы данных
	@echo   up-monitoring   - Запустить только мониторинг
	@echo   up-redis        - Запустить только Redis
	@echo   status          - Показать статус сервисов
	@echo   logs            - Показать логи
	@echo   clean           - Удалить все контейнеры

up:
	@echo Starting all infrastructure...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml up -d
	docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml up -d
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml up -d
	@echo Done!

up-db:
	@echo Starting databases...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml up -d

up-monitoring:
	@echo Starting monitoring...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml up -d

up-redis:
	@echo Starting Redis...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml up -d

down:
	@echo Stopping all services...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml down
	docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml down
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml down

down-db:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml down

down-monitoring:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml down

down-redis:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml down

restart: down up

logs:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml logs -f

logs-db:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml logs -f

logs-monitoring:
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml logs -f

status:
	@echo === Databases ===
	@docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml ps
	@echo.
	@echo === Redis ===
	@docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml ps
	@echo.
	@echo === Monitoring ===
	@docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml ps

clean:
	@echo Cleaning up...
	docker-compose -p $(PROJECT_NAME) -f docker-compose-databases.yml down -v
	docker-compose -p $(PROJECT_NAME) -f docker-compose-redis.yml down -v
	docker-compose -p $(PROJECT_NAME) -f docker-compose-observability.yml down -v
