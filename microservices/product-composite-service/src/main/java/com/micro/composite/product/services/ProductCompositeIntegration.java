package com.micro.composite.product.services;

import static org.springframework.http.HttpMethod.GET;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.recommendation.RecommendationService;
import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.util.http.HttpErrorInfo;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;

    this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
    this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort
        + "/recommendation";
    this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
  }

  @Override
  public Product createProduct(Product body) {
    try {
      String url = productServiceUrl;

      LOG.debug("Going to create a product for id: " + body.getProductId());
      Product product = restTemplate.postForObject(url, body, Product.class);
      LOG.debug("Created a product for id: " + body.getProductId());

      return product;
    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Product getProduct(int productId) {

    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Will call getProduct Api on url: {}" + url);

      Product product = restTemplate.getForObject(url, Product.class);
      LOG.debug("Found product with id: {}" + product.getProductId());

      return product;
    } catch (HttpClientErrorException ex) {
      throw handleHttpErrorException(ex);
    }
  }

  @Override
  public void deleteProduct(int productId) {
    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Going to call deleteApi on url {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
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
  public List<Recommendation> getRecommendations(int productId) {
    try {
      String url = recommendationServiceUrl + "?productId=" + productId;

      LOG.debug("Will call getRecommendations API on url {}", url);
      List<Recommendation> recommendations = restTemplate
          .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
          }).getBody();

      LOG.debug("Found {} recommendations for product with id: {}", recommendations.size(), productId);

      return recommendations;
    } catch (Exception ex) {
      LOG.warn("Got an exception while requesting recommendations, return none {}", ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public void deleteRecommendations(int productId) {
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
  public Review createReview(Review body) {
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
  public List<Review> getReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;

      LOG.debug("Will call getReviews API on url {}", url);
      List<Review> reviews = restTemplate
          .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
          }).getBody();

      LOG.debug("Found {} reviews for product with id: {}", reviews.size(), productId);

      return reviews;
    } catch (Exception ex) {
      LOG.warn("Got an exception while requesting reviews, return none {}", ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public void deleteReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      LOG.debug("Going to call delete api on url {}", url);
      restTemplate.delete(url);
      LOG.debug("deleted reviews of product id {}", productId);
    } catch (HttpClientErrorException e) {
      throw handleHttpErrorException(e);
    }
  }

  private RuntimeException handleHttpErrorException(HttpClientErrorException ex) {
    switch (HttpStatus.resolve(ex.getStatusCode().value())) {
      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(ex));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(ex));

      default:
        LOG.warn("Got an unexpected http error going to rethrow it, {}", ex.getStatusCode());
        LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }

}
