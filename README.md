# PulseCore

Платформа для отслеживания результатов турниров по настольному теннису. Автоматически собирает данные с masters-league.com, уведомляет игроков о предстоящих матчах и подсчитывает заработок.

**Сайт:** [pulsecore-app.ru](https://pulsecore-app.ru)

---

## Возможности

- 🔔 Уведомления о добавлении в состав на ближайшие матчи
- 💰 Автоматический подсчёт заработка за выбранный период
- 📊 Статистика по турнирам и лигам
- 📅 Отображение составов на ближайшие дни
- 💳 Платная подписка через ЮKassa
- 🔐 Регистрация, сброс пароля, управление профилем

---

## Технологии

- Java 21
- Spring Boot 3.4.4
- Spring Security, Spring Data JPA
- PostgreSQL 15
- Flyway (миграции БД)
- Docker / Docker Compose
- Jsoup (парсинг)
- JavaMail (email-уведомления)
- ЮKassa API (приём платежей)

---

## Локальный запуск

1. Установи JDK 21, Docker, Maven
2. Подними PostgreSQL:
   ```bash
   docker run -d --name postgres_db -e POSTGRES_DB=botdb -e POSTGRES_USER=botuser -e POSTGRES_PASSWORD=1234 -p 5432:5432 postgres:15