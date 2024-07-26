package com.micro.core.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.*;
import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.core.product.Product;
import com.micro.api.event.Event;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.core.product.persistence.ProductRepository;

import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ProductRepository repository;

  @Autowired
  @Qualifier("messageProcessor")
  private Consumer<Event<Integer, Product>> messageProcessor;

  @BeforeEach
  void setDb() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();
  }

  @Test
  void getProductById() {
    int productId = 1;

    assertNull(repository.findByProductId(productId).block());
    assertEquals(0, (Long) repository.count().block());

    sendCreateEvent(productId);
    assertNotNull(repository.findByProductId(productId).block());
    assertEquals(1, (Long) repository.count().block());

    getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  void duplicateKey() {
    int productId = 1;

    assertNull(repository.findByProductId(productId).block());
    sendCreateEvent(productId);
    assertNotNull(repository.findByProductId(productId).block());

    InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> sendCreateEvent(productId),
        "Expected invalid input exception here");

    assertEquals("Duplicate key, product id: " + productId, thrown.getMessage());
  }

  @Test
  void deleteProduct() {
    int productId = 1;

    assertNull(repository.findByProductId(productId).block());
    sendCreateEvent(productId);
    assertNotNull(repository.findByProductId(productId).block());

    sendDeleteEvent(productId);
    assertNull(repository.findByProductId(productId).block());

    sendDeleteEvent(productId);
  }

  @Test
  void getInvalidParametereString() {
    getAndVerifyProduct("/no-intger", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/product/no-intger");
  }

  @Test
  void getProductNotFound() {
    int productId = 13;

    getAndVerifyProduct(productId, NOT_FOUND)
        .jsonPath("$.message").isEqualTo("No product found " + productId)
        .jsonPath("$.path").isEqualTo("/product/" + productId);
  }

  @Test
  void getProductInvalidParameterNegative() {
    int productId = -1;

    getAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.message").isEqualTo("Invalid product Id: " + productId)
        .jsonPath("$.path").isEqualTo("/product/" + productId);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int prodcutId, HttpStatus expectedStatus) {
    return getAndVerifyProduct("/" + prodcutId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
    return client.get().uri("/product" + productIdPath).accept(APPLICATION_JSON).exchange()
        .expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
  }

  // private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId,
  // HttpStatus httpStatus) {
  // Product product = new Product(productId, "name", productId, "address");
  // return client.post()
  // .uri("/product")
  // .body(just(product), Product.class)
  // .accept(APPLICATION_JSON)
  // .exchange()
  // .expectStatus().isEqualTo(httpStatus)
  // .expectHeader().contentType(APPLICATION_JSON)
  // .expectBody();
  // }

  // private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int prodcutId,
  // HttpStatus expextedHttpStatus) {
  // return client.delete()
  // .uri("/product/" + prodcutId)
  // .accept(APPLICATION_JSON)
  // .exchange()
  // .expectStatus().isEqualTo(expextedHttpStatus)
  // .expectBody();
  // }

  private void sendCreateEvent(int productId) {
    Product product = new Product(productId, "name", 1, null);
    Event<Integer, Product> event = new Event<Integer, Product>(CREATE, productId, product);
    messageProcessor.accept(event);
  }

  private void sendDeleteEvent(int productId) {
    Event<Integer, Product> event = new Event<Integer, Product>(DELETE, productId, null);
    messageProcessor.accept(event);
  }
}
