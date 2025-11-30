# user_email_send (Português)

Este repositório contém dois pequenos microsserviços Spring Boot que demonstram integração via RabbitMQ:

- `user` - serviço que gerencia usuários e publica uma mensagem no RabbitMQ quando um usuário é criado.
- `email` - serviço que escuta uma fila do RabbitMQ e envia e-mails (configurado com JavaMail).

Este README explica como as aplicações funcionam, como rodar localmente (usando Docker para PostgreSQL), variáveis de ambiente, endpoints e exemplos de requests para que você possa publicar o projeto no GitHub.

---

## Visão geral da arquitetura

- O serviço `user` expõe uma API REST para criar e gerenciar usuários. Ao criar um usuário ele persiste no seu banco PostgreSQL e publica uma mensagem no exchange `user_created` com a routing key `email.key`.
- O serviço `email` declara a fila `email_queue` ligada ao exchange `user_created` com a routing key `email.key`. Ele consome mensagens dessa fila, envia o e-mail usando SMTP e persiste um registro no seu banco.

Ambos os serviços usam Spring Boot, Spring Data JPA e Spring AMQP (RabbitMQ). Cada serviço tem seu próprio PostgreSQL definido em `docker-compose.yml` para desenvolvimento local.

---

## Pré-requisitos rápidos

- Java 17+
- Maven
- Docker & docker-compose (para PostgreSQL)
- Uma instância RabbitMQ (o projeto aponta por padrão para um broker AMQPS em `application.yml`; você pode rodar um RabbitMQ local)
- Conta SMTP para envio de e-mails (por exemplo Gmail) ou um servidor SMTP de teste local

---

## Serviços, portas e configuração

### serviço `user`
- Pacote base: `dev.com.user`
- Porta do servidor: 8081 (`user/src/main/resources/application.yml`)
- Postgres local (docker-compose): container `ms-user-db` mapeado para a porta host `5435`
- Exchange RabbitMQ: `user_created`

Arquivos de configuração importantes:
- `user/src/main/resources/application.yml`
- `user/docker-compose.yml` (Postgres)

Endpoints (consulte `UserController`):
- POST /api/user
  - Cria um usuário e publica a mensagem no RabbitMQ.
  - Body (JSON):
    {
      "name": "Nome completo",
      "email": "usuario@exemplo.com"
    }
  - Resposta: 201 Created com os dados do usuário criado.

- GET /api/user/all
  - Retorna a lista de usuários.

- PATCH /api/user/{id}
  - Atualiza (parcialmente) o nome do usuário.

O `UserProducer` constrói um payload compatível com `EmailResponse` e envia para o exchange `user_created` com routing key `email.key`.

### serviço `email`
- Pacote base: `dev.com.email`
- Porta do servidor: 8080 (`email/src/main/resources/application.yml`)
- Postgres local (docker-compose): container `ms-email-db` mapeado para a porta host `5433`
- RabbitMQ:
  - Fila: `email_queue`
  - Exchange: `user_created`
  - Routing key: `email.key`

Arquivos de configuração importantes:
- `email/src/main/resources/application.yml`
- `email/docker-compose.yml` (Postgres)

Variáveis de ambiente necessárias para o serviço `email` (substitua conforme necessário):
- RABBIT_USERNAME - usuário RabbitMQ para o serviço email
- RABBIT_PASSWORD - senha RabbitMQ para o serviço email
- EMAIL_USERNAME - usuário SMTP (configuração atual usa Gmail)
- EMAIL_PASSWORD - senha SMTP (use App Password para Gmail)

Exemplo do payload esperado na fila (mapeado para `EmailEntity` / `EmailResponse`):
- userId (UUID)
- emailTo (destinatário)
- emailFrom (remetente)
- emailSubject
- emailBody

Quando uma mensagem é recebida, o `EmailConsumer` converte o payload e o `EmailService` envia o e-mail e persiste um `EmailEntity` no banco.

---

## Executando localmente (recomendado)

Iniciaremos os Postgres via docker-compose e depois executamos as aplicações com Maven.

1. Inicie o Postgres do serviço `user`:

```bash
cd user
docker-compose up -d
```

2. Inicie o Postgres do serviço `email`:

```bash
cd ../email
docker-compose up -d
```

3. Garanta que o RabbitMQ esteja disponível. Por padrão as aplicações apontam para um broker AMQPS configurado em `application.yml`. Se preferir usar RabbitMQ local, rode:

```bash
# Exemplo de RabbitMQ local
docker run -d --name local-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

4. Configure as variáveis de ambiente para o serviço `email` (SMTP e RabbitMQ). Exporte no terminal ou use um arquivo .env.

Exemplo (Linux/Mac):

```bash
export RABBIT_USERNAME=myrabbituser
export RABBIT_PASSWORD=myrabbitpass
export EMAIL_USERNAME=seuemail@gmail.com
export EMAIL_PASSWORD=sua-senha-ou-app-password
```

5. Rode cada serviço com Maven (em terminais separados):

```bash
# Rodar user
cd user
./mvnw spring-boot:run

# Rodar email (outro terminal)
cd email
./mvnw spring-boot:run
```

Para gerar jars executáveis:

```bash
# Build dos módulos
./mvnw -pl user -am -DskipTests package
./mvnw -pl email -am -DskipTests package

# Rodar os jars
java -jar user/target/user-0.0.1-SNAPSHOT.jar
java -jar email/target/email-0.0.1-SNAPSHOT.jar
```

---

## Fluxo de exemplo

1. Cliente cria um usuário no serviço `user`:

POST http://localhost:8081/api/user
Body:
{
  "name": "Lucas",
  "email": "lucas@example.com"
}

2. O serviço `user` persiste o usuário e publica um payload `EmailResponse` no exchange `user_created` com routing key `email.key`.

3. O `email` consome a mensagem da fila `email_queue`, converte para `EmailEntity`, envia o e-mail via SMTP e persiste o registro.

---

## Observações e dicas

- Para Gmail use um App Password e configure `EMAIL_USERNAME` e `EMAIL_PASSWORD` adequadamente. Mantenha `mail.smtp.starttls.enable=true` e porta `587` (ver `email/src/main/resources/application.yml`).
- O projeto usa `Jackson2JsonMessageConverter` para serializar/deserializar mensagens JSON automaticamente.
- O produtor (`user`) envia um JSON compatível com os campos de `EmailEntity` que o serviço `email` espera.
- Revise os `application.yml` para ajustar hosts, portas e credenciais antes de um deploy.

---

## Estrutura do projeto (visão geral)

- user/
  - src/main/java/dev/com/user - controller, service, producer, entities, repository
  - src/main/resources/application.yml
  - docker-compose.yml (postgres)

- email/
  - src/main/java/dev/com/email - consumer, service, entities, repository
  - src/main/resources/application.yml
  - docker-compose.yml (postgres)

---

## Comandos úteis

- Rodar testes de um módulo:

```bash
cd user
./mvnw test
```

- Build dos dois módulos:

```bash
./mvnw -pl user,email -am package
```

- Acompanhar logs (ao rodar jars):

```bash
tail -f logs/application.log
```

---

Se quiser, eu também posso:
- Adicionar uma coleção Postman ou exemplos curl prontos.
- Criar um `.env.example` demonstrando as variáveis de ambiente necessárias.
- Adicionar uma workflow do GitHub Actions para build/test no push.

Diga qual desses extras prefere e eu adiciono ao repositório.
