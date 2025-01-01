# Microservices Project

This is an ongoing project that creates a complete microservices in Docker containers.
The services are implemented
in Java using Spring, Spring Boot and Spring Cloud.

It uses three core microservices:
- Product service.
- Recommendation service.
- Review service.
- Additional microservice for exposing the three core microservices api.

## Technologies

- Eureka for service discovery.
- Project Reactor to handle non Blocking i/o operations using reactive programming.
- Kafka and Rabbitmq for Event based messaging system to handle create and delete requests.
- Postgres to store products and recommendations and Mongodb to store reviews.
- OpenApi using springdoc to document the Rest api.


## To be added

- Gateway edge server for exposing the api endpoints.
- OAuth 2.0 to handle authentication.
- Centralized configuration using Spring cloud config server.
- Resilience4j as a circuit breaker and rate limiter to improve the resilience and fault tolerance.
- Zipkin for distributed tracing.
- Kubernetes for deployment and configuration.
- Prometheus and Grafana for monitoring and metrics.


## How To Run

### Prerequisites

- Java version 22 or above is required
- Docker

### Getting started

cd into the main project directory where gradlew bat file is located and run this commands.

```sh
./gradlew build && docker compose build
```

```sh
docker compose up -d
```

to make sure that all services are up and running before trying the api run this curl command to check the health of the microservices using actuator

```sh
curl localhost:8080/actuator/health | jq
```

to check that all microservices are registered correctly with Eureka navigate to http://localhost:8761

#### Trying out the api

to try the api run the following commands.

```sh
curl -X POST localhost:8080/product-composite -H "Content-Type:
application/json" --data "{"productId":1,"name":"product name C","weight":300,
"recommendations":[
{"recommendationId":1,"author":"author 1","rate":1,"content":"content
1"},
{"recommendationId":2,"author":"author 2","rate":2,"content":"content
2"},
{"recommendationId":3,"author":"author 3","rate":3,"content":"content
3"}
], "reviews":[
{"reviewId":1,"author":"author 1","subject":"subject
1","content":"content 1"},
{"reviewId":2,"author":"author 2","subject":"subject
2","content":"content 2"},
{"reviewId":3,"author":"author 3","subject":"subject
3","content":"content 3"}
]}"
```

to fetch the posted content

```sh
curl localhost:8080/product-composite/1 | jq
```

to delete the content

```sh
curl -X DELETE localhost:8080/product-composite/1
```

the service is run originally with rabbitmq to try it out with kafka instead run the following:

```sh
docker compose down
export COMPOSE_FILE=docker-compose-kafka.yml
docker build && docker compose up -d
```

to stop the microservice

```sh
docker compose down
unset COMPOSE_FILE
```