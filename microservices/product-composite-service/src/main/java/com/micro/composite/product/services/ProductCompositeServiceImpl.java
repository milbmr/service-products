package com.micro.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

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
    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

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

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
            List<Review> reviews, String serviceAddress) {

        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                .collect(Collectors.toList());
        List<ReviewSummary> reviewSummaries = reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
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
