# user_email_send

This repository contains two small Spring Boot microservices demonstrating integration via RabbitMQ:

- `user` - a service that manages users and publishes a message to RabbitMQ when a user is created.
- `email` - a service that listens to a RabbitMQ queue and sends emails (using JavaMail configuration).

This README documents how the applications work, how to run them locally (with Docker for PostgreSQL), environment variables, endpoints and example requests.

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

----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

user_email_send (Português)
Este repositório contém dois pequenos microsserviços Spring Boot que demonstram integração via RabbitMQ:

user - serviço que gerencia usuários e publica uma mensagem no RabbitMQ quando um usuário é criado.
email - serviço que escuta uma fila do RabbitMQ e envia e-mails (configurado com JavaMail).
Este README explica como as aplicações funcionam, como rodar localmente (usando Docker para PostgreSQL), variáveis de ambiente, endpoints e exemplos de requests.

Visão geral da arquitetura
O serviço user expõe uma API REST para criar e gerenciar usuários. Ao criar um usuário ele persiste no seu banco PostgreSQL e publica uma mensagem no exchange user_created com a routing key email.key.
O serviço email declara a fila email_queue ligada ao exchange user_created com a routing key email.key. Ele consome mensagens dessa fila, envia o e-mail usando SMTP e persiste um registro no seu banco.
Ambos os serviços usam Spring Boot, Spring Data JPA e Spring AMQP (RabbitMQ). Cada serviço tem seu próprio PostgreSQL definido em docker-compose.yml para desenvolvimento local.

Pré-requisitos rápidos
Java 17+
Maven
Docker & docker-compose (para PostgreSQL)
Uma instância RabbitMQ (o projeto aponta por padrão para um broker AMQPS em application.yml; você pode rodar um RabbitMQ local)
Conta SMTP para envio de e-mails (por exemplo Gmail) ou um servidor SMTP de teste local
Serviços, portas e configuração
serviço user
Pacote base: dev.com.user
Porta do servidor: 8081 (user/src/main/resources/application.yml)
Postgres local (docker-compose): container ms-user-db mapeado para a porta host 5435
Exchange RabbitMQ: user_created
Arquivos de configuração importantes:

user/src/main/resources/application.yml
user/docker-compose.yml (Postgres)
Endpoints (consulte UserController):

POST /api/user

Cria um usuário e publica a mensagem no RabbitMQ.
Body (JSON): { "name": "Nome completo", "email": "usuario@exemplo.com" }
Resposta: 201 Created com os dados do usuário criado.
GET /api/user/all

Retorna a lista de usuários.
PATCH /api/user/{id}

Atualiza (parcialmente) o nome do usuário.
O UserProducer constrói um payload compatível com EmailResponse e envia para o exchange user_created com routing key email.key.

serviço email
Pacote base: dev.com.email
Porta do servidor: 8080 (email/src/main/resources/application.yml)
Postgres local (docker-compose): container ms-email-db mapeado para a porta host 5433
RabbitMQ:
Fila: email_queue
Exchange: user_created
Routing key: email.key
Arquivos de configuração importantes:

email/src/main/resources/application.yml
email/docker-compose.yml (Postgres)
Variáveis de ambiente necessárias para o serviço email (substitua conforme necessário):

RABBIT_USERNAME - usuário RabbitMQ para o serviço email
RABBIT_PASSWORD - senha RabbitMQ para o serviço email
EMAIL_USERNAME - usuário SMTP (configuração atual usa Gmail)
EMAIL_PASSWORD - senha SMTP (use App Password para Gmail)
Exemplo do payload esperado na fila (mapeado para EmailEntity / EmailResponse):

userId (UUID)
emailTo (destinatário)
emailFrom (remetente)
emailSubject
emailBody
Quando uma mensagem é recebida, o EmailConsumer converte o payload e o EmailService envia o e-mail e persiste um EmailEntity no banco.

Executando localmente (recomendado)
Iniciaremos os Postgres via docker-compose e depois executamos as aplicações com Maven.

Inicie o Postgres do serviço user:
cd user
docker-compose up -d
Inicie o Postgres do serviço email:
cd ../email
docker-compose up -d
Garanta que o RabbitMQ esteja disponível. Por padrão as aplicações apontam para um broker AMQPS configurado em application.yml. Se preferir usar RabbitMQ local, rode:
# Exemplo de RabbitMQ local
docker run -d --name local-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
Configure as variáveis de ambiente para o serviço email (SMTP e RabbitMQ). Exporte no terminal ou use um arquivo .env.
Exemplo (Linux/Mac):

export RABBIT_USERNAME=myrabbituser
export RABBIT_PASSWORD=myrabbitpass
export EMAIL_USERNAME=seuemail@gmail.com
export EMAIL_PASSWORD=sua-senha-ou-app-password
Rode cada serviço com Maven (em terminais separados):
# Rodar user
cd user
./mvnw spring-boot:run

# Rodar email (outro terminal)
cd email
./mvnw spring-boot:run
Para gerar jars executáveis:

# Build dos módulos
./mvnw -pl user -am -DskipTests package
./mvnw -pl email -am -DskipTests package

# Rodar os jars
java -jar user/target/user-0.0.1-SNAPSHOT.jar
java -jar email/target/email-0.0.1-SNAPSHOT.jar
Fluxo de exemplo
Cliente cria um usuário no serviço user:
POST http://localhost:8081/api/user Body: { "name": "Lucas", "email": "lucas@example.com" }

O serviço user persiste o usuário e publica um payload EmailResponse no exchange user_created com routing key email.key.

O email consome a mensagem da fila email_queue, converte para EmailEntity, envia o e-mail via SMTP e persiste o registro.

Observações e dicas
Para Gmail use um App Password e configure EMAIL_USERNAME e EMAIL_PASSWORD adequadamente. Mantenha mail.smtp.starttls.enable=true e porta 587 (ver email/src/main/resources/application.yml).
O projeto usa Jackson2JsonMessageConverter para serializar/deserializar mensagens JSON automaticamente.
O produtor (user) envia um JSON compatível com os campos de EmailEntity que o serviço email espera.
Revise os application.yml para ajustar hosts, portas e credenciais antes de um deploy.
Estrutura do projeto (visão geral)
user/

src/main/java/dev/com/user - controller, service, producer, entities, repository
src/main/resources/application.yml
docker-compose.yml (postgres)
email/

src/main/java/dev/com/email - consumer, service, entities, repository
src/main/resources/application.yml
docker-compose.yml (postgres)
Comandos úteis
Rodar testes de um módulo:
cd user
./mvnw test
Build dos dois módulos:
./mvnw -pl user,email -am package
Acompanhar logs (ao rodar jars):
tail -f logs/application.log
