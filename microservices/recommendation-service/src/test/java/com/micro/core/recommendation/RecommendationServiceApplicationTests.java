package com.micro.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.HttpStatus.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.recommendation.Recommendation;
import com.micro.core.recommendation.persistence.RecommendationRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoTestBase {

  @Autowired
  WebTestClient client;
  @Autowired
  RecommendationRepository repository;

  @BeforeEach
  void setDb() {
    repository.deleteAll().block();
  }

  @Test
  void getRecommendationsByProductId() {
    int productId = 1;

    postAndVerifyRecommendation(productId, 1, OK);
    postAndVerifyRecommendation(productId, 2, OK);
    postAndVerifyRecommendation(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).count().block());

    getAndVerifyRecommendation(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(productId);
  }

  @Test
  void duplicateKey() {
    int productId = 1;
    int recommendationId = 1;

    postAndVerifyRecommendation(productId, recommendationId, OK)
        .jsonPath("$.productId").isEqualTo(productId)
        .jsonPath("$.recommendationId").isEqualTo(recommendationId);

    assertEquals(1, repository.count().block());

    postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Duplicate key, prodcut id: " + productId);
  }

  @Test
  void deleteRecommendations() {
    int prodcutId = 1;
    int recommendationId = 1;

    postAndVerifyRecommendation(prodcutId, recommendationId, OK)
        .jsonPath("$.productId").isEqualTo(prodcutId)
        .jsonPath("$.recommendationId").isEqualTo(recommendationId);
    assertEquals(1, repository.findByProductId(prodcutId).count().block());

    deleteAndVerify(prodcutId, OK);
    assertEquals(0, repository.findByProductId(prodcutId).count().block());

    deleteAndVerify(prodcutId, OK);
  }

  @Test
  void getRecommendationsNotFound() {
    int prodcutId = 113;

    getAndVerifyRecommendation(prodcutId, OK)
        .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getRecommendationsInvalidParameterString() {
    getAndVerifyRecommendation("?productId=no-intger", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getRecommendationsInvalidProductId() {
    int prodcutId = -1;

    getAndVerifyRecommendation(prodcutId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Invalid product id" + prodcutId);
  }

  @Test
  void getRecommendationsMissingParameter() {
    getAndVerifyRecommendation("", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
  }

  private WebTestClient.BodyContentSpec getAndVerifyRecommendation(int productId, HttpStatus expectedStatus) {
    return getAndVerifyRecommendation("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyRecommendation(String productPath, HttpStatus expectedStatus) {
    return client.get()
        .uri("/recommendation" + productPath)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recId,
      HttpStatus expectedStatus) {
    Recommendation recommendation = new Recommendation(productId, recId, "a", recId, "c", "sa");
    return client.post()
        .uri("/recommendation")
        .body(just(recommendation), Recommendation.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerify(int productId, HttpStatus expectedStatus) {
    return client.delete()
        .uri("/recommendation?productId=" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }

}
