# user_email_send

This repository contains two small Spring Boot microservices demonstrating integration via RabbitMQ:

- `user` - a service that manages users and publishes a message to RabbitMQ when a user is created.
- `email` - a service that listens to a RabbitMQ queue and sends emails (using JavaMail configuration).

This README documents how the applications work, how to run them locally (with Docker for PostgreSQL), environment variables, endpoints and example requests so you can publish the project to GitHub.

---

## Architecture overview

- The `user` service exposes a REST API to create and manage users. When a user is created it saves the user to its PostgreSQL database and publishes a message to the `user_created` exchange with a routing key `email.key`.
- The `email` service declares a queue `email_queue` bound to the `user_created` exchange with routing key `email.key`. It listens to `email_queue`, receives messages containing email payloads and sends the email using SMTP (configured in `application.yml`). The email record is saved in the `email` service database.

Both services use Spring Boot, Spring Data JPA and Spring AMQP (RabbitMQ). Each service has its own PostgreSQL instance defined in their `docker-compose.yml` files for local development.

---

## Quickstart - prerequisites

- Java 17+ (project uses modern Spring Boot)
- Maven
- Docker & docker-compose (to run PostgreSQL locally)
- A RabbitMQ instance (the project uses an external AMQPS broker in `application.yml` by default; you can run a local RabbitMQ instead)
- An SMTP account for sending emails (e.g. Gmail) or a local SMTP testing service

---

## Services, ports and configuration

### user service
- Base package: `dev.com.user`
- Server port: 8081 (see `user/src/main/resources/application.yml`)
- Local Postgres (docker-compose): container `ms-user-db` mapped to host port `5435`
- RabbitMQ exchange: `user_created`

Important config locations:
- `user/src/main/resources/application.yml`
- `user/docker-compose.yml` (Postgres container)

Environment variables (overrides recommended for production):
- (RabbitMQ credentials are in `application.yml`; you may want to replace them with secure env vars.)

Endpoints (see `UserController`):
- POST /api/user
  - Create a user and publish a message to RabbitMQ.
  - Request body (JSON):
    {
      "name": "Full name",
      "email": "user@example.com"
    }
  - Response: 201 Created with the created user payload.

- GET /api/user/all
  - Returns a list of users.

- PATCH /api/user/{id}
  - Update user's name (partial update supported via `UserUpdateRequest`).

The `UserProducer` builds an `EmailResponse` payload and sends it to the `user_created` exchange with routing key `email.key`.

### email service
- Base package: `dev.com.email`
- Server port: 8080 (see `email/src/main/resources/application.yml`)
- Local Postgres (docker-compose): container `ms-email-db` mapped to host port `5433`
- RabbitMQ:
  - Queue: `email_queue`
  - Exchange: `user_created`
  - Binding key: `email.key`

Important config locations:
- `email/src/main/resources/application.yml`
- `email/docker-compose.yml` (Postgres container)

Environment variables required for email service (replace in your environment or an `.env` file):
- RABBIT_USERNAME - RabbitMQ username used by the email service
- RABBIT_PASSWORD - RabbitMQ password used by the email service
- EMAIL_USERNAME - SMTP username (the application uses Gmail SMTP in the config)
- EMAIL_PASSWORD - SMTP password (for Gmail consider using an App Password)

Example payload the `email` service expects on the queue (structure mapped to `EmailEntity` / `EmailResponse`):
- userId (UUID)
- emailTo (recipient)
- emailFrom (sender)
- emailSubject
- emailBody

When a message is received, `EmailConsumer` maps the payload and `EmailService` sends the email and persists an `EmailEntity` to the service DB.

---

## Running locally (recommended)

We'll start each service's PostgreSQL using its docker-compose, then run the application with Maven.

1. Start Postgres for the `user` service:

```bash
cd user
docker-compose up -d
```

2. Start Postgres for the `email` service:

```bash
cd ../email
docker-compose up -d
```

3. Ensure RabbitMQ is available. By default the apps point to an AMQPS broker configured in `application.yml`. If you prefer to run a local RabbitMQ container, start one and update the `application.yml` files to point to `amqp://localhost` (or set env vars accordingly):

```bash
# Example local rabbitmq
docker run -d --name local-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

4. Configure environment variables for the `email` service (SMTP and RabbitMQ credentials). You can export them in your shell or create an env file and load it.

Example (Linux/Mac):

```bash
export RABBIT_USERNAME=myrabbituser
export RABBIT_PASSWORD=myrabbitpass
export EMAIL_USERNAME=youremail@gmail.com
export EMAIL_PASSWORD=your-email-password-or-app-password
```

5. Run each service with Maven (from the repo root or service folder):

```bash
# Run user service
cd user
./mvnw spring-boot:run

# In another terminal, run email service
cd email
./mvnw spring-boot:run
```

Each service will auto-create tables (hbm2ddl=update). You can also build executable jars:

```bash
# Build both services from repo root
./mvnw -pl user -am -DskipTests package
./mvnw -pl email -am -DskipTests package

# Run the jars
java -jar user/target/user-0.0.1-SNAPSHOT.jar
java -jar email/target/email-0.0.1-SNAPSHOT.jar
```

---

## Example flow

1. Client calls the `user` service to create a user:

POST http://localhost:8081/api/user
Body:
{
  "name": "Lucas",
  "email": "lucas@example.com"
}

2. The `user` service saves the user to its DB and publishes an `EmailResponse` payload to the `user_created` exchange with routing key `email.key`.

3. The `email` service's `EmailConsumer` receives the message from queue `email_queue`, maps it to `EmailEntity`, and `EmailService` sends the email using configured SMTP settings and persists the `EmailEntity`.

---

## Notes and tips

- For Gmail you usually need an App Password and set `EMAIL_USERNAME` and `EMAIL_PASSWORD` accordingly. Also ensure `mail.smtp.starttls.enable=true` and port `587` are correct (see `email/src/main/resources/application.yml`).
- The project uses `Jackson2JsonMessageConverter` so messages are JSON serialized/deserialized automatically.
- The `email` service expects `EmailEntity` fields; the `user` service's producer sends `EmailResponse` shaped JSON that matches.
- Review `application.yml` files to configure hostnames, ports and credentials for production-ready deployments.

---

## Project structure (high level)

- user/
  - src/main/java/dev/com/user - controller, service, producer, entities, repository
  - src/main/resources/application.yml
  - docker-compose.yml (postgres)

- email/
  - src/main/java/dev/com/email - consumer, service, entities, repository
  - src/main/resources/application.yml
  - docker-compose.yml (postgres)

---

## Helpful commands

- Run tests for a module:

```bash
cd user
./mvnw test
```

- Build both modules:

```bash
./mvnw -pl user,email -am package
```

- Tail application logs (when running jars):

```bash
tail -f logs/application.log
```

---

If you want, I can also:
- Add a small Postman collection or curl examples for the endpoints.
- Add a `.env.example` demonstrating required environment variables for both services.
- Create a GitHub Actions workflow to build and run tests on push.

Tell me which extras you'd like and I'll add them to the repo.
