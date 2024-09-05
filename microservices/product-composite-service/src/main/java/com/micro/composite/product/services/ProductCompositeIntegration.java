package com.micro.composite.product.services;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Mono.empty;
import static com.micro.api.event.Event.Type.CREATE;
import static com.micro.api.event.Event.Type.DELETE;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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

    private final ObjectMapper mapper;
    private final WebClient webClient;

    private final String productServiceUrl = "http://product";
    private final String recommendationServiceUrl = "http://recommendation";
    private final String reviewServiceUrl = "http://review";
    private final StreamBridge streamBridge;
    private final Scheduler publicherScheduler;

    public ProductCompositeIntegration(
            ObjectMapper mapper,
            WebClient.Builder webClient,
            StreamBridge streamBridge,
            Scheduler publicherScheduler) {
        this.mapper = mapper;
        this.webClient = webClient.build();
        this.streamBridge = streamBridge;
        this.publicherScheduler = publicherScheduler;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publicherScheduler);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/product/" + productId;
        LOG.debug("Will call getProduct Api on url: {}", url);

        return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publicherScheduler).then();
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publicherScheduler);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        LOG.debug("Will call getRecommendations API on url {}", url);

        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), FINE)
                .onErrorResume(ex -> empty());

    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publicherScheduler).then();
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publicherScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        LOG.debug("Will call getReviews API on url {}", url);

        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE)
                .onErrorResume(ex -> empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
                .subscribeOn(publicherScheduler).then();
    }

    public Mono<Health> getProductHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationsHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewsHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call health endpoint on url {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), FINE);
    }

    @SuppressWarnings("rawtypes")
    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to a {}", event.getEventType(), bindingName);
        Message<?> message = MessageBuilder.withPayload(event).setHeader("partitionKey", event.getKey()).build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Unknown exception will rethrow it {}", ex.toString());
            return ex;
        }

        WebClientResponseException were = (WebClientResponseException) ex;

        switch (HttpStatus.resolve(were.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(were));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(were));

            default:
                LOG.warn("Got an unexpected http error going to rethrow it, {}", were.getStatusCode());
                LOG.warn("Error body: {}", were.getResponseBodyAsString());
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
