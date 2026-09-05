# Race Calendar

A motorsport calendar application that collects and presents race weekends,
sessions and schedule updates across multiple racing series.

## Planned series

- GT World Challenge Europe
- GT World Challenge America
- Formula 1
- Formula 2
- FIA World Endurance Championship
- IMSA
- IndyCar
- NASCAR
- DTM

## Planned features

- Race weekend calendar
- Sprint and endurance event classification
- Session start times in the user's local timezone
- Schedule update tracking
- Countdown to the next session
- REST API
- iCalendar export
- Source attribution

## Technology

- Java 21
- Spring Boot
- Vaadin
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Running locally

Start PostgreSQL:

```shell
docker compose up -d
```

Start the application:

```shell
mvn clean spring-boot:run
```

Then open:

http://localhost:8080

The upcoming-session REST endpoint is available at:

http://localhost:8080/api/v1/sessions/upcoming

## Project status

This project is in early development and currently uses a demonstration calendar provider.