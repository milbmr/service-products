package com.micro.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.product.Product;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.review.Review;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.composite.product.services.ProductCompositeIntegration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {
  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_NOT_INVALID = 3;

  @Autowired
  WebTestClient client;

  @MockBean
  ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {
    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
        .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock address")));

    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
        .thenReturn(Flux
            .fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock adderss"))));

    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
        .thenReturn(Flux
            .fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
        .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_INVALID))
        .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_NOT_INVALID));
  }

  // @Test
  // void createComposite1() {
  //   ProductAggregate product = new ProductAggregate(1, "name", 1, null, null, null);

  //   postAndVerifyProduct(product, OK);
  // }

  // @Test
  // void createComposite2() {
  //   ProductAggregate product = new ProductAggregate(1, "name", 1,
  //       singletonList(new RecommendationSummary(1, "a", 1, "c")), singletonList(new ReviewSummary(1, "a", "s", "c")),
  //       null);

  //   postAndVerifyProduct(product, OK);
  // }

  // @Test
  // void deleteProduct() {
  //   ProductAggregate product = new ProductAggregate(1, "name", 1,
  //       singletonList(new RecommendationSummary(1, "a", 1, "c")), singletonList(new ReviewSummary(1, "a", "s", "c")),
  //       null);

  //   postAndVerifyProduct(product, OK);

  //   deleteAndVerify(product.getProductId(), OK);
  //   deleteAndVerify(product.getProductId(), OK);
  // }

  @Test
  void getProductById() {
    getAndVerifyProduct(PRODUCT_ID_OK, OK)
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  void getProductNotFound() {
    getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  void getProductInvalid() {
    getAndVerifyProduct(PRODUCT_ID_NOT_INVALID, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_INVALID)
        .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_NOT_INVALID);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return client.get()
        .uri("/product-composite/" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  // private void postAndVerifyProduct(ProductAggregate body, HttpStatus expectedStatus) {
  //   client.post()
  //       .uri("/product-composite")
  //       .body(just(body), ProductAggregate.class)
  //       .exchange()
  //       .expectStatus().isEqualTo(expectedStatus);
  // }

  // private void deleteAndVerify(int productId, HttpStatus expectedStatus) {
  //   client.delete()
  //       .uri("/product-composite/" + productId)
  //       .exchange()
  //       .expectStatus().isEqualTo(expectedStatus);
  // }

}
