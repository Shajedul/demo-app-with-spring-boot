# Spring Boot Application - Setup Guide

This README file provides instructions to set up and run the Spring Boot application along with PostgreSQL and RabbitMQ. It also includes steps to run the tests and generate a test coverage report using JaCoCo.

---

## Prerequisites

Ensure you have the following installed:

- [Java 17+](https://adoptopenjdk.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [RabbitMQ](https://www.rabbitmq.com/)

---

## Step 1: Clone the Repository and Install Dependencies

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Install the required Maven dependencies:

   ```bash
   mvn clean install
   ```

Alternatively, you can use an IDE like IntelliJ IDEA or Eclipse that supports Maven to import the project. The IDE will automatically resolve and download the dependencies.

---

## Step 2: Setting Up the Environment

### 2.1. Install and Configure PostgreSQL

1. Download and install PostgreSQL from [here](https://www.postgresql.org/download/).
2. Start the PostgreSQL service.
3. Create a database named `battery_db`:

   ```bash
   psql -U postgres
   CREATE DATABASE battery_db;
   \q
   ```

   Replace the default credentials in the application with your PostgreSQL credentials. Example:

   ```properties
   spring.datasource.url=jdbc:postgresql://<your-host>:<your-port>/<your-database>
   spring.datasource.username=<your-username>
   spring.datasource.password=<your-password>
   ```

### 2.2. Install and Configure RabbitMQ

1. Download and install RabbitMQ from [here](https://www.rabbitmq.com/download.html).
2. Start the RabbitMQ service.
3. Access the RabbitMQ Management Console at `http://localhost:15672`.
   - Default credentials:
      - Username: `guest`
      - Password: `guest`

---

## Step 3: Running the Spring Boot Application

### 3.1. Configure Application Properties

Use the following `application.properties` configuration:

```properties
spring.application.name=battery-api
server.port=8081

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://<your-host>:<your-port>/<your-database>
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jackson.serialization.FAIL_ON_EMPTY_BEANS=false

# Hibernate Batch Settings
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.generate_statistics=true
spring.jpa.properties.hibernate.flush.mode=MANUAL
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

logging.level.org.hibernate.engine.jdbc.batch=TRACE

# RabbitMQ Configuration
rabbitmq.queue.batteries=batteryQueue
spring.amqp.deserialization.trust.all=true

# Logging Levels
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.level.org.springframework=ERROR

# Hikari Connection Pool Settings
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.max-lifetime=300000

spring.rabbitmq.listener.simple.auto-startup=true
```

### 3.2. Start the Application

Use Maven to run the application:

```bash
mvn spring-boot:run
```

Verify the application is running by visiting `http://localhost:8081` in your browser.

---

## Step 4: Running Tests

Run the tests using Maven:

```bash
mvn test
```

> **Important:** Ensure that **Docker** is running before executing the tests. Tests rely on TestContainer running within Docker containers.

---

## Step 5: Generating Test Coverage Report

The project uses [JaCoCo](https://www.jacoco.org/) for test coverage. To generate the test coverage report:

1. Run the tests with the `jacoco` profile enabled:

   ```bash
   mvn test -Pjacoco
   ```

2. Generate the report:

   ```bash
   mvn jacoco:report
   ```

3. View the report:

   Open `target/site/jacoco/index.html` in your browser.

---

## Cleanup

To stop the services:

- For PostgreSQL, stop the service using your system's service manager.
- For RabbitMQ, stop the service using your system's service manager.

---

### Notes

- Ensure the PostgreSQL and RabbitMQ services are running before starting the Spring Boot application.
- If you encounter issues, check the logs of the services:
   - PostgreSQL: Check your system logs or `pg_log` directory.
   - RabbitMQ: Check the RabbitMQ logs in the installation directory.

Enjoy building your application!
