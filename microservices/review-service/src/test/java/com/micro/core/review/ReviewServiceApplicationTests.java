package com.micro.core.review;

import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.review.Review;
import com.micro.api.event.Event;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.core.review.persistence.ReviewRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "eureka.client.enabled=false")
class ReviewServiceApplicationTests extends PostgresTestBase {

  @Autowired
  WebTestClient client;
  @Autowired
  ReviewRepository repository;
  @Autowired
  @Qualifier("messageProcessor")
  private Consumer<Event<Integer, Review>> messageProcessor;

  @BeforeEach
  void setDb() {
    repository.deleteAll();
  }

  @Test
  void getReviewsByProductId() {
    int productId = 1;

    sendCreateMessage(productId, 1);
    sendCreateMessage(productId, 2);
    sendCreateMessage(productId, 3);

    assertEquals(3, repository.findByProductId(productId).size());

    getAndVerifyReview(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(productId);
  }

  @Test
  void duplicateKey() {
    int productId = 1;
    int reviewId = 1;

    sendCreateMessage(productId, reviewId);

    assertEquals(1, repository.count());

    InvalidInputException thrown = assertThrows(InvalidInputException.class,
        () -> sendCreateMessage(productId, reviewId), "Except to throw invalid input exception");

    assertEquals("Duplicate key, for prodcut id: " + productId, thrown.getMessage());

    assertEquals(1, repository.count());
  }

  @Test
  void deleteReviews() {
    int prodcutId = 1;
    int reviewId = 1;

    sendCreateMessage(prodcutId, reviewId);
    assertEquals(1, repository.findByProductId(prodcutId).size());

    sendDeleteMessage(reviewId);
    assertEquals(0, repository.findByProductId(prodcutId).size());

    sendDeleteMessage(reviewId);
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

  private void sendCreateMessage(int productId, int reviewId) {
    Review review = new Review(productId, reviewId, "a", "s", "c", null);
    Event<Integer, Review> event = new Event<Integer, Review>(CREATE, review.getProductId(), review);
    messageProcessor.accept(event);
  }

  private void sendDeleteMessage(int productId) {
    Event<Integer, Review> event = new Event<Integer, Review>(DELETE, productId, null);
    messageProcessor.accept(event);
  }

  // private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int
  // revId,
  // HttpStatus expectedStatus) {
  // Review review = new Review(productId, revId, "a", "s", "c", "sa");
  // return client.post()
  // .uri("/review")
  // .body(just(review), Review.class)
  // .accept(APPLICATION_JSON)
  // .exchange()
  // .expectStatus().isEqualTo(expectedStatus)
  // .expectHeader().contentType(APPLICATION_JSON)
  // .expectBody();
  // }

  // private WebTestClient.BodyContentSpec deleteAndVerify(int productId,
  // HttpStatus expectedStatus) {
  // return client.delete()
  // .uri("/review?productId=" + productId)
  // .accept(APPLICATION_JSON)
  // .exchange()
  // .expectStatus().isEqualTo(expectedStatus)
  // .expectBody();
  // }

}
