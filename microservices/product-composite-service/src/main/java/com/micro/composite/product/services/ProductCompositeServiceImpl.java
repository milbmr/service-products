package com.micro.composite.product.services;

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.composite.product.ProductAggregate;
import com.micro.api.composite.product.ProductCompositeService;
import com.micro.api.composite.product.RecommendationSummary;
import com.micro.api.composite.product.ReviewSummary;
import com.micro.api.composite.product.ServiceAddresses;
import com.micro.api.core.product.Product;
import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.review.Review;
import com.micro.util.http.ServiceUtil;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ProductCompositeServiceImpl implements ProductCompositeService {

  private final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private ProductCompositeIntegration integration;

  @Override
  public Mono<Void> createProduct(ProductAggregate body) {
    try {
      LOG.debug("Going to create product composite for product id {}", body.getProductId());

      List<Mono<?>> monoList = new ArrayList<Mono<?>>();
      monoList.add(integration.createProduct(
          new Product(body.getProductId(), body.getName(), body.getWeight(), null)));

      if (body.getRecommendations() != null) {
        body.getRecommendations()
            .forEach(r -> {
              Recommendation rec = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(),
                  r.getRate(),
                  r.getContent(), null);
              monoList.add(integration.createRecommendation(rec));
            });
      }

      if (body.getReviews() != null) {
        body.getReviews().forEach(r -> {
          Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
              r.getContent(), null);
          monoList.add(integration.createReview(review));
        });
      }

      LOG.debug("Prodcut composite created for product id {}", body.getProductId());
      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
          .doOnError(ex -> LOG.warn("product composite failed {}" + ex.toString())).then();

    } catch (RuntimeException e) {
      LOG.warn("CreateComposite failed {}", e);
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ProductAggregate> getProduct(int productId) {
    LOG.info("Going to get product composite for product id: " + productId);
    return Mono.zip(
        values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1],
            (List<Review>) values[2], serviceUtil.getServiceAddress()),
        integration.getProduct(productId), integration.getRecommendations(productId).collectList(),
        integration.getReviews(productId).collectList())
        .doOnError(ex -> LOG.warn("getProductComposite failed {} " + ex.toString()))
        .log(LOG.getName(), FINE);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    try {
      LOG.debug("Going to delete composite for product id {}", productId);
      return Mono.zip(r -> "",
          integration.deleteProduct(productId),
          integration.deleteRecommendations(productId),
          integration.deleteReviews(productId))
          .doOnError(ex -> LOG.warn("delete product composiet failed {} " + ex.toString())).then();
    } catch (RuntimeException e) {
      LOG.warn("delete composition failed {} " + e.toString());
      throw e;
    }
  }

  private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
      List<Review> reviews, String serviceAddress) {

    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();

    List<RecommendationSummary> recommendationSummaries = recommendations.stream()
        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
        .collect(Collectors.toList());
    List<ReviewSummary> reviewSummaries = reviews.stream()
        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
        .collect(Collectors.toList());

    String productAddress = product.getServiceAddress();
    String recommendationAddress = (recommendations != null && recommendations.size() > 1)
        ? recommendations.get(0).getServiceAddress()
        : "";
    String reviewAddress = (reviews != null && reviews.size() > 1) ? reviews.get(0).getServiceAddress() : "";

    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress,
        recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries,
        serviceAddresses);
  }
}
