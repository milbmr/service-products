package com.micro.composite.product;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.micro.api.composite.product.ProductAggregate;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(TestChannelBinderConfiguration.class)
class MessagingTests {
  private final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

  @Autowired
  private WebTestClient client;

  @Autowired
  private OutputDestination target;

  @BeforeEach
  void setUp() {
    purgeMessages("products");
    purgeMessages("recommendations");
    purgeMessages("reviews");
  }


  private void purgeMessages(String bindingName) {
    getMessages(bindingName);
  }

  private List<String> getMessages(String bindingName) {
    List<String> messages = new ArrayList<>();
    boolean moreMessages = true;

    while (moreMessages) {
      Message<byte[]> message = getMessage(bindingName);

      if (message == null) {
        moreMessages = false;
      } else {
        messages.add(new String(message.getPayload()));
      }
    }
    return messages;
  }

  private Message<byte[]> getMessage(String bindingName) {
    try {
      return target.receive(0, bindingName);
    } catch (NullPointerException npe) {
      LOG.error("getMessage() got a null pointer for binding {}", bindingName);
      return null;
    }
  }

  private void postProduct(ProductAggregate productAggregate, HttpStatus httpStatus) {
    client.post()
        .uri("/product-composite")
        .body(just(productAggregate), ProductAggregate.class)
        .exchange()
        .expectStatus().isEqualTo(httpStatus);
  }

  private void deleteProduct(int productId, HttpStatus httpStatus) {
    client.delete()
        .uri("/product-composite/" + productId)
        .exchange()
        .expectStatus().isEqualTo(httpStatus);
  }
}
