package com.micro.composite.product.services;

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
import com.micro.api.exceptions.NotFoundException;
import com.micro.util.http.ServiceUtil;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class ProductCompositeServiceImpl implements ProductCompositeService {

  private final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private ProductCompositeIntegration integration;

  @Override
  public void createProduct(ProductAggregate body) {
    try {
      LOG.debug("Going to create product composite for product id {}", body.getProductId());
      integration.createProduct(
          new Product(body.getProductId(), body.getName(), body.getWeight(), null));

      if (body.getRecommendations() != null) {
        body.getRecommendations()
            .forEach(r -> {
              Recommendation rec = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(),
                  r.getRate(),
                  r.getContent(), null);
              integration.createRecommendation(rec);
            });
      }

      if (body.getReviews() != null) {
        body.getReviews().forEach(r -> {
          Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
              r.getContent(), null);
          integration.createReview(review);
        });
      }

      LOG.debug("Prodcut composite created for product id {}", body.getProductId());
    } catch (RuntimeException e) {
      LOG.warn("CreateComposite failed {}", e);
      throw e;
    }
  }

  @Override
  public ProductAggregate getProduct(int productId) {
    Product product = integration.getProduct(productId);

    if (product == null) {
      throw new NotFoundException("No product found for product id: " + productId);
    }

    List<Recommendation> recommendations = integration.getRecommendations(productId);
    List<Review> reviews = integration.getReviews(productId);

    return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
  }

  @Override
  public void deleteProduct(int productId) {
      LOG.debug("Going to delete composite for product id {}", productId);
      integration.deleteProduct(productId);
      integration.deleteRecommendations(productId);
      integration.deleteReviews(productId);
      LOG.debug("deleted composite for product id {}", productId);
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
