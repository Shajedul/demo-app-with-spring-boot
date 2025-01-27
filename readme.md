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
3. Create a database and user:

   ```bash
   psql -U postgres
   CREATE DATABASE battery_db;
   CREATE USER <your-username> WITH PASSWORD '<your-password>';
   GRANT ALL PRIVILEGES ON DATABASE battery_db TO <your-username>;
   \q
   ```

   Replace `<your-username>` and `<your-password>` with your desired credentials. Update the application configuration file with these credentials:

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




# Battery Test Dataset

This repository contains a test dataset designed to simulate battery information. The dataset includes both valid and invalid entries for testing and validation purposes.

## Dataset Overview

The dataset is provided in JSON format and includes the following attributes for each battery:

- `name`: The name of the battery.
- `postcode`: A 4-digit numeric string representing the postcode.
- `wattCapacity`: An integer indicating the watt capacity of the battery (1-1000).

### Valid Data Rules
For a dataset entry to be valid, it must satisfy the following criteria:

1. All three keys (`name`, `postcode`, `wattCapacity`) must be present.
2. `name` must not be blank.
3. `postcode` must:
   - Be a 4-digit numeric string.
   - Fall within the range of `0200` to `9999`.
4. `wattCapacity` must be an integer within the range of `1` to `1000`.

### Invalid Data
The dataset intentionally includes invalid entries to test error-handling scenarios. These invalid entries may:
- Have missing keys.
- Contain blank `name` values.
- Feature out-of-range or improperly formatted `postcode` values.
- Include `wattCapacity` values outside the valid range.

## Sample Dataset
Below is a sample of the dataset:

```json
[
   { "name": "PowerSurge_1001", "postcode": "3451", "wattCapacity": 760 },
   { "name": "EnergyBlast_404", "postcode": "6012", "wattCapacity": 875 },
   { "name": "ChargePulse_328", "postcode": "7723", "wattCapacity": 250 },
   { "name": "ThunderGrid_920", "postcode": "4952", "wattCapacity": 630 },
   { "name": "LightFlow_511", "postcode": "7200", "wattCapacity": 120 },
   { "name": "BrightVolt_443", "postcode": "2134", "wattCapacity": 560 },
   { "name": "MegaCell_759", "postcode": "3469", "wattCapacity": 930 },
   { "name": "VoltTitan_305", "postcode": "8902", "wattCapacity": 101 },
   { "name": "UltraVolt_893", "postcode": "3002", "wattCapacity": 405 },
   { "name": "", "postcode": "4222", "wattCapacity": 678 },
   { "name": "EnergySpike_628", "postcode": "5545", "wattCapacity": 299 },
   { "name": "BrightPath_993", "postcode": "2456", "wattCapacity": 978 },
   { "name": "PulsePower_880", "postcode": "2234", "wattCapacity": 407 },
   { "name": "NovaCore_441", "postcode": "9821", "wattCapacity": 930 },
   { "name": "ThunderRay_306", "postcode": "2040", "wattCapacity": 488 },
   { "name": "SparkBlast_788", "postcode": "5723", "wattCapacity": 210 },
   { "name": "GlowEnergy_555", "postcode": "3345", "wattCapacity": 987 },
   { "name": "VoltBeam_325", "postcode": "9122", "wattCapacity": 762 },
   { "name": "MegaBolt_912", "postcode": "8722", "wattCapacity": 401 },
   { "name": "", "postcode": "7781", "wattCapacity": 150 },
   { "name": "PulseCell_567", "postcode": "6410", "wattCapacity": 710 },
   { "name": "LightVault_431", "postcode": "3721", "wattCapacity": 512 },
   { "name": "ChargeBoost_600", "postcode": "7812", "wattCapacity": 305 },
   { "name": "ElectroWave_453", "postcode": "2305", "wattCapacity": 940 },
   { "name": "PowerGrid_907", "postcode": "5678", "wattCapacity": 407 },
   { "name": "DynamicVolt_405", "postcode": "6890", "wattCapacity": 515 },
   { "name": "GlowSurge_215", "postcode": "4175", "wattCapacity": 670 },
   { "name": "PowerRay_315", "postcode": "9204", "wattCapacity": 930 },
   { "name": "ChargeLink_911", "postcode": "3034", "wattCapacity": 789 },
   { "name": "ElectroRay_891", "postcode": "2345", "wattCapacity": 204 },
   { "name": "HyperCell_529", "postcode": "8271", "wattCapacity": 635 },
   { "name": "MegaBoost_401", "postcode": "2135", "wattCapacity": 470 },
   { "name": "VoltPulse_309", "postcode": "7250", "wattCapacity": 879 },
   { "name": "EnergyCore_661", "postcode": "2309", "wattCapacity": 307 },
   { "name": "ChargePath_321", "postcode": "9911", "wattCapacity": 750 },
   { "name": "ThunderFlow_601", "postcode": "4213", "wattCapacity": 201 },
   { "name": "EnergyWave_882", "postcode": "6892", "wattCapacity": 469 },
   { "name": "BrightCell_111", "postcode": "2310", "wattCapacity": 530 },
   { "name": "SparkVolt_512", "postcode": "8055", "wattCapacity": 350 },
   { "name": "MegaSurge_413", "postcode": "3922", "wattCapacity": 915 },
   { "name": "VoltPath_882", "postcode": "1405", "wattCapacity": 804 },
   { "name": "PulseRay_755", "postcode": "2044", "wattCapacity": 306 },
   { "name": "ThunderCore_602", "postcode": "4421", "wattCapacity": 908 },
   { "name": "SparkFlow_554", "postcode": "5633", "wattCapacity": 701 },
   { "name": "ChargeGrid_933", "postcode": "7843", "wattCapacity": 405 },
   { "name": "", "postcode": "1204", "wattCapacity": 403 },
   { "name": "EnergyCharge_500", "postcode": "4302", "wattCapacity": 507 }
]

```

## Usage
This dataset can be used to:

- Test battery data ingestion pipelines.
- Validate API endpoints handling battery information.
- Simulate real-world data scenarios, including error handling for invalid entries.

## License
This dataset is provided for testing and educational purposes. Feel free to use and modify it as needed.

---

For any issues or suggestions, please reach out to the repository maintainer.
