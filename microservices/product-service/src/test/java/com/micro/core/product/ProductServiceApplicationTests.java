package com.micro.core.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static reactor.core.publisher.Mono.just;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.product.Product;
import com.micro.core.product.persistence.ProductRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoTestBase {

  @Autowired
  private WebTestClient client;
  @Autowired
  private ProductRepository repository;

  @BeforeEach
  void setDb() {
    repository.deleteAll();
  }

  @Test
  void getProductById() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);
    assertTrue(repository.findByProductId(productId).isPresent());

    getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  void duplicateKey() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);
    assertTrue(repository.findByProductId(productId).isPresent());

    postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.message")
        .isEqualTo("Duplicate key, product id: " + productId)
        .jsonPath("$.path")
        .isEqualTo("/prodcut");
  }

  @Test
  void deleteProduct() {
    int productId = 1;

    postAndVerifyProduct(productId, OK);
    assertTrue(repository.findByProductId(productId).isPresent());

    deleteAndVerifyProduct(productId, OK);
    assertFalse(repository.findByProductId(productId).isPresent());

    deleteAndVerifyProduct(productId, OK);
  }

  @Test
  void getInvalidParametereString() {
    getAndVerifyProduct("/no-intger", BAD_REQUEST);
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

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int prodcutId, HttpStatus expectedStatus) {
    return getAndVerifyProduct("/" + prodcutId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
    return client.get().uri("/product" + productIdPath).accept(APPLICATION_JSON).exchange()
        .expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus httpStatus) {
    Product product = new Product(productId, "name", productId, "address");
    return client.post().uri("/product").body(just(product), Product.class).accept(APPLICATION_JSON)
        .exchange().expectStatus().isEqualTo(httpStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
  }

  private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int prodcutId, HttpStatus expextedHttpStatus) {
    return client.delete().uri("/product/" + prodcutId).accept(APPLICATION_JSON)
        .exchange().expectStatus().isEqualTo(expextedHttpStatus)
        .expectHeader().contentType(APPLICATION_JSON).expectBody();
  }

}
