package com.micro.composite.product;

import static java.util.Collections.singletonList;
import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.micro.composite.product.IsSameEvent.sameEventExcept;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.micro.api.composite.product.RecommendationSummary;
import com.micro.api.composite.product.ReviewSummary;
import com.micro.api.core.product.Product;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.review.Review;
import com.micro.api.event.Event;

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

  @Test
  void createCompositeProduct1() {
    ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
    postProduct(composite, ACCEPTED);

    List<String> products = getMessages("products");
    List<String> recommendations = getMessages("recommendations");
    List<String> reviews = getMessages("reviews");

    assertEquals(1, products.size());

    Event<Integer, Product> expectedEvent = new Event<>(CREATE, composite.getProductId(),
        new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));

    assertThat(products.get(0), is(sameEventExcept(expectedEvent)));
    assertEquals(0, recommendations.size());
    assertEquals(0, reviews.size());
  }

  @Test
  void createCompositeProduct2() {
    ProductAggregate composite = new ProductAggregate(1, "name", 2,
        singletonList(new RecommendationSummary(1, "a", 1, "r")), singletonList(new ReviewSummary(1, "a", "s", "c")),
        null);
    postProduct(composite, ACCEPTED);

    List<String> products = getMessages("products");
    List<String> recommendations = getMessages("recommendations");
    List<String> reviews = getMessages("reviews");

    assertEquals(1, products.size());

    Event<Integer, Product> expectedProductEvent = new Event<>(CREATE, composite.getProductId(),
        new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
    assertThat(products.get(0), is(sameEventExcept(expectedProductEvent)));

    assertEquals(1, recommendations.size());

    RecommendationSummary rec = composite.getRecommendations().get(0);
    Event<Integer, Recommendation> expectedRecommendationsEvent = new Event<>(CREATE, composite.getProductId(),
        new Recommendation(composite.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(),
            rec.getContent(), null));
    assertThat(recommendations.get(0), is(sameEventExcept(expectedRecommendationsEvent)));

    assertEquals(1, reviews.size());

    ReviewSummary rev = composite.getReviews().get(0);
    Event<Integer, Review> expectedReviewsEvent = new Event<>(CREATE, composite.getProductId(), new Review(
        composite.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null));
    assertThat(reviews.get(0), is(sameEventExcept(expectedReviewsEvent)));
  }

  @Test
  void deleteComposite() {
    deleteProduct(1, ACCEPTED);

    List<String> products = getMessages("products");
    List<String> recs = getMessages("recommendations");
    List<String> revs = getMessages("reviews");

    assertEquals(1, products.size());

    Event<Integer, Product> proEvent = new Event<>(DELETE, 1, null);
    assertThat(products.get(0), is(sameEventExcept(proEvent)));

    assertEquals(1, recs.size());

    Event<Integer, Recommendation> recEvent = new Event<>(DELETE, 1, null);
    assertThat(recs.get(0), is(sameEventExcept(recEvent)));

    assertEquals(1, revs.size());

    Event<Integer, Review> revEvent = new Event<>(DELETE, 1, null);
    assertThat(revs.get(0), is(sameEventExcept(revEvent)));
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
