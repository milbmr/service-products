package com.micro.core.product;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Test
  void getProductById() {
    int productId = 1;
    client.get().uri("/product/" + productId).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  void getInvalidParametereString() {
    client.get().uri("/product/no-intger").accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(BAD_REQUEST)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/no-intger");
  }

  @Test
  void getProductNotFound() {
    int productId = 13;
    client.get().uri("/product/" + productId).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(NOT_FOUND)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/" + productId)
        .jsonPath("$.message").isEqualTo("Not found product Id: " + productId);
  }

  @Test
  void getProductInvalidParameterNegative() {
    int productId = -1;

    client.get().uri("/product/" + productId).accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/" + productId)
        .jsonPath("$.message").isEqualTo("Invalid product Id: " + productId);
  }

}
