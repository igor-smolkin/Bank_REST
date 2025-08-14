# Система управления банковскими картами

Это Spring Boot REST API для работы с банковскими картами. Проект собран на Gradle и запускается в Docker-контейнерах (приложение + PostgreSQL + pgAdmin).

---
## Содержание

1. [Предварительные требования](#предварительные-требования)
2. [Структура проекта](#структура-проекта)
3. [Генерация OpenAPI спецификации](#генерация-openapi-спецификации)
4. [Быстрый старт с Docker Compose](#быстрый-старт-с-docker-compose)
5. [Локальный запуск без Docker (профиль `local`)](#локальный-запуск-без-docker-профиль-dev)
6. [Документация API](#документация-api)
7. [Уже созданные пользователи](#уже-созданные-пользователи)

---

## Предварительные требования

- Установленный [Docker](https://docs.docker.com/get-docker/)  
- Установленный [Docker Compose](https://docs.docker.com/compose/install/)  
- (При локальном запуске) Java 17+ и Gradle 8.14+

---

## Структура проекта

```
Bank_REST
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── docs/
│   └── openapi.yaml
└── src/
    ├── main/
    │   ├── java/com/example/bankcards/… 
    │   └── resources/
    │       ├── application.yml
    │       ├── application-docker.yml
    │       └── application-local.yml
    └── test/
        └── java/…
```

---

## Быстрый старт с Docker Compose

1. **Клонировать репозиторий и перейти в директорию проекта**  
   ```bash
   git clone <URL_репозитория>
   cd Bank_REST
   ```

2. **Собрать образы и запустить контейнеры**  
   ```bash
   docker-compose up --build -d
   ```

3. **Проверить сервисы**  
   - API: `http://localhost:8080`  
   - pgAdmin: `http://localhost:5433`  
     — логин: `admin@admin.com`, пароль: `admin`
4. **Удалить все образы и контейнер**
   ```bash
    docker-compose down --rmi all -v
   ```


### Состав контейнеров

| Сервис       | Образ              | Порт (хост → контейнер) | Описание                                |
|--------------|--------------------|-------------------------|-----------------------------------------|
| **postgres** | `postgres:15`      | `5432 → 5432`           | PostgreSQL с БД `bank_rest_db`          |
| **pgadmin**  | `dpage/pgadmin4`   | `5433 → 80`             | Веб-интерфейс для управления PostgreSQL |
| **app**      | `bank-rest:latest` | `8080 → 8080`           | Spring Boot приложение                  |

---

## Локальный запуск без Docker

1. Убедитесь, что установлены Java 17+ и Gradle 8.14+.  
2. В каталоге проекта выполните:

   ```bash
   # на Unix/macOS
   ./gradlew clean build
   ./gradlew bootRun --args='--spring.profiles.active=local'

   # на Windows PowerShell
   .\gradlew.bat clean build
   .\gradlew.bat bootRun --args="--spring.profiles.active=local"
   ```

3. Перейдите в браузере по адресу:  
   `http://localhost:8080`
---

## Генерация OpenAPI спецификации

Чтобы сгенерировать файл `docs/openapi.yaml`, выполните в корне проекта (сервер должен быть запущен и доступен):

```bash
  ./gradlew openApiGenerate
```


## Документация API

- **OpenAPI (YAML):** `docs/openapi.yaml`  
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Уже созданные пользователи

   По дефолту уже созданы 2 пользователя: Админ и Обычный пользователь.

- **Admin**
  - Логин: `admin`
  - Пароль: `admin`
- **User:**
  - Логин: `user`
  - Пароль: `user`
---

