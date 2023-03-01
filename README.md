# Spring Boot application template

## Purpose

Purpose of the judicial-payment-service goes here

## Building and deploying the application

### Prerequisites

- [JDK 17](https://java.com)
- [Docker](https://www.docker.com)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

The easiest way to run the application locally is to use the `bootWithCCD` Gradle task.
First time running or when you pull new images you will first need to run the below 
```bash
 az acr login --name hmctsprivate --subscription DCD-CNP-PROD
 az acr login --name hmctspublic --subscription DCD-CNP-PROD
```
All subsequent times of starting the application locally you can just run without the above
```bash
 ./gradlew bootWithCCD
```

This will start the application and its dependent services.

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative running the application

Create the image of the application by executing the following command:

```bash
 ./gradlew assemble
```

Create docker image:

```bash
docker-compose build
```

Run the distribution (created in `build/install/hmc-judicial-payment-service` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
curl http://localhost:4561/health
```

You should get a response similar to this:

```
{"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```


## Developing

### Unit tests
To run all unit tests execute the following command:
```bash
  ./gradlew test
```
### Integration tests
To run all integration tests execute the following command:
```bash
  ./gradlew integration
```
### Functional tests
To run all integration tests execute the following command:
```bash
  ./gradlew functionalTest
```

