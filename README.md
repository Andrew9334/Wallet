# Test task from JavaCode

Wallet API — это RESTful приложение, предназначенное для управления кошельками. Оно позволяет выполнять операции по пополнению и снятию средств, а также получать баланс кошелька.

## Стек технологий

- **Java**: 17
- **Spring Boot**: 3
- **PostgreSQL**: для хранения данных
- **Liquibase**: для миграций базы данных
- **Docker**: для контейнеризации приложения и базы данных
- **JUnit & Mockito**: для тестирования

## Эндпоинты

### 1. Создание/обновление кошелька

**POST** `/api/v1/wallet`

#### Запрос

```json
{
  "walletId": "UUID",
  "operationType": "DEPOSIT" | "WITHDRAW",
  "amount": 1000
}
