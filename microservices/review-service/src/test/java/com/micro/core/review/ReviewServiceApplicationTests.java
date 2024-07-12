package com.micro.core.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.micro.api.core.review.Review;
import com.micro.core.review.persistence.ReviewRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests extends PostgresTestBase {

  @Autowired
  WebTestClient client;
  @Autowired
  ReviewRepository repository;

  @BeforeEach
  void setDb() {
    repository.deleteAll();
  }

  @Test
  void getReviewsByProductId() {
    int productId = 1;

    postAndVerifyReview(productId, 1, OK);
    postAndVerifyReview(productId, 2, OK);
    postAndVerifyReview(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).size());

    getAndVerifyReview(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(productId);
  }

  @Test
  void duplicateKey() {
    int productId = 1;
    int reviewId = 1;

    postAndVerifyReview(productId, reviewId, OK)
        .jsonPath("$.productId").isEqualTo(productId)
        .jsonPath("$.reviewId").isEqualTo(reviewId);

    assertEquals(1, repository.count());

    postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Duplicate key, for prodcut id: " + productId);
  }

  @Test
  void deleteReviews() {
    int prodcutId = 1;
    int reviewId = 1;

    postAndVerifyReview(prodcutId, reviewId, OK)
        .jsonPath("$.productId").isEqualTo(prodcutId)
        .jsonPath("$.reviewId").isEqualTo(reviewId);
    assertEquals(1, repository.findByProductId(prodcutId).size());

    deleteAndVerify(prodcutId, OK);
    assertEquals(0, repository.findByProductId(prodcutId).size());

    deleteAndVerify(prodcutId, OK);
  }

  @Test
  void getReviewsInvalidProductId() {
    int productId = -1;

    getAndVerifyReview(productId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Invalid product Id: " + productId);
  }

  @Test
  void getReviewsMissingParameter() {
    getAndVerifyReview("", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
  }

  @Test
  void getReviewsNotFound() {
    int prodcutId = 213;

    getAndVerifyReview(prodcutId, OK)
        .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getReviewsNoneIntParm() {
    getAndVerifyReview("?productId=no-intger", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  private WebTestClient.BodyContentSpec getAndVerifyReview(int productId, HttpStatus expectedStatus) {
    return getAndVerifyReview("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReview(String productPath, HttpStatus expectedStatus) {
    return client.get()
        .uri("/review" + productPath)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int revId,
      HttpStatus expectedStatus) {
    Review review = new Review(productId, revId, "a", "s", "c", "sa");
    return client.post()
        .uri("/review")
        .body(just(review), Review.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerify(int productId, HttpStatus expectedStatus) {
    return client.delete()
        .uri("/review?productId=" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }

}
