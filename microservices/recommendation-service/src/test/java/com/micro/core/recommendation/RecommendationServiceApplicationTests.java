package com.micro.core.recommendation;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests {

  @Autowired
  WebTestClient client;

  @Test
  void getRecommendationsByProductId() {
    int prodcutId = 1;

    client.get().uri("/recommendation?productId=" + prodcutId).accept(APPLICATION_JSON)
        .exchange().expectStatus().isOk().expectHeader().contentType(APPLICATION_JSON)
        .expectBody().jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(prodcutId);
  }

  @Test
  void getRecommendationsNotFound() {
    int prodcutId = 113;

    client.get().uri("/recommendation?productId=" + prodcutId).accept(APPLICATION_JSON)
        .exchange().expectStatus().isOk().expectHeader().contentType(APPLICATION_JSON)
        .expectBody().jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getRecommendationsInvalidParameterString() {
    client.get().uri("/recommendation?productId=no-intger").accept(APPLICATION_JSON)
        .exchange().expectStatus().isEqualTo(BAD_REQUEST).expectHeader().contentType(APPLICATION_JSON)
        .expectBody().jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getRecommendationsInvalidProductId() {
    int prodcutId = -1;

    client.get().uri("/recommendation?productId=" + prodcutId).accept(APPLICATION_JSON)
        .exchange().expectStatus().isEqualTo(UNPROCESSABLE_ENTITY).expectHeader().contentType(APPLICATION_JSON)
        .expectBody().jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Invalid product id" + prodcutId);
  }

  @Test
  void getRecommendationsMissingParameter() {
    client.get().uri("/recommendation").accept(APPLICATION_JSON).exchange()
        .expectStatus().isEqualTo(BAD_REQUEST).expectHeader()
        .contentType(APPLICATION_JSON).expectBody().jsonPath("$.path")
        .isEqualTo("/recommendation").jsonPath("$.message")
        .isEqualTo("Required query parameter 'productId' is not present.");
  }

}
