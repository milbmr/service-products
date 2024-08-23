package com.micro.composite.product.services;

import static java.util.logging.Level.FINE;
import static org.springframework.http.HttpMethod.GET;
import static reactor.core.publisher.Mono.empty;
import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.recommendation.RecommendationService;
import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.event.Event;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.util.http.HttpErrorInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;
  private final WebClient webClient;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;
  private final StreamBridge streamBridge;

  public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper mapper, WebClient.Builder webClient,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort,
      StreamBridge streamBridge,
      ) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;
    this.webClient = webClient.build();

    this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
    this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort
        + "/recommendation";
    this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    this.streamBridge = streamBridge;
  }

  @Override
  public Mono<Product> createProduct(Product body) {
    return Mono.fromCallable(() -> {
      sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publi)
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    String url = productServiceUrl + "/" + productId;
    LOG.debug("Will call getProduct Api on url: {}" + url);

    return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), FINE)
        .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Going to call deleteApi on url {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    try {
      String url = recommendationServiceUrl;
      LOG.debug("will post a new recommendation to url {}", url);
      Recommendation rec = restTemplate.postForObject(url, body, Recommendation.class);
      LOG.debug("Recommendation created with product id {}", body.getProductId());

      return rec;

    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    String url = recommendationServiceUrl + "?productId=" + productId;
    LOG.debug("Will call getRecommendations API on url {}", url);

    return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), FINE)
        .onErrorResume(ex -> empty());

  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    try {
      String url = recommendationServiceUrl + "?productId=" + productId;
      LOG.debug("going to call delete api on url {}", url);

      restTemplate.delete(url);
      LOG.debug("recommdendation of product id {} was deleted", productId);

    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Mono<Review> createReview(Review body) {
    try {
      String url = reviewServiceUrl;
      LOG.debug("going to create review on url {}", url);
      Review review = restTemplate.postForObject(url, body, Review.class);
      LOG.debug("Review created of product id {}", body.getProductId());

      return review;

    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    String url = reviewServiceUrl + "?productId=" + productId;
    LOG.debug("Will call getReviews API on url {}", url);

    return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE)
        .onErrorResume(ex -> empty());
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      LOG.debug("Going to call delete api on url {}", url);
      restTemplate.delete(url);
      LOG.debug("deleted reviews of product id {}", productId);
    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  private void sendMessage(String bindingName, Event event) {
    LOG.debug("Sending a {} message to a {}", event.getEventType(), bindingName);
    Message<?> message = MessageBuilder.withPayload(event).setHeader("partionKey", event.getKey()).build();
    streamBridge.send(bindingName, message);
  }

  private Throwable handleException(Throwable ex) {
    if (!(ex instanceof WebClientResponseException)) {
      LOG.warn("Unkown exception will rethrow it {} " + ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException) ex;

    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(wcre));

      default:
        LOG.warn("Got an unexpected http error going to rethrow it, {}", wcre.getStatusCode());
        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }

}
