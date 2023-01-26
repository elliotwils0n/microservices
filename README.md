# microservices

## Table of contents
* [General info](#general-info)
* [Microservices](#microservices)
* [Requirements](#requirements)
* [Startup instructions](#startup-instructions)
* [Usage](#usage)

## General info
An example of microservices architecture with Service Discovery and API Gateway.
Communication between microservices is made by using message queues (RabbitMQ).

## Microservices
- bom - contains versions of dependencies used across microservices
- core - contains objects and logic shared between microservices
- eureka-server - eureka discovery server for microservices
- gateway - API gateway for microservices
- user - microservice for user management
- verification - microservice for user verification

## Requirements
[__Docker installation__](#docker-installation):
- Docker Engine[^1]

[__Manual installation__](#manual-installation):
- RabbitMQ, Java 11, Maven

## Startup instructions

### Docker installation

#### All-in-one (one instance of each microservice)

* Starting up the project, run in project's root directory

    ```docker compose up```

* Shutting down the project (deleting containers and networks, keeping images and volumes)

    ```docker compose down```

* Shutting down and clean up (deleting containers, networks, images and volumes)

    ```docker compose down --rmi local -v```

#### Running microservices independently (manual instances management)

##### Build bom, core dependencies and build and run eureka-server and gateway 

* Starting up the project, run in project's root directory

    ```docker compose -f docker-compose-base.yml up```

* Shutting down the project (deleting containers and networks, keeping images and volumes)

    ```docker compose -f docker-compose-base.yml down```

* Shutting down and clean up (deleting containers, networks, images and volumes)

    ```docker compose -f docker-compose-base.yml down --rmi local -v```

##### Start up each microservice

To be able to run miltiple instances of same microservice add project name to your docker compose command (-p PROJECT_NAME)

* Starting up the microservice, run in microservice's directory (i.e. user):

    ```docker compose -p [PROJECT] up```

* Shutting down the project (deleting containers and networks, keeping images and volumes)

    ```docker compose -p [PROJECT] down```

* Shutting down and clean up (deleting containers, networks, images and volumes)

    ```docker compose -p [PROJECT] down --rmi local -v```

### Manual installation

* Start RabbitMQ with default ports 5672 and 15672.

    If you have docker installed you can use docker compose file, run in project's root directory:

    ```docker compose -f docker-rabbitmq.yml up```

    to close it, use 

    ```docker compose -f docker-rabbitmq.yml down --rmi local -v```

* Install bom locally, run in bom directory:

    ```mvn clean install```

* Install core locally, run in core directory:

    ```mvn clean install```

* Compile and run eureka discovery server, run in eureka-server directory:

    ```mvn clean package```

    ```mvn spring-boot:run```

* Compile and run gateway API microservice, run in gateway directory:

    ```mvn clean package```

    ```mvn spring-boot:run -Dspring-boot.run.profiles=dev```

* Compile and run any of the microservices, run in their directory:

    ```mvn clean package```

    ```mvn spring-boot:run -Dspring-boot.run.profiles=dev```

## Usage

#### Links

* Eureka: [localhost:8761](http://localhost:8761/)

* RabbitMQ: [localhost:15672](http://localhost:15672/#/queues)

* API gateway: [localhost:8080](http://localhost:8080)

#### UML Diagram

![UML Diagram](docs/microservices-uml.drawio.png?raw=true)

#### API

* [Postman collection](docs/microservices.postman_collection.json)

* Create user

    ```shell
    curl \
      -X POST \
      -H "Content-Type: application/json" \
      -d \
      '{
        "username": "elliotwils0n",
        "email": "elliotwils0n@github.io",
        "password": "password"
      }' \
      http://localhost:8080/service/user/api/v1/user/register
    ```
* Check account status

    ```shell
    curl \
      -X POST \
      -H "Content-Type: application/json" \
      -d \
      '{
        "email": "elliotwils0n@github.io",
        "password": "password"
      }' \
      http://localhost:8080/service/user/api/v1/user/checkStatus
    ```

* Verify account (for simplicity link is added in verication µs debug logs)

    ```shell
    curl -G \
      -X GET \
      -d user=${USER_UUID} \
      -d hash=${HASH} \
      http://localhost:8080/service/verification/api/v1/verification/verify
    ```

[^1]:[Docker Engine](https://docs.docker.com/engine/install/)
