# MOLB Agency Portal Backend

This document lists the steps to get this up and running.

## Prerequisites

- Java 11
- Gradle

To run the supporting services, you need:

- Docker
- Docker Compose

## Setup

#### Configure your IDEA

- Import code style
    - Open IDEA, enter `Preferences -> Editor -> Code Style -> Import Schema`
    - Choose `config/GoogleStyle.xml`
      > This style is based on GoogleStyle and default Kotlin style of Intellij Team, is suitable for both Java and Kotlin.

- New line end of file
    - Enter `Preferences -> Editor -> General`
    - Check on `Other: Ensure line feed at file end on Save`
    - For newer versions of IntelliJ, Check on `Save Files: Ensure every saved file ends with a line break`

- Set jvm of gradle
    - Enter `Build, Execution, Deployment -> Build Tools -> Gradle`
    - Select `Gradle JVM: Use Project JDK`
  > If the jvm is different with your project, will cause 'No tests found' error

### Lint

If you are using InteliJ for backend development. Please follow the steps to configure code format rule of the IDE.

execute command: `./gradlew ktlintApplyToIdea` inside project’s root directory

you can use `./gradlew ktlintFormat` to format code.

for more details see [ktlint-gradle](https://github.com/JLLeitschuh/ktlint-gradle)

### WOG AD

See https://confluence.ship.gov.sg/display/AGENCYOLVM/WOG+AD+Setup

## Run

### Supporting services only (for backend development)

#### From your terminal:

***1. Start local DB***

```
./docker/common.sh
```

***1.2 Running application with access to uat database***

To build service configuration that accesses uat env database, run the below command instead:

```
./docker/common.sh uat
```

***2. Start service locally***

```
./gradlew bootRun
```

***3. Access Open API locally:***

```
TODO: Using default Spring Security credentials for now. To implement proper authentication
URL: http://localhost:8088/swagger-ui/index.html
Username: user
Password: Search the logs for "Using generated security password"
```

## Flyway

### Creating a new Flyway migration script file

#### From your terminal:

```
./gradlew flywayNewMigration -PmigrationName=<migration-name>
```

where <migration-name> = description of the migration script

i.e.

migration-name = `create_audit_logs`

new migration filename = `V202205121113__create_audit_logs.sql`

## Test

### JaCoCo Coverage Test

#### Excluding Classes and/or Functions from the Coverage Test
`@ExcludeFromGeneratedCoverageTest` annotation was created to mark classes and/or functions to be 
excluded from coverage test.

Reference: https://github.com/jacoco/jacoco/releases/tag/v0.8.2

***Classes/Functions that can be excluded from coverage test (list can be updated as we progress)***
* Configurations
* Constants
* DTOs
* Exceptions
* Models
* Repositories
* Helper functions that does not contain any logic (i.e. get system date/time)
