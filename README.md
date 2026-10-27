# Notes Service

REST API для управления заметками (Spring Boot 3.3, Java 17, in-memory хранилище).

## Запуск
    mvn spring-boot:run
    docker build -t notes-service . && docker run -p 8080:8080 notes-service

## Тесты
    mvn test

## API
| Метод  | Путь          | Успех          | Ошибки   |
|--------|---------------|----------------|----------|
| POST   | /notes        | 201 + Location | 400      |
| GET    | /notes        | 200            | 400      |
| GET    | /notes?tag=x  | 200            | 400      |
| GET    | /notes/{id}   | 200            | 400, 404 |
| PUT    | /notes/{id}   | 200            | 400, 404 |
| DELETE | /notes/{id}   | 204            | 404      |

## Примеры
    curl -i -X POST localhost:8080/notes -H 'Content-Type: application/json' \
      -d '{"title":"Release plan","content":"Check CI","tags":["work","urgent"]}'
    curl 'localhost:8080/notes?tag=work'
    curl -i -X DELETE localhost:8080/notes/1

## Правила данных

- title обязателен, после удаления пробелов по краям — до 200 символов.
- content обязателен, допускается пустая строка, до 10 000 символов.
- tags можно не передавать: будет пустое множество. Пустые теги запрещены, длина тега — до 50 символов.
- Теги приводятся к нижнему регистру независимо от языка системы; фильтр использует точное совпадение нормализованного тега.
- id и createdAt назначает сервер. PUT полностью заменяет title/content/tags, сохраняя createdAt.
- Данные хранятся в памяти и теряются после перезапуска. Авторизация и постоянное хранение в это тестовое не входят.

## Запуск собранного приложения

```sh
mvn clean verify
java -jar target/notes-service-1.0.0.jar
```

Ошибки возвращаются в формате Problem Details с полями status, title и detail.
