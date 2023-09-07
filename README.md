[![API v1_internal](https://img.shields.io/badge/API%20Docs-e140ad.svg)](https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/jps-judicial-payment-service.json)

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


### Recreating the users
If you want to recreate the users run the following commands
NOTE: the prune command will remove all volumes not just cftLib
```bash
docker container stop $(docker container ls -a -q)
docker system prune -a -f --volumes
./gradlew bootWithCCD
```
you then may see this error
```bash
 ERROR [restartedMain] org.springframework.boot.SpringApplicationorg.springframework.beans.factory.BeanCreationException: Error creating bean with name 'cftLibConfig': Invocation of init method failed; nested exception is java.lang.IllegalStateException: Could not find a valid Docker environment. Please see logs and check configuration
 ```
 to solve this run
```bash
sudo ln -s $HOME/.docker/run/docker.sock /var/run/docker.sock
./gradlew bootWithCCD
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

Run the distribution (created in `build/install/jps-judicial-payment-service` directory)
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

### Smoke tests
To run all smoke tests execute the following command:
```bash
  ./gradlew smoke
```

### Functional tests
To run all functional tests execute the following command:
```bash
  ./gradlew functional
```
To specify a particular test or feature for execution, update the tag within the functional task in the build.gradle file with the desired test's tag.
