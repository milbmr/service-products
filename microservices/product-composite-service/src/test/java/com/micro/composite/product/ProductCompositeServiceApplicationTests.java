package com.micro.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.product.Product;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.review.Review;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.composite.product.services.ProductCompositeIntegration;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  @Autowired
  WebTestClient client;

  int PRODUCT_ID_OK = 1;
  int PRODUCT_ID_NOT_FOUND = 2;
  int PRODUCT_ID_NOT_INVALID = 3;

  @MockBean
  ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {
    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
        .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock address"));
    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
        .thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock adderss")));
    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
        .thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
        .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_INVALID))
        .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_NOT_INVALID));
  }

  @Test
  void getProductById() {
    client.get().uri("/product-composite/" + PRODUCT_ID_OK).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  void getProductNotFound() {
    client.get().uri("/product-composite/" + PRODUCT_ID_NOT_FOUND).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(NOT_FOUND)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  void getProductInvalid() {
    client.get().uri("/product-composite/" + PRODUCT_ID_NOT_INVALID).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_INVALID)
        .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_NOT_INVALID);
  }

}
